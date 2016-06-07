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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.junit.Test;
import org.openshift.kieserver.common.coder.Coder;
import org.openshift.kieserver.common.coder.SumCoder.MD5;

public class ServerConfigTest {

    private static final String KIE_SERVER_REPO = "repo";
    private static final String KIE_SERVER_ID = "id";
    private static final String KIE_SERVER_STATE_FILE = new File(KIE_SERVER_REPO + File.separator + KIE_SERVER_ID + ".xml").getPath();
    private static final String KIE_CONTAINER_DEPLOYMENT = "c1=g1:a1:v1|c2=g2:a2:v2|c2=g2:a2:v2.1";
    private static final Coder CODER = new MD5();
    private static final String DEPLOYMENT_ID_C2_V21 = CODER.encode("c2=g2:a2:v2.1");
    private static final String DEPLOYMENT_ID_C2_V2 = CODER.encode("c2=g2:a2:v2");

    private ServerConfig newServerConfig(boolean containerRedirectEnabled) {
        return new ServerConfig(
                KIE_SERVER_REPO,
                KIE_SERVER_ID,
                KIE_SERVER_STATE_FILE,
                KIE_CONTAINER_DEPLOYMENT,
                String.valueOf(containerRedirectEnabled));
    }

    @Test
    public void testDefaultDeploymentId() {
        ServerConfig serverConfig = newServerConfig(true);
        assertEquals(DEPLOYMENT_ID_C2_V21, serverConfig.getDefaultDeploymentIdForAlias("c2"));
        serverConfig = newServerConfig(false);
        assertEquals("c2", serverConfig.getDefaultDeploymentIdForAlias("c2"));
    }

    @Test
    public void testEmptyDeployment() {
        ServerConfig serverConfig = ServerConfig.getInstance();
        assertNull(serverConfig.getDefaultDeploymentIdForAlias(""));
    }

    @Test
    public void testMainEnv() throws Exception {
        ServerConfig serverConfig = newServerConfig(true);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean valid = ServerConfig.main(serverConfig, new String[]{"env"}, new PrintStream(out, true), null);
        assertTrue(valid);
        String env = new String(out.toByteArray(), "UTF-8");
        assertEquals("ServerConfig: serverStateFile=[" + KIE_SERVER_STATE_FILE + "], containerDeployment=[" + KIE_CONTAINER_DEPLOYMENT + "]", env);
    }

    @Test
    public void testMainXml() throws Exception {
        ServerConfig serverConfig = newServerConfig(true);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean valid = ServerConfig.main(serverConfig, new String[]{"xml"}, new PrintStream(out, true), null);
        assertTrue(valid);
        String xml = new String(out.toByteArray(), "UTF-8");
        assertTrue(xml.contains("<containerId>" + DEPLOYMENT_ID_C2_V21 + "</containerId>"));
        assertTrue(xml.contains("<containerId>" + DEPLOYMENT_ID_C2_V2 + "</containerId>"));
        assertFalse(xml.contains("<containerId>c2</containerId>"));
        serverConfig = newServerConfig(false);
        out = new ByteArrayOutputStream();
        valid = ServerConfig.main(serverConfig, new String[]{"xml"}, new PrintStream(out, true), null);
        assertTrue(valid);
        xml = new String(out.toByteArray(), "UTF-8");
        assertTrue(xml.contains("<containerId>c2</containerId>"));
        assertFalse(xml.contains("<containerId>" + DEPLOYMENT_ID_C2_V21 + "</containerId>"));
        assertFalse(xml.contains("<containerId>" + DEPLOYMENT_ID_C2_V2 + "</containerId>"));
    }

    @Test
    public void testMainInvalid() throws Exception {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        boolean valid = ServerConfig.main(null, new String[]{"invalid"}, null, new PrintStream(err, true));
        assertFalse(valid);
        assertEquals(ServerConfig.USAGE, new String(err.toByteArray(), "UTF-8"));
    }

}
