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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Properties;

import org.junit.Test;
import org.openshift.kieserver.web.security.SecurityFilter;

public class SecurityFilterTest {

    private static final SecurityFilter filter = new SecurityFilter();
    static {
        Properties properties = new Properties();
        properties.setProperty("containers", "/server/containers/([^/]*/?)|PUT,DELETE|restricted");
        properties.setProperty("releaseIds", "/server/containers/.*/release-id|POST|restricted");
        properties.setProperty("scanners", "/server/containers/.*/scanner|POST|restricted");
        filter.init(SecurityFilterTest.class.getSimpleName(), properties);
    }

    @Test
    public void testContainersRule() throws IOException {
        TestSecurityData data;
        data = new TestSecurityData("/server/containers/MyContainer", "GET", "kie-server");
        assertTrue(filter.doFilter(data));
        data = new TestSecurityData("/server/containers/MyContainer/", "GET", "kie-server");
        assertTrue(filter.doFilter(data));
        data = new TestSecurityData("/server/containers/MyContainer", "PUT", "kie-server");
        assertFalse(filter.doFilter(data));
        data = new TestSecurityData("/server/containers/MyContainer/", "DELETE", "kie-server");
        assertFalse(filter.doFilter(data));
    }

    @Test
    public void testReleaseIdsRule() throws IOException {
        TestSecurityData data;
        data = new TestSecurityData("/server/containers/MyContainer/release-id", "GET", "kie-server");
        assertTrue(filter.doFilter(data));
        data = new TestSecurityData("/server/containers/MyContainer/release-id", "POST", "kie-server");
        assertFalse(filter.doFilter(data));
    }

    @Test
    public void testScannersRule() throws IOException {
        TestSecurityData data;
        data = new TestSecurityData("/server/containers/MyContainer/scanner", "GET", "kie-server");
        assertTrue(filter.doFilter(data));
        data = new TestSecurityData("/server/containers/MyContainer/scanner", "POST", "kie-server");
        assertFalse(filter.doFilter(data));
    }

}
