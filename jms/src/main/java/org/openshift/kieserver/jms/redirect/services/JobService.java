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
package org.openshift.kieserver.jms.redirect.services;

import java.util.List;

import org.kie.api.executor.ExecutorService;
import org.kie.server.api.model.instance.RequestInfoInstanceList;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.jbpm.ExecutorServiceBase;
import org.openshift.kieserver.jms.redirect.RedirectIndex;

public class JobService extends ExecutorServiceBase {

    private JobService(ExecutorService executorService, KieServerRegistry context) {
        super(executorService, context);
    }

    @Override
    @RedirectIndex(containerId=0)
    public String scheduleRequest(String containerId, String payload, String marshallingType) {
        return null;
    }

    @Override
    public void cancelRequest(long requestId) {
    }

    @Override
    public void requeueRequest(long requestId) {
    }

    @Override
    public RequestInfoInstanceList getRequestsByStatus(List<String> statuses, Integer page, Integer pageSize) {
        return null;
    }

    @Override
    public RequestInfoInstanceList getRequestsByBusinessKey(String businessKey, Integer page, Integer pageSize) {
        return null;
    }

    @Override
    public RequestInfoInstanceList getRequestsByCommand(String command, Integer page, Integer pageSize) {
        return null;
    }

    @Override
    public String getRequestById(long requestId, boolean withErrors, boolean withData, String marshallingType) {
        return null;
    }

}
