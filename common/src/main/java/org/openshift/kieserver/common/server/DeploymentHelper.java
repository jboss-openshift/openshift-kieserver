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

import static org.openshift.kieserver.common.server.ServerUtil.CAPABILITY_BPM;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.services.api.model.UserTaskInstanceDesc;
import org.kie.api.executor.ExecutorService;
import org.kie.api.executor.RequestInfo;
import org.kie.internal.KieInternalServices;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.process.CorrelationKeyFactory;
import org.openshift.kieserver.common.id.ConversationId;

public class DeploymentHelper {

    private final RuntimeDataService runtimeDataService;
    private final ExecutorService executorService;
    private final CorrelationKeyFactory correlationKeyFactory;

    public DeploymentHelper() {
        runtimeDataService = ServerUtil.getAppComponentService(CAPABILITY_BPM, RuntimeDataService.class);
        executorService = ServerUtil.getAppComponentService(CAPABILITY_BPM, ExecutorService.class);
        correlationKeyFactory = KieInternalServices.Factory.get().newCorrelationKeyFactory();
    }

    public String getDeploymentIdByConversationId(String conversationId) {
        conversationId = trimToNull(conversationId);
        if (conversationId != null) {
            return getDeploymentIdByConversationId(ConversationId.fromString(conversationId));
        }
        return null;
    }

    public String getDeploymentIdByConversationId(ConversationId conversationId) {
        if (conversationId != null) {
            return conversationId.getContainerId();
        }
        return null;
    }

    public String getDeploymentIdByCorrelationKey(String correlationKey) {
        correlationKey = trimToNull(correlationKey);
        if (correlationKey != null) {
            return getDeploymentIdByCorrelationKey(correlationKeyFactory.newCorrelationKey(correlationKey));
        }
        return null;
    }

    public String getDeploymentIdByCorrelationKey(CorrelationKey correlationKey) {
        if (correlationKey != null) {
            ProcessInstanceDesc desc = runtimeDataService.getProcessInstanceByCorrelationKey(correlationKey);
            if (desc != null) {
                return desc.getDeploymentId();
            }
        }
        return null;
    }

    public String getDeploymentIdByJobId(String jobId) {
        jobId = trimToNull(jobId);
        if (jobId != null) {
            return getDeploymentIdByJobId(Long.valueOf(jobId));
        }
        return null;
    }

    public String getDeploymentIdByJobId(Long jobId) {
        if (jobId != null) {
            RequestInfo requestInfo = executorService.getRequestById(jobId);
            if (requestInfo instanceof org.jbpm.executor.entities.RequestInfo) {
                return ((org.jbpm.executor.entities.RequestInfo)requestInfo).getDeploymentId();
            }
        }
        return null;
    }

    public String getDeploymentIdByProcessInstanceId(String pInstanceId) {
        pInstanceId = trimToNull(pInstanceId);
        if (pInstanceId != null) {
            return getDeploymentIdByProcessInstanceId(Long.valueOf(pInstanceId));
        }
        return null;
    }

    public String getDeploymentIdByProcessInstanceIds(String[] pInstanceIds) {
        if (pInstanceIds != null) {
            List<Long> list = new ArrayList<Long>(pInstanceIds.length);
            for (String pInstanceId : pInstanceIds) {
                pInstanceId = trimToNull(pInstanceId);
                if (pInstanceId != null) {
                    list.add(Long.valueOf(pInstanceId));
                }
            }
            return getDeploymentIdByProcessInstanceIds(list);
        }
        return null;
    }

    public String getDeploymentIdByProcessInstanceId(Long pInstanceId) {
        if (pInstanceId != null) {
            ProcessInstanceDesc desc = runtimeDataService.getProcessInstanceById(pInstanceId);
            if (desc != null) {
                return desc.getDeploymentId();
            }
        }
        return null;
    }

    public String getDeploymentIdByProcessInstanceIds(List<Long> pInstanceIds) {
        if (pInstanceIds != null) {
            TreeSet<Long> set = new TreeSet<Long>();
            for (Long pInstanceId : pInstanceIds) {
                if (pInstanceId != null) {
                    set.add(pInstanceId);
                }
            }
            // using a descending iterator so the most current process instance gets checked first
            Iterator<Long> iter = set.descendingIterator();
            while (iter.hasNext()) {
                String deploymentId = getDeploymentIdByProcessInstanceId(iter.next());
                if (deploymentId != null) {
                    return deploymentId;
                }
            }
        }
        return null;
    }

    public String getDeploymentIdByTaskInstanceId(String tInstanceId) {
        tInstanceId = trimToNull(tInstanceId);
        if (tInstanceId != null) {
            return getDeploymentIdByTaskInstanceId(Long.valueOf(tInstanceId));
        }
        return null;
    }

    public String getDeploymentIdByTaskInstanceId(Long tInstanceId) {
        if (tInstanceId != null) {
            UserTaskInstanceDesc desc = runtimeDataService.getTaskById(tInstanceId);
            if (desc != null) {
                return desc.getDeploymentId();
            }
        }
        return null;
    }

    public String getDeploymentIdByWorkItemId(String workItemId) {
        workItemId = trimToNull(workItemId);
        if (workItemId != null) {
            return getDeploymentIdByWorkItemId(Long.valueOf(workItemId));
        }
        return null;
    }

    public String getDeploymentIdByWorkItemId(Long workItemId) {
        if (workItemId != null) {
            UserTaskInstanceDesc desc = runtimeDataService.getTaskByWorkItemId(workItemId);
            if (desc != null) {
                return desc.getDeploymentId();
            }
        }
        return null;
    }

    private String trimToNull(String s) {
        if (s != null) {
            s = s.trim();
            if (s.isEmpty()) {
                s = null;
            }
        }
        return s;
    }

}
