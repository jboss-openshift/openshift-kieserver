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
package org.openshift.kieserver.jms.redirect;

import static org.kie.server.api.jms.JMSConstants.CONTAINER_ID_PROPERTY_NAME;
import static org.kie.server.api.jms.JMSConstants.SERIALIZATION_FORMAT_PROPERTY_NAME;
import static org.kie.server.api.marshalling.MarshallingFormat.JAXB;
import static org.kie.server.api.marshalling.MarshallingFormat.JSON;
import static org.kie.server.api.marshalling.MarshallingFormat.XSTREAM;

import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.kie.server.api.commands.CallContainerCommand;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.DescriptorCommand;
import org.kie.server.api.commands.GetContainerInfoCommand;
import org.kie.server.api.commands.GetScannerInfoCommand;
import org.kie.server.api.jms.JMSConstants;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.jms.JMSRuntimeException;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.impl.KieServerLocator;
import org.openshift.kieserver.common.id.ConversationId;
import org.openshift.kieserver.common.server.DeploymentHelper;
import org.openshift.kieserver.common.server.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedirectInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedirectInterceptor.class);

    private static final String ON_MESSAGE = "onMessage";
    private static final String ID_NECESSARY = "This id is needed to be able to match a request to a response message.";

    // org.kie.server.api.jms.JMSConstants.CONVERSATION_ID_PROPERTY_NAME exists in 6.4+
    private static final String CONVERSATION_ID_PROPERTY_NAME;
    static {
        String conversationIdPropertyName = null;
        if (ConversationId.isSupported()) {
            try {
                Field field = JMSConstants.class.getDeclaredField("CONVERSATION_ID_PROPERTY_NAME");
                field.setAccessible(true);
                conversationIdPropertyName = (String)field.get(null);
            } catch (Throwable t) {
                LOGGER.warn(t.getMessage());
            }
        }
        if (conversationIdPropertyName == null) {
            conversationIdPropertyName = "kie_conversation_id";
        }
        CONVERSATION_ID_PROPERTY_NAME = conversationIdPropertyName;
    }

    private final ServerConfig serverConfig;
    private final boolean containerRedirectEnabled;
    private final Map<MarshallingFormat, Marshaller> marshallers;
    private final ServiceHelper serviceHelper;
    private final DeploymentHelper deploymentHelper;

    public RedirectInterceptor() {
        serverConfig = ServerConfig.getInstance();
        containerRedirectEnabled = serverConfig.isContainerRedirectEnabled();
        marshallers = new ConcurrentHashMap<MarshallingFormat, Marshaller>();
        serviceHelper = new ServiceHelper();
        deploymentHelper = new DeploymentHelper();
        if (containerRedirectEnabled) {
            ClassLoader classLoader = CommandScript.class.getClassLoader();
            marshallers.put(XSTREAM, MarshallerFactory.getMarshaller(XSTREAM, classLoader));
            marshallers.put(JAXB, MarshallerFactory.getMarshaller(JAXB, classLoader));
            marshallers.put(JSON, MarshallerFactory.getMarshaller(JSON, classLoader));
        }
    }

    @AroundInvoke
    public Object doIntercept(InvocationContext ctx) throws Exception {
        if (containerRedirectEnabled && ON_MESSAGE.equals(ctx.getMethod().getName())) {
            Message message = (Message)ctx.getParameters()[0];
            // this is often null for JMS
            String requestedContainerId = getRequestedContainerId(message);
            // only if the id is not an actual deployment, do we try to redirect
            if (!serverConfig.hasDeploymentId(requestedContainerId)) {
                String msgCorrId = getCorrelationId(message);
                MarshallingFormat format = getMarshallingFormat(message, msgCorrId);
                String configDeploymentId = serverConfig.getDeploymentIdForConfig(requestedContainerId);
                String redirectDeploymentId = null;
                if (serverConfig.hasDeploymentId(configDeploymentId)) {
                    redirectDeploymentId = configDeploymentId;
                } else {
                    String conversationDeploymentId = getDeploymentIdByConversationId(message);
                    String containerAlias = serverConfig.getContainerAliasForDeploymentId(conversationDeploymentId);
                    if (requestedContainerId != null && !requestedContainerId.equals(containerAlias)) {
                        conversationDeploymentId = null;
                    }
                    String defaultDeploymentId = serverConfig.getDefaultDeploymentIdForAlias(requestedContainerId);
                    Marshaller marshaller = getMarshaller(format, conversationDeploymentId, defaultDeploymentId);
                    String commandDeploymentId = getCommandDeploymentId(message, msgCorrId, marshaller);
                    if (serverConfig.hasDeploymentId(commandDeploymentId)) {
                        redirectDeploymentId = commandDeploymentId;
                    } else if (serverConfig.hasDeploymentId(conversationDeploymentId)) {
                        redirectDeploymentId = conversationDeploymentId;
                    } else if (serverConfig.hasDeploymentId(defaultDeploymentId)) {
                        redirectDeploymentId = defaultDeploymentId;
                    }
                }
                if (redirectDeploymentId != null) {
                    if (LOGGER.isDebugEnabled()) {
                        String log = String.format("%s redirecting to %s", ON_MESSAGE, redirectDeploymentId);
                        LOGGER.debug(log);
                    }
                    // properties are read-only unless you clear them first
                    Map<String,Object> properties = new HashMap<String,Object>();
                    Enumeration<?> propNames = message.getPropertyNames();
                    while (propNames.hasMoreElements()) {
                        String propName = (String)propNames.nextElement();
                        properties.put(propName, message.getObjectProperty(propName));
                    }
                    properties.put(CONTAINER_ID_PROPERTY_NAME, redirectDeploymentId);
                    message.clearProperties();
                    for (Entry<String,Object> property : properties.entrySet()) {
                        message.setObjectProperty(property.getKey(), property.getValue());
                    }
                    boolean resetText = false;
                    Marshaller marshaller = getMarshaller(format, redirectDeploymentId);
                    CommandScript script = unmarshallRequest(message, msgCorrId, marshaller);
                    for (KieServerCommand command : script.getCommands()) {
                        // not all commands are allowed in OpenShift KIE Server
                        if (command instanceof CallContainerCommand) {
                            ((CallContainerCommand)command).setContainerId(redirectDeploymentId);
                            resetText = true;
                        } else if (command instanceof DescriptorCommand) {
                            DescriptorCommand dc = (DescriptorCommand)command;
                            ServiceMethod sm = serviceHelper.getServiceMethod(dc);
                            if (sm != null && sm.setContainerId(dc, redirectDeploymentId)) {
                                resetText = true;
                            }
                        } else if (command instanceof GetContainerInfoCommand) {
                            ((GetContainerInfoCommand)command).setContainerId(redirectDeploymentId);
                            resetText = true;
                        } else if (command instanceof GetScannerInfoCommand) {
                            ((GetScannerInfoCommand)command).setContainerId(redirectDeploymentId);
                            resetText = true;
                        }
                    }
                    if (resetText) {
                        String text = marshallRequest(script, msgCorrId, marshaller);
                        // body is read-only unless you clear it first
                        message.clearBody();
                        ((TextMessage)message).setText(text);
                    }
                }
            }
        }
        return ctx.proceed();
    }

    private String getCommandDeploymentId(Message message, String msgCorrId, Marshaller marshaller) {
        boolean found = false;
        String commandDeploymentId = null;
        CommandScript script = unmarshallRequest(message, msgCorrId, marshaller);
        for (KieServerCommand command : script.getCommands()) {
            if (command instanceof DescriptorCommand) {
                DescriptorCommand dc = (DescriptorCommand)command;
                ServiceMethod sm = serviceHelper.getServiceMethod(dc);
                if (sm == null) {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn(String.format("cannot find ServiceMethod match for DescriptorCommand: service=%s, method=%s", dc.getService(), dc.getMethod()));
                    }
                    continue;
                }
                commandDeploymentId = deploymentHelper.getDeploymentIdByProcessInstanceId(sm.getProcessInstanceId(dc));
                if (serverConfig.hasDeploymentId(commandDeploymentId)) {
                    found = true;
                    break;
                }
                commandDeploymentId = deploymentHelper.getDeploymentIdByProcessInstanceIds(sm.getProcessInstanceIds(dc));
                if (serverConfig.hasDeploymentId(commandDeploymentId)) {
                    found = true;
                    break;
                }
                commandDeploymentId = deploymentHelper.getDeploymentIdByCorrelationKey(sm.getCorrelationKey(dc));
                if (serverConfig.hasDeploymentId(commandDeploymentId)) {
                    found = true;
                    break;
                }
                commandDeploymentId = deploymentHelper.getDeploymentIdByTaskInstanceId(sm.getTaskInstanceId(dc));
                if (serverConfig.hasDeploymentId(commandDeploymentId)) {
                    found = true;
                    break;
                }
                commandDeploymentId = deploymentHelper.getDeploymentIdByWorkItemId(sm.getWorkItemId(dc));
                if (serverConfig.hasDeploymentId(commandDeploymentId)) {
                    found = true;
                    break;
                }
                commandDeploymentId = deploymentHelper.getDeploymentIdByJobId(sm.getJobId(dc));
                if (serverConfig.hasDeploymentId(commandDeploymentId)) {
                    found = true;
                    break;
                }
                final String requestedContainerId = sm.getContainerId(dc);
                commandDeploymentId = requestedContainerId;
                if (serverConfig.hasDeploymentId(commandDeploymentId)) {
                    found = true;
                    break;
                }
                commandDeploymentId = serverConfig.getDeploymentIdForConfig(requestedContainerId);
                if (serverConfig.hasDeploymentId(commandDeploymentId)) {
                    found = true;
                    break;
                }
                commandDeploymentId = serverConfig.getDefaultDeploymentIdForAlias(requestedContainerId);
                if (serverConfig.hasDeploymentId(commandDeploymentId)) {
                    found = true;
                    break;
                }
            }
        }
        return found ? commandDeploymentId : null;
    }

    private String getRequestedContainerId(Message message) {
        String containerId = null;
        try {
            if (message.propertyExists(CONTAINER_ID_PROPERTY_NAME)) {
                containerId = message.getStringProperty(CONTAINER_ID_PROPERTY_NAME);
            }
        } catch (JMSException jmse) {
            // no-op
        }
        return containerId;
    }

    private String getDeploymentIdByConversationId(Message message) {
        try {
            if (message.propertyExists(CONVERSATION_ID_PROPERTY_NAME)) {
                String conversationId = message.getStringProperty(CONVERSATION_ID_PROPERTY_NAME);
                return deploymentHelper.getDeploymentIdByConversationId(conversationId);
            }
        } catch (JMSException jmse) {
            // no-op
        }
        return null;
    }

    private String getCorrelationId(Message message) {
        String msgCorrId = null;
        try {
            msgCorrId = message.getJMSCorrelationID();
        } catch (JMSException jmse) {
            String errMsg = "Unable to retrieve JMS correlation id from message! " + ID_NECESSARY;
            throw new JMSRuntimeException(errMsg, jmse);
        }
        return msgCorrId;
    }

    private MarshallingFormat getMarshallingFormat(Message message, String msgCorrId) {
        MarshallingFormat format = null;
        try {
            if (!message.propertyExists(SERIALIZATION_FORMAT_PROPERTY_NAME)) {
                format = MarshallingFormat.JAXB;
            } else {
                int intFormat = message.getIntProperty(SERIALIZATION_FORMAT_PROPERTY_NAME);
                format = MarshallingFormat.fromId(intFormat);
                if (format == null) {
                    String errMsg = "Unsupported marshalling format '" + intFormat + "' from message " + msgCorrId + ".";
                    throw new JMSRuntimeException(errMsg);
                }
            }
        } catch (JMSException jmse) {
            String errMsg = "Unable to retrieve property '" + SERIALIZATION_FORMAT_PROPERTY_NAME + "' from message " + msgCorrId + ".";
            throw new JMSRuntimeException(errMsg, jmse);
        }
        return format;
    }

    private Marshaller getMarshaller(MarshallingFormat format, String... deploymentIds) {
        for (String deploymentId : deploymentIds) {
            if (deploymentId != null && !deploymentId.isEmpty() && serverConfig.hasDeploymentId(deploymentId)) {
                KieServerImpl kieServer = KieServerLocator.getInstance();
                KieContainerInstance kieContainerInstance = kieServer.getServerRegistry().getContainer(deploymentId);
                if (kieContainerInstance != null && kieContainerInstance.getKieContainer() != null) {
                    return kieContainerInstance.getMarshaller(format);
                }
            }
        }
        return marshallers.get(format);
    }

    private CommandScript unmarshallRequest(Message message, String msgCorrId, Marshaller marshaller) {
        CommandScript cmdMsg = null;
        try {
            String msgStrContent = ((TextMessage)message).getText();
            cmdMsg = marshaller.unmarshall(msgStrContent, CommandScript.class);
        } catch (JMSException jmse) {
            String errMsg = "Unable to read information from message " + msgCorrId + ".";
            throw new JMSRuntimeException(errMsg, jmse);
        } catch (Exception e) {
            String errMsg = "Unable to unmarshall request to " + CommandScript.class.getSimpleName() + " [msg id: " + msgCorrId + "].";
            throw new JMSRuntimeException(errMsg, e);
        }
        return cmdMsg;
    }

    private String marshallRequest(CommandScript cmdMsg, String msgId, Marshaller marshaller) {
        String msgStrContent = null;
        try {
            msgStrContent = marshaller.marshall(cmdMsg);
        } catch (Exception e) {
            String errMsg = "Unable to marshall request from " + CommandScript.class.getSimpleName() + " [msg id: " + msgId + "].";
            throw new JMSRuntimeException(errMsg, e);
        }
        return msgStrContent;
    }

}
