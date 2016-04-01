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
package org.openshift.kieserver.common.id;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ConversationIdTest {

    private static final String HEADER = "%27myServerId%27%3A%27myContainerId%27%3A%27org.openshift%3Atest%3A1.3%27%3A%27d5bcfc1e-bf2c-4813-bff3-a97c0a2549db%27";

    @Test
    public void testGetContainerId() {
        ConversationId conversationId = ConversationId.fromString(HEADER);
        if (ConversationId.isSupported()) {
            assertNotNull(conversationId);
            assertEquals("myContainerId", conversationId.getContainerId());
        } else {
            assertNull(conversationId);
        }
    }

}
