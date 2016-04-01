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

import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieServerLocator;

public final class ServerUtil {

    // org.kie.server.api.KieServerConstants.CAPABILITY_BPM exists in 6.4+
    public static final String CAPABILITY_BPM = "BPM";

    private ServerUtil() {}

    public static <T> T getAppComponentService(String implementedCapability, Class<T> serviceType) {
        KieServerRegistry registry = KieServerLocator.getInstance().getServerRegistry();
        for (KieServerExtension extension : registry.getServerExtensions()) {
            if (extension.getImplementedCapability().equals(implementedCapability)) {
                T service = extension.getAppComponents(serviceType);
                if (service != null) {
                    return service;
                }
            }
        }
        return null;
    }

}
