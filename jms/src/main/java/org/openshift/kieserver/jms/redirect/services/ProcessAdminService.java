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

import org.jbpm.services.api.admin.ProcessInstanceMigrationService;
import org.kie.server.api.model.admin.MigrationReportInstance;
import org.kie.server.api.model.admin.MigrationReportInstanceList;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.jbpm.admin.ProcessAdminServiceBase;
import org.openshift.kieserver.jms.redirect.RedirectIndex;

public class ProcessAdminService extends ProcessAdminServiceBase {

    private ProcessAdminService(ProcessInstanceMigrationService processInstanceMigrationService, KieServerRegistry context) {
        super(processInstanceMigrationService, context);
    }

    @Override
    @RedirectIndex(containerId=0, processInstanceId=1)
    public MigrationReportInstance migrateProcessInstance(String containerId, Number processInstanceId, String targetContainerId, String targetProcessId, String payload, String marshallingType) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0, processInstanceIds=1)
    public MigrationReportInstanceList migrateProcessInstances(String containerId, List<Long> processInstancesId, String targetContainerId, String targetProcessId, String payload, String marshallingType) {
        return null;
    }

}
