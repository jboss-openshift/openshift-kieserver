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
package org.openshift.kieserver.web.redirect;

import static org.openshift.kieserver.common.id.ConversationId.KIE_CONVERSATION_ID_TYPE_HEADER;
import static org.openshift.kieserver.web.redirect.PathPattern.CORRELATION_KEY;
import static org.openshift.kieserver.web.redirect.PathPattern.ID;
import static org.openshift.kieserver.web.redirect.PathPattern.JOB_ID;
import static org.openshift.kieserver.web.redirect.PathPattern.P_INSTANCE_ID;
import static org.openshift.kieserver.web.redirect.PathPattern.T_INSTANCE_ID;
import static org.openshift.kieserver.web.redirect.PathPattern.WORK_ITEM_ID;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openshift.kieserver.common.server.DeploymentHelper;

public class ServletRedirectData implements RedirectData {

    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final PathPattern pathPattern;
    private final Map<String, String> pathVariables;
    private final DeploymentHelper deploymentHelper;

    public ServletRedirectData(
            ServletRequest request,
            ServletResponse response, 
            List<PathPattern> pathPatterns,
            DeploymentHelper deploymentHelper) {
        this.request = (HttpServletRequest)request;
        this.response = (HttpServletResponse)response;
        this.pathPattern = getPathPattern(pathPatterns);
        this.pathVariables = getPathVariables(pathPattern);
        this.deploymentHelper = deploymentHelper;
    }

    private PathPattern getPathPattern(List<PathPattern> pathPatterns) {
        for (PathPattern pathPattern : pathPatterns) {
            if (pathPattern.matches(request)) {
                return pathPattern;
            }
        }
        return null;
    }

    private Map<String, String> getPathVariables(PathPattern pathPattern) {
        if (pathPattern != null) {
            return pathPattern.getVariables(request);
        }
        return Collections.emptyMap();
    }

    @Override
    public String getRequestedContainerId() {
        String id = pathVariables.get(ID);
        if (id == null) {
            id = request.getParameter("containerId");
        }
        return id;
    }

    @Override
    public String getDeploymentIdByConversationId() {
        String conversationId = request.getHeader(KIE_CONVERSATION_ID_TYPE_HEADER);
        return deploymentHelper.getDeploymentIdByConversationId(conversationId);
    }

    @Override
    public String getDeploymentIdByCorrelationKey() {
        String correlationKey = pathVariables.get(CORRELATION_KEY);
        return deploymentHelper.getDeploymentIdByCorrelationKey(correlationKey);
    }

    @Override
    public String getDeploymentIdByJobId() {
        String jobId = pathVariables.get(JOB_ID);
        return deploymentHelper.getDeploymentIdByJobId(jobId);
    }

    @Override
    public String getDeploymentIdByProcessInstanceId() {
        String pInstanceId = pathVariables.get(P_INSTANCE_ID);
        return deploymentHelper.getDeploymentIdByProcessInstanceId(pInstanceId);
    }

    @Override
    public String getDeploymentIdByTaskInstanceId() {
        String tInstanceId = pathVariables.get(T_INSTANCE_ID);
        return deploymentHelper.getDeploymentIdByTaskInstanceId(tInstanceId);
    }

    @Override
    public String getDeploymentIdByWorkItemId() {
        String workItemId = pathVariables.get(WORK_ITEM_ID);
        return deploymentHelper.getDeploymentIdByWorkItemId(workItemId);
    }

    @Override
    public String buildRedirect(String deploymentId) {
        if (pathPattern != null) {
            return pathPattern.buildRedirectPath(request, deploymentId);
        }
        return null;
    }

    @Override
    public void log(String msg) {
        request.getServletContext().log(msg);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        response.sendError(sc, msg);
    }

}
