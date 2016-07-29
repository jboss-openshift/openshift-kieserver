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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
public class RedirectServletRequestWrapper extends HttpServletRequestWrapper {

    private final HttpServletRequest request;
    private final Set<String> headerIgnores;
    private final Map<String, String[]> parameterOverrides;

    public RedirectServletRequestWrapper(HttpServletRequest request) {
        this(request, null);
    }

    public RedirectServletRequestWrapper(HttpServletRequest request, Set<String> headerIgnores) {
        this(request, headerIgnores, null);
    }

    public RedirectServletRequestWrapper(HttpServletRequest request, Set<String> headerIgnores, Map<String, String[]> parameterOverrides) {
        super(request);
        this.request = request;
        this.headerIgnores = (headerIgnores != null ? headerIgnores : Collections.<String>emptySet());
        this.parameterOverrides = (parameterOverrides != null ? parameterOverrides : Collections.<String, String[]>emptyMap());
    }

    @Override
    public String getHeader(String name) {
        if (name != null && headerIgnores.contains(name.toUpperCase())) {
            return null;
        }
        return request.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Collection<String> coll = new ArrayList<String>();
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (!headerIgnores.contains(name.toUpperCase())) {
                coll.add(name);
            }
        }
        return Collections.enumeration(coll);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if (name != null && headerIgnores.contains(name.toUpperCase())) {
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
