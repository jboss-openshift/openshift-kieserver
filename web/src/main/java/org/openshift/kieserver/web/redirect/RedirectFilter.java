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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.openshift.kieserver.common.server.DeploymentHelper;
import org.openshift.kieserver.common.server.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedirectFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedirectFilter.class);

    private ServerConfig serverConfig = null;
    private boolean containerRedirectEnabled = false;
    private List<PathPattern> pathPatterns = null;
    private DeploymentHelper deploymentHelper = null;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        serverConfig = ServerConfig.getInstance();
        containerRedirectEnabled = serverConfig.isContainerRedirectEnabled();
        if (containerRedirectEnabled) {
            pathPatterns = PathPattern.buildPathPatterns();
            deploymentHelper = new DeploymentHelper();
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
        String redirectDeploymentId = null;
        RedirectData data = new ServletRedirectData(request, response, pathPatterns, deploymentHelper);
        String requestedContainerId = data.getRequestedContainerId();
        // only if the id is not an actual deployment, do we try to redirect
        if (!serverConfig.hasDeploymentId(requestedContainerId)) {
            if (redirect == null) {
                redirectDeploymentId = serverConfig.getDeploymentIdForContainerConfig(requestedContainerId);
                if (serverConfig.hasDeploymentId(redirectDeploymentId)) {
                    redirect = data.buildRedirect(redirectDeploymentId);
                }
            }
            if (redirect == null) {
                redirectDeploymentId = data.getDeploymentIdByProcessInstanceId();
                if (serverConfig.hasDeploymentId(redirectDeploymentId)) {
                    redirect = data.buildRedirect(redirectDeploymentId);
                }
            }
            if (redirect == null) {
                redirectDeploymentId = data.getDeploymentIdByCorrelationKey();
                if (serverConfig.hasDeploymentId(redirectDeploymentId)) {
                    redirect = data.buildRedirect(redirectDeploymentId);
                }
            }
            if (redirect == null) {
                redirectDeploymentId = data.getDeploymentIdByTaskInstanceId();
                if (serverConfig.hasDeploymentId(redirectDeploymentId)) {
                    redirect = data.buildRedirect(redirectDeploymentId);
                }
            }
            if (redirect == null) {
                redirectDeploymentId = data.getDeploymentIdByWorkItemId();
                if (serverConfig.hasDeploymentId(redirectDeploymentId)) {
                    redirect = data.buildRedirect(redirectDeploymentId);
                }
            }
            if (redirect == null) {
                redirectDeploymentId = data.getDeploymentIdByJobId();
                if (serverConfig.hasDeploymentId(redirectDeploymentId)) {
                    redirect = data.buildRedirect(redirectDeploymentId);
                }
            }
            if (redirect == null) {
                String conversationDeploymentId = data.getDeploymentIdByConversationId();
                String containerAlias = serverConfig.getContainerAliasForDeploymentId(conversationDeploymentId);
                String containerConfig = serverConfig.getContainerConfigForDeploymentId(conversationDeploymentId);
                if (requestedContainerId == null || requestedContainerId.equals(containerAlias) || requestedContainerId.equals(containerConfig)) {
                    if (serverConfig.hasDeploymentId(conversationDeploymentId)) {
                        redirectDeploymentId = conversationDeploymentId;
                        redirect = data.buildRedirect(redirectDeploymentId);
                    }
                }
            }
            if (redirect == null) {
                redirectDeploymentId = serverConfig.getDefaultDeploymentIdForAlias(requestedContainerId);
                if (serverConfig.hasDeploymentId(redirectDeploymentId)) {
                    redirect = data.buildRedirect(redirectDeploymentId);
                }
            }
        }
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        if (redirectDeploymentId != null) {
            if (httpRequest.getParameter("containerId") != null) {
                Map<String, String[]> parameterOverrides = new HashMap<String, String[]>();
                // override the container id with the redirected one
                parameterOverrides.put("containerId", new String[]{redirectDeploymentId});
                request = new RedirectServletRequestWrapper(httpRequest, redirectDeploymentId, parameterOverrides);
            } else {
                request = new RedirectServletRequestWrapper(httpRequest, redirectDeploymentId);
            }
            if (redirect == null) {
                // We have to redirect because we need to override the containerId request parameter and filter the X-KIE-ConversationId
                // request header, but we only want to create a new redirect path if we haven't already built one above.
                String pathInfo = httpRequest.getPathInfo();
                redirect = httpRequest.getServletPath() + (pathInfo.startsWith("/") ? pathInfo : "/" + pathInfo);
            }
        } else {
            // We always need to filter the X-KIE-ConversationId request header.
            if (!serverConfig.hasDeploymentId(requestedContainerId)) {
                requestedContainerId = null;
            }
            request = new RedirectServletRequestWrapper(httpRequest, requestedContainerId);
        }
        if (redirect != null) {
            if (LOGGER.isDebugEnabled()) {
                String log = String.format("doFilter redirecting from %s%s to %s", httpRequest.getServletPath(), httpRequest.getPathInfo(), redirect);
                LOGGER.debug(log);
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
        deploymentHelper = null;
    }

}
