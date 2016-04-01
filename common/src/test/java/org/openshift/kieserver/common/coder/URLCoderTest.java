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
package org.openshift.kieserver.common.coder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;
import org.openshift.kieserver.common.coder.URLCoder;

public class URLCoderTest {

    private static final String DECODED = "My Container=org.example:test:1.3.0-SNAPSHOT";
    private static final String ENCODED = "My+Container%3Dorg.example%3Atest%3A1.3.0-SNAPSHOT";

    @Test
    public void testEncode() {
        assertEquals(ENCODED, new URLCoder().encode(DECODED));
    }

    @Test
    public void testDecode() {
        assertEquals(DECODED, new URLCoder().decode(ENCODED));
    }

    @Test
    public void testMainEncode() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean valid = URLCoder.main(new String[]{"encode", DECODED}, new PrintStream(out, true), null);
        assertTrue(valid);
        assertEquals(ENCODED, new String(out.toByteArray(), "UTF-8"));
    }

    @Test
    public void testMainDecode() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean valid = URLCoder.main(new String[]{"decode", ENCODED}, new PrintStream(out, true), null);
        assertTrue(valid);
        assertEquals(DECODED, new String(out.toByteArray(), "UTF-8"));
    }

    @Test
    public void testMainInvalid() throws Exception {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        boolean valid = URLCoder.main(new String[]{"invalid"}, null, new PrintStream(err, true));
        assertFalse(valid);
        assertEquals(URLCoder.USAGE, new String(err.toByteArray(), "UTF-8"));
    }

}
