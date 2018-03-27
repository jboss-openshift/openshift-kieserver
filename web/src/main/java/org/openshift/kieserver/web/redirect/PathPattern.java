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

import java.io.File;
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
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.plugins.server.servlet.ServletUtil;
import org.jboss.resteasy.specimpl.PathSegmentImpl;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.rest.RestURI;
import org.kie.server.remote.rest.common.KieServerApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PathPattern {

    public static final String ID = RestURI.CONTAINER_ID; // id
    public static final String CORRELATION_KEY = RestURI.CORRELATION_KEY; // correlationKey
    public static final String JOB_ID = RestURI.JOB_ID; // jobId
    public static final String P_INSTANCE_ID = RestURI.PROCESS_INST_ID; // pInstanceId
    public static final String T_INSTANCE_ID = RestURI.TASK_INSTANCE_ID; // tInstanceId
    public static final String WORK_ITEM_ID = RestURI.WORK_ITEM_ID; // workItemId

    private static final Logger LOGGER = LoggerFactory.getLogger(PathPattern.class);

    private static final Class<?>[] JAXRS_METHODS = new Class<?>[] {
            DELETE.class, GET.class, HEAD.class, OPTIONS.class, POST.class, PUT.class
    };
    private static final String NUM_EXPR = "[0-9]+";
    private static final String ANY_EXPR = "[^/]+";

    private final String path;
    private final Pattern pattern;
    private final Map<Integer,String> positions = new LinkedHashMap<Integer,String>();

    // package-protected for junit testing
    PathPattern(String pathInfo) {
        this.path = pathInfo.startsWith("/") ? pathInfo : "/" + pathInfo;
        String regex = this.path;
        for (String id : new String[]{JOB_ID, P_INSTANCE_ID, T_INSTANCE_ID, WORK_ITEM_ID}) {
            regex = regex.replaceAll("\\{" + id + "\\}", NUM_EXPR);
        }
        regex = regex.replaceAll("\\{" + ANY_EXPR + "\\}", ANY_EXPR);
        this.pattern = regex.contains(NUM_EXPR) || regex.contains(ANY_EXPR) ? Pattern.compile(regex) : null;
        String undPath = this.path.replaceAll("\\{", "_").replaceAll("\\}", "");
        List<PathSegment> undSegs = PathSegmentImpl.parseSegments(undPath, false);
        for (int i=0; i < undSegs.size(); i++) {
            String undVar = undSegs.get(i).getPath();
            if (undVar.startsWith("_")) {
                positions.put(i, undVar.substring(1));
            }
        }
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
            UriInfo uriInfo = ServletUtil.extractUriInfo(request, request.getServletPath());
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

    public String buildRedirectPath(HttpServletRequest request, String containerId) {
        Map<String, String> vars = getVariables(request);
        if (containerId != null) {
            vars.put(ID, containerId);
        }
        String path = buildPath(vars);
        return request.getServletPath() + path;
    }

    @Override
    public String toString() {
        return String.format("%s: path=[ %s ], pattern=[ %s ], positions=[ %s ]", PathPattern.class.getSimpleName(), path, pattern, positions);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static List<PathPattern> buildPathPatterns() {
        Set<String> paths = new TreeSet<String>(new Comparator<String>(){
            @Override
            public int compare(String o1, String o2) {
                //return o1.compareTo(o2);
                // make longer paths match first
                int l1 = o1.length();
                int l2 = o2.length();
                if (l1 > l2) {
                    return -1;
                } else if (l2 > l1) {
                    return 1;
                } else {
                    return o1.compareTo(o2);
                }
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
            PathPattern pp = new PathPattern(path);
            //System.out.println(pp);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(pp.toString());
            }
            pathPatterns.add(pp);
        }
        return Collections.unmodifiableList(pathPatterns);
    }

    public static void main(String... args) {
        String simpleName = PathPattern.class.getSimpleName();
        File simpleFile = null;
        if (KieServerEnvironment.getServerId() == null) {
            KieServerEnvironment.setServerId(simpleName);
            simpleFile = new File(simpleName + ".xml");
        }
        if (KieServerEnvironment.getServerName() == null) {
            KieServerEnvironment.setServerName(simpleName);
        }
        for (PathPattern pp : buildPathPatterns()) {
            System.out.println(pp);
        }
        if (simpleFile != null && simpleFile.exists()) {
            simpleFile.delete();
        }
    }

}