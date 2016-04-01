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
import static org.openshift.kieserver.web.redirect.PathPattern.ID;
import static org.openshift.kieserver.web.redirect.PathPattern.P_INSTANCE_ID;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.openshift.kieserver.common.id.ConversationId;

public class ServletRedirectData implements RedirectData {

    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final List<PathPattern> pathPatterns;
    private final RuntimeDataService runtimeDataService;

    public ServletRedirectData(ServletRequest request, ServletResponse response, List<PathPattern> pathPatterns, RuntimeDataService runtimeDataService) {
        this.request = (HttpServletRequest)request;
        this.response = (HttpServletResponse)response;
        this.pathPatterns = pathPatterns;
        this.runtimeDataService = runtimeDataService;
    }

    @Override
    public String getRequestedContainerId() {
        for (PathPattern pathPattern : pathPatterns) {
            if (pathPattern.matches(request)) {
                Map<String, String> vars = pathPattern.getVariables(request);
                String cid = vars.get(ID);
                if (cid != null) {
                    return cid;
                }
            }
        }
        return null;
    }

    @Override
    public String getConversationContainerId() {
        String header = request.getHeader(KIE_CONVERSATION_ID_TYPE_HEADER);
        if (header != null) {
            header = header.trim();
            if (header.length() > 0) {
                ConversationId conversationId = ConversationId.fromString(header);
                return conversationId.getContainerId();
            }
        }
        return null;
    }

    @Override
    public String getProcessContainerId() {
        String pInstanceId = null;
        for (PathPattern pathPattern : pathPatterns) {
            if (pathPattern.matches(request)) {
                Map<String, String> vars = pathPattern.getVariables(request);
                pInstanceId = vars.get(P_INSTANCE_ID);
                if (pInstanceId != null) {
                    break;
                }
            }
        }
        if (pInstanceId != null) {
            long pid = Long.valueOf(pInstanceId).longValue();
            ProcessInstanceDesc desc = runtimeDataService.getProcessInstanceById(pid);
            if (desc != null) {
                return desc.getDeploymentId();
            }
        }
        return null;
    }

    @Override
    public String buildRedirect(String containerId) {
        for (PathPattern pathPattern : pathPatterns) {
            if (pathPattern.matches(request)) {
                return pathPattern.buildRedirectPath(request, containerId);
            }
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
