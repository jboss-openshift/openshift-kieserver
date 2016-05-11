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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
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
import org.kie.server.api.marshalling.ModelWrapper;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.Wrapped;
import org.kie.server.jms.JMSRuntimeException;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.impl.KieServerLocator;
import org.kie.server.services.jbpm.DefinitionServiceBase;
import org.kie.server.services.jbpm.ProcessServiceBase;
import org.kie.server.services.jbpm.UserTaskServiceBase;
import org.openshift.kieserver.common.id.ConversationId;
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
    private final Map<MarshallingFormat, Marshaller> marshallers;
    private final Map<String,Map<String,Integer>> services_methods_cidxs;

    public RedirectInterceptor() {
        serverConfig = ServerConfig.getInstance();
        marshallers = new ConcurrentHashMap<MarshallingFormat, Marshaller>();
        ClassLoader classLoader = CommandScript.class.getClassLoader();
        marshallers.put(XSTREAM, MarshallerFactory.getMarshaller(XSTREAM, classLoader));
        marshallers.put(JAXB, MarshallerFactory.getMarshaller(JAXB, classLoader));
        marshallers.put(JSON, MarshallerFactory.getMarshaller(JSON, classLoader));
        services_methods_cidxs = new HashMap<String,Map<String,Integer>>();
        for (Method method : DefinitionServiceBase.class.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                putServiceContainerIdIndexMethods("DefinitionService", 0, method.getName());
            }
        }
        for (Method method : ProcessServiceBase.class.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                putServiceContainerIdIndexMethods("ProcessService", 0, method.getName());
            }
        }
        for (Method method : UserTaskServiceBase.class.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                putServiceContainerIdIndexMethods("UserTaskService", 0, method.getName());
            }
        }
        putServiceContainerIdIndexMethods("QueryService", 0,
                "getProcessInstancesByDeploymentId",
                "getProcessesByDeploymentId",
                "getProcessesByDeploymentIdProcessId");
    }

    private void putServiceContainerIdIndexMethods(String service, Integer cidx, String... methods) {
        Map<String,Integer> methods_cidxs = services_methods_cidxs.get(service);
        if (methods_cidxs == null) {
            methods_cidxs = new HashMap<String,Integer>();
            services_methods_cidxs.put(service, methods_cidxs);
        }
        for (String method : methods) {
            methods_cidxs.put(method, cidx);
        }
    }

    private Integer getServiceMethodContainerIdIndex(String service, String method) {
        if (service != null && method != null) {
            Map<String,Integer> methods_cidxs = services_methods_cidxs.get(service);
            if (methods_cidxs != null) {
                return methods_cidxs.get(method);
            }
        }
        return null;
    }

    @AroundInvoke
    public Object doIntercept(InvocationContext ctx) throws Exception {
        if (ON_MESSAGE.equals(ctx.getMethod().getName())) {
            Message message = (Message)ctx.getParameters()[0];
            String requestedContainerId = getRequestedContainerId(message);
            if (!serverConfig.hasDeploymentId(requestedContainerId)) {
                String redirectContainerId = null;
                if (redirectContainerId == null) {
                    String conversationContainerId = getConversationContainerId(message);
                    if (serverConfig.hasDeploymentId(conversationContainerId)) {
                        redirectContainerId = conversationContainerId;
                    }
                }
                if (redirectContainerId == null) {
                    String defaultDeploymentId = serverConfig.getDefaultDeploymentId(requestedContainerId);
                    if (serverConfig.hasDeploymentId(defaultDeploymentId)) {
                        redirectContainerId = defaultDeploymentId;
                    }
                }
                if (redirectContainerId != null) {
                    if (LOGGER.isDebugEnabled()) {
                        String log = String.format("%s redirecting: %s -> %s", ON_MESSAGE, requestedContainerId, redirectContainerId);
                        LOGGER.debug(log);
                    }
                    // properties are read-only unless you clear them first
                    Map<String,Object> properties = new HashMap<String,Object>();
                    Enumeration<?> propNames = message.getPropertyNames();
                    while (propNames.hasMoreElements()) {
                        String propName = (String)propNames.nextElement();
                        properties.put(propName, message.getObjectProperty(propName));
                    }
                    properties.put(CONTAINER_ID_PROPERTY_NAME, redirectContainerId);
                    message.clearProperties();
                    for (Entry<String,Object> property : properties.entrySet()) {
                        message.setObjectProperty(property.getKey(), property.getValue());
                    }
                    boolean resetText = false;
                    String msgCorrId = getCorrelationId(message);
                    MarshallingFormat format = getMarshallingFormat(message, msgCorrId);
                    Marshaller marshaller = getMarshaller(redirectContainerId, format);
                    CommandScript script = unmarshallRequest(message, msgCorrId, marshaller);
                    for (KieServerCommand command : script.getCommands()) {
                        // not all commands are allowed in OpenShift KIE Server
                        if (command instanceof CallContainerCommand) {
                            ((CallContainerCommand)command).setContainerId(redirectContainerId);
                            resetText = true;
                        } else if (command instanceof DescriptorCommand) {
                            DescriptorCommand descriptorCommand = (DescriptorCommand)command;
                            Integer index = getServiceMethodContainerIdIndex(descriptorCommand.getService(), descriptorCommand.getMethod());
                            if (index != null) {
                                List<Object> arguments = descriptorCommand.getArguments();
                                if (arguments.size() > index) {
                                    Object arg = arguments.get(index);
                                    boolean wrap = false;
                                    if (arg instanceof Wrapped) {
                                        arg = ((Wrapped<?>)arg).unwrap();
                                        wrap = true;
                                    }
                                    if (requestedContainerId.equals(arg)) {
                                        arg = redirectContainerId;
                                        if (wrap) {
                                            arg = ModelWrapper.wrap(arg);
                                        }
                                        arguments.set(index, arg);
                                        resetText = true;
                                    }
                                }
                            }
                        } else if (command instanceof GetContainerInfoCommand) {
                            ((GetContainerInfoCommand)command).setContainerId(redirectContainerId);
                            resetText = true;
                        } else if (command instanceof GetScannerInfoCommand) {
                            ((GetScannerInfoCommand)command).setContainerId(redirectContainerId);
                            resetText = true;
                        }
                    }
                    if (resetText) {
                        String text = marshallRequest(script, msgCorrId, marshaller);
                        //System.out.println(text);
                        // body is read-only unless you clear it first
                        message.clearBody();
                        ((TextMessage)message).setText(text);
                    }
                }
            }
        }
        return ctx.proceed();
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

    private String getConversationContainerId(Message message) {
        try {
            if (message.propertyExists(CONVERSATION_ID_PROPERTY_NAME)) {
                String property = message.getStringProperty(CONVERSATION_ID_PROPERTY_NAME);
                ConversationId conversationId = ConversationId.fromString(property);
                return conversationId.getContainerId();
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

    protected Marshaller getMarshaller(String containerId, MarshallingFormat format) {
        if (containerId == null || containerId.isEmpty()) {
            return marshallers.get(format);
        }
        KieServerImpl kieServer = KieServerLocator.getInstance();
        KieContainerInstance kieContainerInstance = kieServer.getServerRegistry().getContainer(containerId);
        if (kieContainerInstance != null && kieContainerInstance.getKieContainer() != null) {
            return kieContainerInstance.getMarshaller(format);
        }
        return marshallers.get(format);
    }

    private static CommandScript unmarshallRequest(Message message, String msgId, Marshaller serializationProvider) {
        CommandScript cmdMsg = null;
        try {
            String msgStrContent = ((TextMessage)message).getText();
            cmdMsg = serializationProvider.unmarshall(msgStrContent, CommandScript.class);
        } catch (JMSException jmse) {
            String errMsg = "Unable to read information from message " + msgId + ".";
            throw new JMSRuntimeException(errMsg, jmse);
        } catch (Exception e) {
            String errMsg = "Unable to unmarshall request to " + CommandScript.class.getSimpleName() + " [msg id: " + msgId + "].";
            throw new JMSRuntimeException(errMsg, e);
        }
        return cmdMsg;
    }

    private static String marshallRequest(CommandScript cmdMsg, String msgId, Marshaller serializationProvider) {
        String msgStrContent = null;
        try {
            msgStrContent = serializationProvider.marshall(cmdMsg);
        } catch (Exception e) {
            String errMsg = "Unable to marshall request from " + CommandScript.class.getSimpleName() + " [msg id: " + msgId + "].";
            throw new JMSRuntimeException(errMsg, e);
        }
        return msgStrContent;
    }

}
