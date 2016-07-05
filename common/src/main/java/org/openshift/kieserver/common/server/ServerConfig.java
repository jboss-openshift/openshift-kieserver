/**
 *  Copyright 2016 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package org.openshift.kieserver.common.server;

import static org.kie.server.api.KieServerConstants.KIE_SERVER_ID;
import static org.kie.server.api.KieServerConstants.KIE_SERVER_STATE_REPO;
import static org.kie.server.api.model.KieContainerStatus.STARTED;
import static org.openshift.kieserver.common.id.ReleaseIdComparator.SortDirection.DESCENDING;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.kie.api.builder.ReleaseId;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.KieServerConfigItem;
import org.kie.server.services.impl.storage.KieServerState;
import org.openshift.kieserver.common.coder.Coder;
import org.openshift.kieserver.common.coder.SumCoder.MD5;
import org.openshift.kieserver.common.id.ComparableReleaseId;
import org.openshift.kieserver.common.id.ReleaseIdComparator;

import com.thoughtworks.xstream.XStream;

public class ServerConfig {

    private static final ServerConfig INSTANCE = new ServerConfig(
            System.getenv("KIE_SERVER_REPO"),
            System.getenv("KIE_SERVER_ID"),
            System.getenv("KIE_SERVER_STATE_FILE"),
            System.getenv("KIE_CONTAINER_DEPLOYMENT"),
            System.getenv("KIE_CONTAINER_REDIRECT_ENABLED"));

    private final String serverRepo;
    private final String serverId;
    private final String serverStateFile;
    private final String containerDeployment;
    private final boolean containerRedirectEnabled;
    private final Coder coder;
    private final Map<String,Set<ReleaseId>> containerAliases_releaseIds;
    private final Map<String,String> containerConfigs_deploymentIds;
    private final Map<String,String> deploymentIds_containerAliases;

    // package-protected for JUnit testing
    ServerConfig(
            String serverRepo,
            String serverId,
            String serverStateFile,
            String containerDeployment,
            String containerRedirectEnabled) {
        this.serverRepo = serverRepo != null ? serverRepo : ".";
        this.serverId = serverId != null ? serverId : "kieserver";
        this.serverStateFile = serverStateFile != null ? serverStateFile : new File(this.serverRepo, this.serverId + ".xml").getPath();
        try {
            String serverRepoDir = new File(this.serverRepo).getCanonicalPath();
            String serverStateFileDir = new File(this.serverStateFile).getParentFile().getCanonicalPath();
            if (!serverRepoDir.equals(serverStateFileDir)) {
                throw new IllegalArgumentException(String.format(
                        "serverStateFile: %s with serverId: %s must exist in serverRepo: %s",
                        this.serverStateFile, this.serverId, this.serverRepo));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.containerDeployment = containerDeployment != null ? containerDeployment.trim() : "";
        this.containerRedirectEnabled = containerRedirectEnabled != null && Boolean.valueOf(containerRedirectEnabled.trim().toLowerCase());
        coder = new MD5();
        containerAliases_releaseIds = new TreeMap<String,Set<ReleaseId>>();
        containerConfigs_deploymentIds = new TreeMap<String,String>();
        deploymentIds_containerAliases = new TreeMap<String,String>();
        if (this.containerDeployment.length() > 0) {
            for (String unit :  this.containerDeployment.split("\\|")) {
                String[] split = unit.split("=");
                if (split.length == 2) {
                    String containerAlias = split[0].trim();
                    String gav = split[1].trim();
                    if (containerAlias.length() > 0 && gav.length() > 0) {
                        Set<ReleaseId> releaseIds = containerAliases_releaseIds.get(containerAlias);
                        if (releaseIds == null) {
                            // the descending sort means the first releaseId will be the default (latest) version
                            releaseIds = new TreeSet<ReleaseId>(new ReleaseIdComparator(DESCENDING));
                            containerAliases_releaseIds.put(containerAlias, releaseIds);
                        }
                        ReleaseId releaseId = new ComparableReleaseId(gav);
                        releaseIds.add(releaseId);
                    }
                }
            }
            for (Entry<String,Set<ReleaseId>> entry : containerAliases_releaseIds.entrySet()) {
                String containerAlias = entry.getKey();
                Set<ReleaseId> releaseIds = entry.getValue();
                if (!this.containerRedirectEnabled && releaseIds.size() > 1) {
                    // trim out all but default (latest) version per containerId
                    // when container redirect is not enabled
                    Iterator<ReleaseId> iter = releaseIds.iterator();
                    boolean first = true;
                    while (iter.hasNext()) {
                        iter.next();
                        if (first) {
                            first = false;
                        } else {
                            iter.remove();
                        }
                    }
                }
                for (ReleaseId releaseId : releaseIds) {
                    String containerConfig = createContainerConfig(containerAlias, releaseId);
                    String deploymentId = createDeploymentId(containerAlias, releaseId);
                    containerConfigs_deploymentIds.put(containerConfig, deploymentId);
                    deploymentIds_containerAliases.put(deploymentId, containerAlias);
                }
            }
        }
    }

    public boolean isContainerRedirectEnabled() {
        return containerRedirectEnabled;
    }

    public boolean hasDeploymentId(String deploymentId) {
        return deploymentId != null && containerConfigs_deploymentIds.containsValue(deploymentId);
    }

    public String getDeploymentIdForConfig(String containerConfig) {
        return containerConfig !=null ? containerConfigs_deploymentIds.get(containerConfig) : null;
    }

    public String getContainerAliasForDeploymentId(String deploymentId) {
        return deploymentId != null ? deploymentIds_containerAliases.get(deploymentId) : null;
    }

    public String getDefaultDeploymentIdForAlias(String containerAlias) {
        if (containerAlias != null) {
            Set<ReleaseId> releaseIds = containerAliases_releaseIds.get(containerAlias);
            if (releaseIds != null && releaseIds.size() > 0) {
                // the first releaseId will be the default (latest) version; see constructor above
                ReleaseId defaultReleaseId = releaseIds.iterator().next();
                return createDeploymentId(containerAlias, defaultReleaseId);
            }
        }
        return null;
    }

    private String createContainerConfig(String containerAlias, ReleaseId releaseId) {
        return containerAlias + "=" + releaseId.toExternalForm();
    }

    /*
     * this method needs to stay in sync with the bash logic in
     * jboss-dockerfiles/scripts/os-kieserver-launch/added/kieserver-config.sh
     * function getKieDeploymentId()
     */
    private String createDeploymentId(String containerAlias, ReleaseId releaseId) {
        if (containerRedirectEnabled) {
            return coder.encode(createContainerConfig(containerAlias, releaseId));
        } else {
            return containerAlias;
        }
    }

    public String toString() {
        return String.format("%s: serverStateFile=[%s], containerDeployment=[%s]",
                getClass().getSimpleName(),
                serverStateFile,
                containerDeployment);
    }

    public String toXml() {
        StringWriter writer = new StringWriter();
        XStream xs = new XStream();
        xs.alias("kie-server-state", KieServerState.class);
        xs.alias("container", KieContainerResource.class);
        xs.alias("config-item", KieServerConfigItem.class);
        xs.toXML(toState(), writer);
        return writer.toString();
    }

    public KieServerState toState() {
        KieServerState state = new KieServerState();
        KieServerConfig config = new KieServerConfig();
        String string = String.class.getName();
        config.addConfigItem(new KieServerConfigItem(KIE_SERVER_STATE_REPO, serverRepo, string));
        config.addConfigItem(new KieServerConfigItem(KIE_SERVER_ID, serverId, string));
        state.setConfiguration(config);
        Set<KieContainerResource> resources = new LinkedHashSet<KieContainerResource>();
        for (Entry<String,Set<ReleaseId>> entry : containerAliases_releaseIds.entrySet()) {
            String containerAlias = entry.getKey();
            Set<ReleaseId> releaseIds = entry.getValue();
            for (ReleaseId releaseId : releaseIds) {
                String deploymentId = createDeploymentId(containerAlias, releaseId);
                KieContainerResource container = new KieContainerResource(
                        deploymentId,
                        new org.kie.server.api.model.ReleaseId(releaseId),
                        STARTED);
                resources.add(container);
            }
        }
        state.setContainers(resources);
        return state;
    }

    public static ServerConfig getInstance() {
        return INSTANCE;
    }

    public static void main(String[] args) {
        boolean valid = main(INSTANCE, args, System.out, System.err);
        if (!valid) {
            System.exit(1);
        }
    }

    // package-protected for JUnit testing
    static final String USAGE = "Usage: java " + ServerConfig.class.getName() + " <env|xml>\n";
    static boolean main(ServerConfig serverConfig, String[] args, PrintStream out, PrintStream err) {
        boolean valid = false;
        if (args != null && args.length == 1) {
            String op = args[0].trim().toLowerCase();
            if ("env".equals(op)) {
                out.print(serverConfig.toString());
                valid = true;
            } else if ("xml".equals(op)) {
                out.print(serverConfig.toXml());
                valid = true;
            }
        }
        if (!valid) {
            err.print(USAGE);
        }
        return valid;
    }

}
