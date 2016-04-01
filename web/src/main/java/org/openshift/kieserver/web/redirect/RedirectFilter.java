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

import static java.util.logging.Level.FINE;
import static org.openshift.kieserver.common.server.ServerUtil.CAPABILITY_BPM;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.jbpm.services.api.RuntimeDataService;
import org.openshift.kieserver.common.server.ServerConfig;
import org.openshift.kieserver.common.server.ServerUtil;

public class RedirectFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(RedirectFilter.class.getName());

    private ServerConfig serverConfig = null;
    private boolean containerRedirectEnabled = false;
    private List<PathPattern> pathPatterns = null;
    private RuntimeDataService runtimeDataService = null;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        serverConfig = ServerConfig.getInstance();
        containerRedirectEnabled = serverConfig.isContainerRedirectEnabled();
        if (containerRedirectEnabled) {
            pathPatterns = PathPattern.buildPathPatterns();
            runtimeDataService = ServerUtil.getAppComponentService(CAPABILITY_BPM, RuntimeDataService.class);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!containerRedirectEnabled) {
            chain.doFilter(request, response);
            return;
        }
        String redirect = null;
        RedirectData data = new ServletRedirectData(request, response, pathPatterns, runtimeDataService);
        String requestedContainerId = data.getRequestedContainerId();
        if (!serverConfig.hasDeploymentId(requestedContainerId)) {
            if (redirect == null) {
                String conversationContainerId = data.getConversationContainerId();
                if (serverConfig.hasDeploymentId(conversationContainerId)) {
                    redirect = data.buildRedirect(conversationContainerId);
                }
            }
            if (redirect == null) {
                String processContainerId = data.getProcessContainerId();
                if (serverConfig.hasDeploymentId(processContainerId)) {
                    redirect = data.buildRedirect(processContainerId);
                }
            }
            if (redirect == null) {
                String defaultDeploymentId = serverConfig.getDefaultDeploymentId(requestedContainerId);
                if (serverConfig.hasDeploymentId(defaultDeploymentId)) {
                    redirect = data.buildRedirect(defaultDeploymentId);
                }
            }
        }
        if (redirect != null) {
            if (LOGGER.isLoggable(FINE)) {
                HttpServletRequest httpRequest = (HttpServletRequest)request;
                String log = String.format("doFilter redirecting: %s%s -> %s", httpRequest.getServletPath(), httpRequest.getPathInfo(), redirect);
                LOGGER.log(FINE, log);
            }
            request.getRequestDispatcher(redirect).forward(request, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        serverConfig = null;
        containerRedirectEnabled = false;
        pathPatterns = null;
        runtimeDataService = null;
    }

}
