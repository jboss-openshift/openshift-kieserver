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
import java.util.regex.Pattern;

public final class URLCoder implements Coder {

    private static final String UTF_8 = "UTF-8";
    private static final Pattern PLUS_PATTERN = Pattern.compile("\\+");
    private static final String PERCENT20_STRING = "%20";

    private final String charset;
    private final boolean replacePlus;

    public URLCoder() {
        this(UTF_8, true);
    }

    public URLCoder(String charset) {
        this(charset, true);
    }

    public URLCoder(boolean replacePlus) {
        this(UTF_8, replacePlus);
    }

    public URLCoder(String charset, boolean replacePlus) {
        this.charset = charset;
        this.replacePlus = replacePlus;
    }

    @Override
    public String encode(String s) {
        try {
            String e = URLEncoder.encode(s, charset);
            if (replacePlus) {
                e = PLUS_PATTERN.matcher(e).replaceAll(PERCENT20_STRING);
            }
            return e;
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
        if (args != null && args.length > 1) {
            String op = args[0].trim().toLowerCase();
            StringBuilder sb = new StringBuilder();
            for (int i=1; i < args.length; i++) {
                if (i > 1) {
                    sb.append(' ');
                }
                sb.append(args[i]);
            }
            String s = sb.toString();
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
