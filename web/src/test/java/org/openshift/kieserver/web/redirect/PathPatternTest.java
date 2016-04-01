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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openshift.kieserver.web.redirect.PathPattern.ID;
import static org.openshift.kieserver.web.redirect.PathPattern.P_INSTANCE_ID;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class PathPatternTest {

    @Test
    public void testMatches() {
        PathPattern pp = new PathPattern("foo/{id}/bar/{pInstanceId}/pizza");
        assertTrue(pp.matches("/foo/MyId/bar/42/pizza"));
        assertTrue(pp.matches("foo/MyId/bar/42/pizza"));
        assertFalse(pp.matches("/bar/MyId/foo/42/pizza"));
    }

    @Test
    public void testBuildPath() {
        PathPattern pp = new PathPattern("foo/{id}/bar/{pInstanceId}/pizza");
        Map<String,String> vars = new HashMap<String,String>();
        vars.put(ID, "MyId");
        vars.put(P_INSTANCE_ID, "42");
        String path = pp.buildPath(vars);
        assertEquals("/foo/MyId/bar/42/pizza", path);
    }

}
