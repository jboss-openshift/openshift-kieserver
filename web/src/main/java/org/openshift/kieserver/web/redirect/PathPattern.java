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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;

import org.jboss.resteasy.plugins.server.servlet.ServletUtil;
import org.jboss.resteasy.specimpl.PathSegmentImpl;
import org.jboss.resteasy.specimpl.UriInfoImpl;
import org.kie.server.remote.rest.common.KieServerApplication;

public final class PathPattern {

    public static final String ID = "id";
    public static final String P_INSTANCE_ID = "pInstanceId";

    private static final Class<?>[] JAXRS_METHODS = new Class<?>[] {
        DELETE.class, GET.class, HEAD.class, OPTIONS.class, POST.class, PUT.class
    };
    private static final String EXPR = "[^/]+";

    private final String path;
    private final Pattern pattern;
    private final Map<Integer,String> positions = new LinkedHashMap<Integer,String>();

    // package-protected for junit testing
    PathPattern(String pathInfo) {
        this.path = pathInfo.startsWith("/") ? pathInfo : "/" + pathInfo;
        String regex = this.path.replaceAll("\\{" + EXPR + "\\}", EXPR);
        this.pattern = regex.contains(EXPR) ? Pattern.compile(regex) : null;
        String undPath = this.path.replaceAll("\\{", "_").replaceAll("\\}", "");
        List<PathSegment> undSegs = PathSegmentImpl.parseSegments(undPath, false);
        for (int i=0; i < undSegs.size(); i++) {
            String undVar = undSegs.get(i).getPath();
            if (undVar.startsWith("_")) {
                positions.put(i, undVar.substring(1));
            }
        }
    }

    public String getPath() {
        return path;
    }

    public boolean matches(String pathInfo) {
        if (pathInfo != null && pattern != null) {
            if (!pathInfo.startsWith("/")) {
                pathInfo = "/" + pathInfo;
            }
            return pattern.matcher(pathInfo).matches();
        }
        return false;
    }

    public boolean matches(HttpServletRequest request) {
        if (request != null) {
            String pathInfo = request.getPathInfo();
            if (pathInfo != null && pattern != null) {
                if (!pathInfo.startsWith("/")) {
                    pathInfo = "/" + pathInfo;
                }
                return pattern.matcher(pathInfo).matches();
            }
        }
        return false;
    }

    public Map<String, String> getVariables(HttpServletRequest request) {
        Map<String, String> reqVars = new LinkedHashMap<String, String>();
        if (matches(request)) {
            UriInfoImpl uriInfo = ServletUtil.extractUriInfo(request, request.getServletPath());
            List<PathSegment> reqSegs = uriInfo.getPathSegments();
            for (int i=0; i < reqSegs.size(); i++) {
                String var = positions.get(i);
                if (var != null) {
                    reqVars.put(var, reqSegs.get(i).getPath());
                }
            }
        }
        return reqVars;
    }

    public String buildPath(Map<String, String> variables) {
        return variables != null ? UriBuilder.fromPath(path).buildFromMap(variables).toString() : path;
    }

    public String buildRedirectPath(HttpServletRequest request, String id) {
        Map<String, String> vars = getVariables(request);
        vars.put(ID, id);
        String path = buildPath(vars);
        return request.getServletPath() + path;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static List<PathPattern> buildPathPatterns() {
        Set<String> paths = new TreeSet<String>(new Comparator<String>(){
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
                /*
                // longer paths match first
                int l1 = o1.length();
                int l2 = o2.length();
                if (l1 > l2) {
                    return -1;
                } else if (l2 > l1) {
                    return 1;
                } else {
                    return o1.compareTo(o2);
                }
                */
            }});
        KieServerApplication app = new KieServerApplication();
        Set<Object> singletons = app.getSingletons();
        for (Object singleton : singletons) {
            Path classPathType = singleton.getClass().getAnnotation(Path.class);
            String classPath = classPathType != null ? classPathType.value() : "";
            if (!classPath.startsWith("/")) {
                classPath = "/" + classPath;
            }
            Method[] javaMethods = singleton.getClass().getMethods();
            for (Method javaMethod : javaMethods) {
                String path = classPath;
                Path methodPathType = javaMethod.getAnnotation(Path.class);
                if (methodPathType != null) {
                    String methodPath = methodPathType.value();
                    if (!classPath.endsWith("/") && !methodPath.startsWith("/")) {
                        methodPath = "/" + methodPath;
                    }
                    path = path + methodPath;
                }
                for (Class jaxrsMethod : JAXRS_METHODS) {
                    if (javaMethod.isAnnotationPresent(jaxrsMethod)) {
                        paths.add(path);
                        break;
                    }
                }
            }
        }
        List<PathPattern> pathPatterns = new ArrayList<PathPattern>();
        for (String path : paths) {
            //System.out.println(path);
            pathPatterns.add(new PathPattern(path));
        }
        return Collections.unmodifiableList(pathPatterns);
    }

}
