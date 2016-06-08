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

import org.jbpm.services.api.RuntimeDataService;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.definition.ProcessDefinitionList;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.api.model.instance.NodeInstanceList;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.ProcessInstanceList;
import org.kie.server.api.model.instance.TaskEventInstanceList;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummaryList;
import org.kie.server.api.model.instance.VariableInstanceList;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.jbpm.RuntimeDataServiceBase;
import org.openshift.kieserver.jms.redirect.RedirectIndex;

public class QueryService extends RuntimeDataServiceBase {

    private QueryService(RuntimeDataService delegate, KieServerRegistry context) {
        super(delegate, context);
    }

    @Override
    public ProcessInstanceList getProcessInstances(List<Integer> status, String initiator, String processName, Integer page, Integer pageSize) {
        return null;
    }

    @Override
    public ProcessInstanceList getProcessInstancesByProcessId(String processId, List<Integer> status, String initiator, Integer page, Integer pageSize) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0)
    public ProcessInstanceList getProcessInstancesByDeploymentId(String containerId, List<Integer> status, Integer page, Integer pageSize) {
        return null;
    }

    @Override
    @RedirectIndex(correlationKey=0)
    public ProcessInstanceList getProcessInstancesByCorrelationKey(String correlationKey, Integer page, Integer pageSize) {
        return null;
    }

    @Override
    @RedirectIndex(correlationKey=0)
    public ProcessInstance getProcessInstanceByCorrelationKey(String correlationKey) {
        return null;
    }

    @Override
    public ProcessInstanceList getProcessInstanceByVariables(String variableName, String variableValue, List<Integer> status, Integer page, Integer pageSize) {
        return null;
    }

    @Override
    @RedirectIndex(processInstanceId=0)
    public ProcessInstance getProcessInstanceById(long processInstanceId) {
        return null;
    }

    //@Override exists in 6.4.0 but not 6.3.0
    @RedirectIndex(processInstanceId=0)
    public ProcessInstance getProcessInstanceById(long processInstanceId, boolean withVars) {
        return null;
    }

    @Override
    @RedirectIndex(processInstanceId=0, workItemId=1)
    public NodeInstance getNodeInstanceForWorkItem(long processInstanceId, long workItemId) {
        return null;
    }

    @Override
    @RedirectIndex(processInstanceId=0)
    public NodeInstanceList getProcessInstanceHistory(long processInstanceId, Boolean active, Boolean completed, Integer page, Integer pageSize) {
        return null;
    }

    @Override
    @RedirectIndex(processInstanceId=0)
    public VariableInstanceList getVariablesCurrentState(long processInstanceId) {
        return null;
    }

    @Override
    @RedirectIndex(processInstanceId=0)
    public VariableInstanceList getVariableHistory(long processInstanceId, String variableName, Integer page, Integer pageSize) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0)
    public ProcessDefinitionList getProcessesByDeploymentId(String containerId, Integer page, Integer pageSize) {
        return null;
    }

    @Override
    public ProcessDefinitionList getProcessesByFilter(String filter, Integer page, Integer pageSize) {
        return null;
    }

    @Override
    public ProcessDefinitionList getProcessesById(String processId) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0)
    public ProcessDefinition getProcessesByDeploymentIdProcessId(String containerId, String processId) {
        return null;
    }

    @Override
    @RedirectIndex(workItemId=0)
    public TaskInstance getTaskByWorkItemId(long workItemId) {
        return null;
    }

    @Override
    @RedirectIndex(taskInstanceId=0)
    public TaskInstance getTaskById(long taskId) {
        return null;
    }

    @Override
    public TaskSummaryList getTasksAssignedAsBusinessAdministratorByStatus(List<String> status, String userId, Integer page, Integer pageSize) {
        return null;
    }

    @Override
    public TaskSummaryList getTasksAssignedAsPotentialOwner(List<String> status, List<String> groupIds, String userId, Integer page, Integer pageSize) {
        return null;
    }

    @Override
    public TaskSummaryList getTasksOwnedByStatus(List<String> status, String userId, Integer page, Integer pageSize) {
        return null;
    }

    @Override
    @RedirectIndex(processInstanceId=0)
    public TaskSummaryList getTasksByStatusByProcessInstanceId(Number processInstanceId, List<String> status, Integer page, Integer pageSize) {
        return null;
    }

    @Override
    public TaskSummaryList getAllAuditTask(String userId, Integer page, Integer pageSize) {
        return null;
    }

    @Override
    @RedirectIndex(taskInstanceId=0)
    public TaskEventInstanceList getTaskEvents(long taskId, Integer page, Integer pageSize) {
        return null;
    }

    //@Override exists in 6.4.0 but not 6.3.0
    public TaskSummaryList getTasksByVariables(String userId, String variableName, String variableValue, List<String> status, Integer page, Integer pageSize) {
        return null;
    }

}
