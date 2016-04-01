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

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public final class URLCoder implements Coder {

    private final String charset;

    public URLCoder() {
        this("UTF-8");
    }

    public URLCoder(String charset) {
        this.charset = charset;
    }

    @Override
    public String encode(String s) {
        try {
            return URLEncoder.encode(s, charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String decode(String s) {
        try {
            return URLDecoder.decode(s, charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        boolean valid = main(args, System.out, System.err);
        if (!valid) {
            System.exit(1);
        }
    }

    // package-protected for JUnit testing
    static final String USAGE = "Usage: java " + URLCoder.class.getName() + " <encode|decode> <string>\n";
    static boolean main(String[] args, PrintStream out, PrintStream err) {
        boolean valid = false;
        if (args != null && args.length == 2) {
            String op = args[0].trim().toLowerCase();
            String s = args[1];
            if ("encode".equals(op)) {
                out.print(new URLCoder().encode(s));
                valid = true;
            } else if ("decode".equals(op)) {
                out.print(new URLCoder().decode(s));
                valid = true;
            }
        }
        if (!valid) {
            err.print(USAGE);
        }
        return valid;
    }

}
