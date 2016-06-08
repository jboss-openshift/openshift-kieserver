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

import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.RuntimeDataService;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.jbpm.ProcessServiceBase;
import org.openshift.kieserver.jms.redirect.RedirectIndex;

public class ProcessService extends ProcessServiceBase {

    private ProcessService(org.jbpm.services.api.ProcessService processService, DefinitionService definitionService, RuntimeDataService runtimeDataService, KieServerRegistry context) {
        super(processService, definitionService, runtimeDataService, context);
    }

    @Override
    @RedirectIndex(containerId=0)
    public String startProcess(String containerId, String processId, String marshallingType) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0)
    public String startProcess(String containerId, String processId, String payload, String marshallingType) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0, correlationKey=2)
    public String startProcessWithCorrelation(String containerId, String processId, String correlationKey, String payload, String marshallingType) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0, processInstanceId=1)
    public Object abortProcessInstance(String containerId, Number processInstanceId) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0, processInstanceIds=1)
    public Object abortProcessInstances(String containerId, List<Long> processInstanceIds) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0, processInstanceId=1)
    public void signalProcessInstance(String containerId, Number processInstanceId, String signalName, String marshallingType) {
    }

    @Override
    @RedirectIndex(containerId=0, processInstanceId=1)
    public void signalProcessInstance(String containerId, Number processInstanceId, String signalName, String eventPayload, String marshallingType) {
    }

    @Override
    @RedirectIndex(containerId=0, processInstanceIds=1)
    public void signalProcessInstances(String containerId, List<Long> processInstanceIds, String signalName, String marshallingType) {
    }

    @Override
    @RedirectIndex(containerId=0, processInstanceIds=1)
    public void signalProcessInstances(String containerId, List<Long> processInstanceIds, String signalName, String eventPayload, String marshallingType) {
    }

    //@Override exists in 6.4.0 but not 6.3.0
    @RedirectIndex(containerId=0)
    public void signal(String containerId, String signalName, String marshallingType) {
    }

    //@Override exists in 6.4.0 but not 6.3.0
    @RedirectIndex(containerId=0)
    public void signal(String containerId, String signalName, String eventPayload, String marshallingType) {
    }

    @Override
    @RedirectIndex(containerId=0, processInstanceId=1)
    public String getProcessInstance(String containerId, Number processInstanceId, boolean withVars, String marshallingType) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0, processInstanceId=1)
    public void setProcessVariable(String containerId, Number processInstanceId, String varName, String variablePayload, String marshallingType) {
    }

    @Override
    @RedirectIndex(containerId=0, processInstanceId=1)
    public void setProcessVariables(String containerId, Number processInstanceId, String variablePayload, String marshallingType) {
    }

    @Override
    @RedirectIndex(containerId=0, processInstanceId=1)
    public String getProcessInstanceVariable(String containerId, Number processInstanceId, String varName, String marshallingType) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0, processInstanceId=1)
    public String getProcessInstanceVariables(String containerId, Number processInstanceId, String marshallingType) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0, processInstanceId=1)
    public String getAvailableSignals(String containerId, Number processInstanceId, String marshallingType) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0, processInstanceId=1, workItemId=2)
    public void completeWorkItem(String containerId, Number processInstanceId, Number workItemId, String resultPayload, String marshallingType) {
    }

    @Override
    @RedirectIndex(containerId=0, processInstanceId=1, workItemId=2)
    public void abortWorkItem(String containerId, Number processInstanceId, Number workItemId) {
    }

    @Override
    @RedirectIndex(containerId=0, processInstanceId=1, workItemId=2)
    public String getWorkItem(String containerId, Number processInstanceId, Number workItemId, String marshallingType) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0, processInstanceId=1)
    public String getWorkItemByProcessInstance(String containerId, Number processInstanceId, String marshallingType) {
        return null;
    }

}
