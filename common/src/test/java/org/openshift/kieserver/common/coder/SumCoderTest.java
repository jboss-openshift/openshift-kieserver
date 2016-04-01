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
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;
import org.openshift.kieserver.common.coder.SumCoder.MD5;
import org.openshift.kieserver.common.coder.SumCoder.SHA1;
import org.openshift.kieserver.common.coder.SumCoder.SHA256;

public class SumCoderTest {

    private static final String DECODED = "My Container=org.example:test:1.3.0-SNAPSHOT";
    private static final String MD5_ENCODED = "b56155057560fe8f3dc44b3347fd3c99";
    private static final String SHA1_ENCODED = "18d1e949c130174f82ade8dac19ce55c48b0b425";
    private static final String SHA256_ENCODED = "9b90c6d7a058ad1731f60c9a52f55db8cbc3c66a2be5f39d55e47277281a8a8e";

    @Test
    public void testEncode() {
        assertEquals(MD5_ENCODED, new MD5().encode(DECODED));
        assertEquals(SHA1_ENCODED, new SHA1().encode(DECODED));
        assertEquals(SHA256_ENCODED, new SHA256().encode(DECODED));
    }

    @Test
    public void testDecode() {
        try {
            new MD5().decode(MD5_ENCODED);
            fail("MD5 decode should be unsupported");
        } catch (UnsupportedOperationException e) {
            assertEquals("MD5 decode unsupported", e.getMessage());
        }
        try {
            new SHA1().decode(SHA1_ENCODED);
            fail("SHA-1 decode should be unsupported");
        } catch (UnsupportedOperationException e) {
            assertEquals("SHA-1 decode unsupported", e.getMessage());
        }
        try {
            new SHA256().decode(SHA256_ENCODED);
            fail("SHA-256 decode should be unsupported");
        } catch (UnsupportedOperationException e) {
            assertEquals("SHA-256 decode unsupported", e.getMessage());
        }
    }

    @Test
    public void testMainEncode() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean valid = MD5.main(new String[]{DECODED}, new PrintStream(out, true), null);
        assertTrue(valid);
        assertEquals(MD5_ENCODED, new String(out.toByteArray(), "UTF-8"));
        out = new ByteArrayOutputStream();
        valid = SHA1.main(new String[]{DECODED}, new PrintStream(out, true), null);
        assertTrue(valid);
        assertEquals(SHA1_ENCODED, new String(out.toByteArray(), "UTF-8"));
        out = new ByteArrayOutputStream();
        valid = SHA256.main(new String[]{DECODED}, new PrintStream(out, true), null);
        assertTrue(valid);
        assertEquals(SHA256_ENCODED, new String(out.toByteArray(), "UTF-8"));
    }

    @Test
    public void testMainInvalid() throws Exception {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        boolean valid = MD5.main(new String[]{}, null, new PrintStream(err, true));
        assertFalse(valid);
        assertEquals(MD5.USAGE, new String(err.toByteArray(), "UTF-8"));
        err = new ByteArrayOutputStream();
        valid = SHA1.main(new String[]{}, null, new PrintStream(err, true));
        assertFalse(valid);
        assertEquals(SHA1.USAGE, new String(err.toByteArray(), "UTF-8"));
        err = new ByteArrayOutputStream();
        valid = SHA256.main(new String[]{}, null, new PrintStream(err, true));
        assertFalse(valid);
        assertEquals(SHA256.USAGE, new String(err.toByteArray(), "UTF-8"));
    }

}
