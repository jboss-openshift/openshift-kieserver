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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.kie.server.api.KieServerConstants;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// org.kie.server.api.ConversationId exists in 6.4+
public final class ConversationId {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConversationId.class);

    // org.kie.server.api.KieServerConstants.KIE_CONVERSATION_ID_TYPE_HEADER exists in 6.4+
    public static final String KIE_CONVERSATION_ID_TYPE_HEADER;

    private static final Method FROM_STRING;
    private static final Method GET_CONTAINER_ID;

    static {
        String kieConversationIdTypeHeader = null;
        Method fromString = null;
        Method getContainerId = null;
        if (isSupported()) {
            try {
                Field field = KieServerConstants.class.getDeclaredField("KIE_CONVERSATION_ID_TYPE_HEADER");
                field.setAccessible(true);
                kieConversationIdTypeHeader = (String)field.get(null);
                Class<?> clazz = Class.forName("org.kie.server.api.ConversationId");
                fromString = clazz.getDeclaredMethod("fromString", String.class);
                fromString.setAccessible(true);
                getContainerId = clazz.getDeclaredMethod("getContainerId");
                getContainerId.setAccessible(true);
            } catch (Throwable t) {
                LOGGER.warn(t.getMessage());
            }
        }
        if (kieConversationIdTypeHeader == null) {
            kieConversationIdTypeHeader = "X-KIE-ConversationId";
        }
        KIE_CONVERSATION_ID_TYPE_HEADER = kieConversationIdTypeHeader;
        FROM_STRING = fromString;
        GET_CONTAINER_ID = getContainerId;
    }

    public static boolean isSupported() {
        Version version = KieServerEnvironment.getVersion();
        return version.getMajor() >= 6 && version.getMinor() >= 4;
    }

    private final Object conversationId;

    private ConversationId(Object conversationId) {
        this.conversationId = conversationId;
    }

    public String getContainerId() {
        if (GET_CONTAINER_ID != null) {
            try {
                return (String)GET_CONTAINER_ID.invoke(conversationId);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static ConversationId fromString(String conversationIdString) {
        if (FROM_STRING != null) {
            try {
                Object conversationId = FROM_STRING.invoke(null, conversationIdString);
                return conversationId != null ? new ConversationId(conversationId) : null;
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
