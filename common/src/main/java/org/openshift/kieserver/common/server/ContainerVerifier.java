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

import java.io.PrintStream;

import org.kie.api.KieServices;
import org.kie.api.builder.Message;
import org.kie.api.builder.Message.Level;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.Results;
import org.kie.api.runtime.KieContainer;
import org.openshift.kieserver.common.id.ComparableReleaseId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContainerVerifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerVerifier.class);

    public boolean verify(String gav) {
        boolean verified;
        try {
            ReleaseId releaseId = new ComparableReleaseId(gav);
            verified = verify(releaseId);
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
            verified = false;
        }
        return verified;
    }

    public boolean verify(String groupId, String artifactId, String version) {
        boolean verified;
        try {
            ReleaseId releaseId = new ComparableReleaseId(groupId, artifactId, version);
            verified = verify(releaseId);
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
            verified = false;
        }
        return verified;
    }

    public boolean verify(ReleaseId releaseId) {
        boolean verified;
        KieServices services = KieServices.Factory.get();
        try {
            KieContainer container = services.newKieContainer(releaseId);
            verified = verify(container);
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
            verified = false;
        }
        return verified;
    }

    public boolean verify(KieContainer container) {
        boolean verified = true;
        try {
            Results results = container.verify();
            for (Message message : results.getMessages()) {
                Level level = message.getLevel();
                switch (level) {
                    case INFO:
                        LOGGER.info(message.toString());
                        break;
                    case WARNING:
                        LOGGER.warn(message.toString());
                        break;
                    case ERROR:
                        LOGGER.error(message.toString());
                        verified = false;
                        break;
                }
            }
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
            verified = false;
        }
        return verified;
    }

    public static void main(String[] args) {
        boolean verified = main(args, System.out, System.err);
        if (!verified) {
            System.exit(1);
        }
    }

    // package-protected for JUnit testing
    static final String USAGE = "Usage: java " + ContainerVerifier.class.getName() + " <gav1> <gav2> ...\n";
    static boolean main(String[] args, PrintStream out, PrintStream err) {
        boolean triggered = false;
        boolean verified = true;
        if (args != null && args.length > 0) {
            ContainerVerifier verifier = new ContainerVerifier();
            for (String arg : args) {
                if (arg != null) {
                    arg = arg.trim();
                    if (!arg.isEmpty()) {
                        triggered = true;
                        if (verifier.verify(arg)) {
                            LOGGER.info(arg + " verified.");
                        } else {
                            LOGGER.error(arg + " not verified.");
                            verified = false;
                        }
                    }
                }
            }
        }
        if (!triggered) {
            err.print(USAGE);
        }
        return verified;
    }

}
