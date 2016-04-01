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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class SecurityRule {

    private final String name;
    private final Pattern pattern;
    private final Set<String> methods = new HashSet<String>();
    private final String role;

    public SecurityRule(String name, String config) {
        this.name = name;
        String[] values = config.split("\\|");
        this.pattern = Pattern.compile(values[0]);
        for (String method : values[1].split(",")) {
            methods.add(method.toUpperCase());
        }
        this.role = values[2];
    }

    public String getName() {
        return name;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public Set<String> getMethods() {
        return Collections.unmodifiableSet(methods);
    }

    public String getRole() {
        return role;
    }

    public boolean accepts(SecurityData data) {
        if (pattern.matcher(data.getPath()).matches()) {
            if (methods.contains(data.getMethod())) {
                if (!data.isUserInRole(role)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "SecurityRule [name=" + name + ", pattern=" + pattern + ", methods=" + methods + ", role=" + role + "]";
    }

}
