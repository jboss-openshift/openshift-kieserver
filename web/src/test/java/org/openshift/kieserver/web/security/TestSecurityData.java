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
package org.openshift.kieserver.web.security;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.openshift.kieserver.web.security.SecurityData;

public class TestSecurityData implements SecurityData {

    private final String path;
    private final String method;
    private final Set<String> roles = new HashSet<String>();

    public TestSecurityData(String path, String method, String... roles) {
        this.path = path;
        this.method = method.toUpperCase();
        for (String role : roles) {
            this.roles.add(role);
        }
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public boolean isUserInRole(String role) {
        return roles.contains(role);
    }

    @Override
    public void log(String msg) {
        System.out.println(msg);
        System.out.flush();
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        System.err.println("[" + sc + "] " + msg);
        System.err.flush();
    }

}
