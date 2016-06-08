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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

import org.kie.server.api.commands.DescriptorCommand;
import org.openshift.kieserver.jms.redirect.services.DefinitionService;
import org.openshift.kieserver.jms.redirect.services.JobService;
import org.openshift.kieserver.jms.redirect.services.ProcessService;
import org.openshift.kieserver.jms.redirect.services.QueryService;
import org.openshift.kieserver.jms.redirect.services.UserTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ServiceHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceHelper.class);

    private static final Class<?>[] SERVICES = new Class<?>[] {
        DefinitionService.class,
        JobService.class,
        ProcessService.class,
        //QueryDataService.class, exists in 6.4.0 but not 6.3.0
        QueryService.class,
        UserTaskService.class
    };

    private final Map<String,Map<String,ServiceMethod>> serviceMap;

    public ServiceHelper() {
        serviceMap = new TreeMap<String,Map<String,ServiceMethod>>();
        for (Class<?> javaClass : SERVICES) {
            for (Method javaMethod : javaClass.getMethods()) {
                RedirectIndex redirectIndex = javaMethod.getAnnotation(RedirectIndex.class);
                if (redirectIndex != null) {
                    String service = javaClass.getSimpleName();
                    Map<String,ServiceMethod> methodMap = serviceMap.get(service);
                    if (methodMap == null) {
                        methodMap = new TreeMap<String,ServiceMethod>();
                        serviceMap.put(service, methodMap);
                    }
                    String method = javaMethod.getName();
                    ServiceMethod serviceMethod = new ServiceMethod(
                            service, method, redirectIndex.containerId(),
                            redirectIndex.processInstanceId(), redirectIndex.processInstanceIds(), redirectIndex.correlationKey(),
                            redirectIndex.taskInstanceId(), redirectIndex.workItemId());
                    if (methodMap.containsKey(method)) {
                        ServiceMethod existingMethod = methodMap.get(method);
                        if (!serviceMethod.equals(existingMethod)) {
                            Object[] warningArgs = new Object[]{existingMethod, serviceMethod};
                            LOGGER.warn(String.format("overriding service method %s with overloaded service method %s", warningArgs));
                        }
                    }
                    methodMap.put(method, serviceMethod);
                }
            }
        }
    }

    public ServiceMethod getServiceMethod(DescriptorCommand dc) {
        return dc != null ? getServiceMethod(dc.getService(), dc.getMethod()) : null;
    }

    public ServiceMethod getServiceMethod(String service, String method) {
        if (service != null && method != null) {
            Map<String,ServiceMethod> methodMap = serviceMap.get(service);
            if (methodMap != null) {
                return methodMap.get(method);
            }
        }
        return null;
    }

    public static void main(String... args) {
        ServiceHelper serviceHelper = new ServiceHelper();
        String className = serviceHelper.getClass().getSimpleName();
        for (Map.Entry<String,Map<String,ServiceMethod>> serviceEntry : serviceHelper.serviceMap.entrySet()) {
            String service = serviceEntry.getKey();
            Map<String,ServiceMethod> methodMap = serviceEntry.getValue();
            for (Map.Entry<String,ServiceMethod> methodEntry : methodMap.entrySet()) {
                String method = methodEntry.getKey();
                ServiceMethod sm = methodEntry.getValue();
                //System.out.println("\n" + sm);
                System.out.println(String.format("%s: %s.%s( %s )", className, service, method, sm.toArguments()));
            }
        }
    }

}
