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

import org.kie.server.api.model.definition.AssociatedEntitiesDefinition;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.definition.ServiceTasksDefinition;
import org.kie.server.api.model.definition.SubProcessesDefinition;
import org.kie.server.api.model.definition.TaskInputsDefinition;
import org.kie.server.api.model.definition.TaskOutputsDefinition;
import org.kie.server.api.model.definition.UserTaskDefinitionList;
import org.kie.server.api.model.definition.VariablesDefinition;
import org.kie.server.services.jbpm.DefinitionServiceBase;
import org.openshift.kieserver.jms.redirect.RedirectIndex;

public class DefinitionService extends DefinitionServiceBase {

    private DefinitionService(org.jbpm.services.api.DefinitionService definitionService) {
        super(definitionService);
    }

    @Override
    @RedirectIndex(containerId=0)
    public ProcessDefinition getProcessDefinition(String containerId, String processId) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0)
    public SubProcessesDefinition getReusableSubProcesses(String containerId, String processId) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0)
    public VariablesDefinition getProcessVariables(String containerId, String processId) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0)
    public ServiceTasksDefinition getServiceTasks(String containerId, String processId) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0)
    public AssociatedEntitiesDefinition getAssociatedEntities(String containerId, String processId) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0)
    public UserTaskDefinitionList getTasksDefinitions(String containerId, String processId) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0)
    public TaskInputsDefinition getTaskInputMappings(String containerId, String processId, String taskName) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0)
    public TaskOutputsDefinition getTaskOutputMappings(String containerId, String processId, String taskName) {
        return null;
    }

}
