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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.kie.server.services.impl.KieServerLocator;
import org.openshift.kieserver.common.id.ConversationId;

public class RedirectServletRequestWrapper extends HttpServletRequestWrapper {

    private static final boolean CONVERSATION_ID_SUPPORTED = ConversationId.isSupported();

    private final HttpServletRequest request;
    private final String conversationId;
    private final Map<String, String[]> parameterOverrides;

    public RedirectServletRequestWrapper(HttpServletRequest request) {
        this(request, null);
    }

    public RedirectServletRequestWrapper(HttpServletRequest request, String redirectDeploymentId) {
        this(request, redirectDeploymentId, null);
    }

    public RedirectServletRequestWrapper(HttpServletRequest request, String redirectDeploymentId, Map<String, String[]> parameterOverrides) {
        super(request);
        this.request = request;
        String redirectConversationId = null;
        if (redirectDeploymentId != null && CONVERSATION_ID_SUPPORTED) {
            KieContainerInstanceImpl container = KieServerLocator.getInstance().getServerRegistry().getContainer(redirectDeploymentId);
            if (container != null) {
                ReleaseId releaseId = container.getResource().getResolvedReleaseId();
                if (releaseId == null) {
                    releaseId = container.getResource().getReleaseId();
                }
                redirectConversationId = ConversationId.from(KieServerEnvironment.getServerId(), redirectDeploymentId, releaseId).toString();
            }
        }
        this.conversationId = redirectConversationId;
        this.parameterOverrides = (parameterOverrides != null ? parameterOverrides : Collections.<String, String[]>emptyMap());
    }

    @Override
    public String getHeader(String name) {
        if (KIE_CONVERSATION_ID_TYPE_HEADER.equalsIgnoreCase(name)) {
            return conversationId;
        }
        return request.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Collection<String> coll = new ArrayList<String>();
        boolean conversationIdAdded = false;
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (KIE_CONVERSATION_ID_TYPE_HEADER.equalsIgnoreCase(name)) {
                if (conversationId != null) {
                    coll.add(name);
                    conversationIdAdded = true;
                }
            } else {
                coll.add(name);
            }
        }
        if (!conversationIdAdded && conversationId != null) {
            coll.add(KIE_CONVERSATION_ID_TYPE_HEADER);
        }
        return Collections.enumeration(coll);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if (KIE_CONVERSATION_ID_TYPE_HEADER.equalsIgnoreCase(name)) {
            if (conversationId != null) {
                return Collections.enumeration(Collections.singleton(conversationId));
            }
            return Collections.emptyEnumeration();
        }
        return request.getHeaders(name);
    }

    @Override
    public String getParameter(String name) {
        String[] values = getParameterValues(name);
        return values != null && values.length > 0 ? values[0] : null;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> parameterMap = new HashMap<String, String[]>();
        parameterMap.putAll(request.getParameterMap());
        parameterMap.putAll(parameterOverrides);
        return parameterMap;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(getParameterMap().keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return getParameterMap().get(name);
    }

    @Override
    public String getQueryString() {
        String queryString = request.getQueryString();
        if (queryString != null && !parameterOverrides.isEmpty()) {
            // TODO: fix regex so we don't need to add the preceding question mark
            if (!queryString.startsWith("?")) {
                queryString = "?" + queryString;
            }
            for (String name : parameterOverrides.keySet()) {
                String value = getParameter(name);
                queryString = queryString.replaceAll("(?<=[?&;])" + name + "=[^&;]*", name + "=" + value);
            }
            return queryString.startsWith("?") ? queryString.substring(1, queryString.length()) : queryString;
        }
        return queryString;
    }

}
