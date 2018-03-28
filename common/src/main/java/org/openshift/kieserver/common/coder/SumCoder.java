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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

public class SumCoder implements Coder {

    private final String charset;
    private final String algorithm;

    public SumCoder(String algorithm) {
        this(algorithm, "UTF-8");
    }

    public SumCoder(String algorithm, String charset) {
        this.algorithm = algorithm;
        this.charset = charset;
    }

    @Override
    public String encode(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] digest = md.digest(s.getBytes(charset));
            return DatatypeConverter.printHexBinary(digest).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String decode(String s) {
        throw new UnsupportedOperationException(algorithm + " decode unsupported");
    }

    public static void main(String[] args) {
        boolean valid = main(args, System.out, System.err);
        if (!valid) {
            System.exit(1);
        }
    }

    // package-protected for JUnit testing
    static final String USAGE = "Usage: java " + SumCoder.class.getName() + " <algorithm> <string>\n";
    static boolean main(String[] args, PrintStream out, PrintStream err) {
        boolean valid = false;
        if (args != null && args.length > 1) {
            String algo = args[0].trim().toUpperCase();
            StringBuilder sb = new StringBuilder();
            for (int i=1; i < args.length; i++) {
                if (i > 1) {
                    sb.append(' ');
                }
                sb.append(args[i]);
            }
            String s = sb.toString();
            out.print(new SumCoder(algo).encode(s));
            valid = true;
        }
        if (!valid) {
            err.print(USAGE);
        }
        return valid;
    }

    public static class MD5 extends SumCoder {

        public MD5() {
            super("MD5");
        }

        public MD5(String charset) {
            super("MD5", charset);
        }

        // package-protected for JUnit testing
        static final String USAGE = "Usage: java " + SumCoder.class.getName() + "$" + MD5.class.getSimpleName() + " <string>\n";
        static boolean main(String[] args, PrintStream out, PrintStream err) {
            boolean valid = false;
            if (args != null && args.length > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i=0; i < args.length; i++) {
                    if (i > 0) {
                        sb.append(' ');
                    }
                    sb.append(args[i]);
                }
                String s = sb.toString();
                out.print(new MD5().encode(s));
                valid = true;
            }
            if (!valid) {
                err.print(USAGE);
            }
            return valid;
        }

    }

    public static class SHA1 extends SumCoder {

        public SHA1() {
            super("SHA-1");
        }

        public SHA1(String charset) {
            super("SHA-1", charset);
        }

        // package-protected for JUnit testing
        static final String USAGE = "Usage: java " + SumCoder.class.getName() + "$" + SHA1.class.getSimpleName() + " <string>\n";
        static boolean main(String[] args, PrintStream out, PrintStream err) {
            boolean valid = false;
            if (args != null && args.length > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i=0; i < args.length; i++) {
                    if (i > 0) {
                        sb.append(' ');
                    }
                    sb.append(args[i]);
                }
                String s = sb.toString();
                out.print(new SHA1().encode(s));
                valid = true;
            }
            if (!valid) {
                err.print(USAGE);
            }
            return valid;
        }

    }

    public static class SHA256 extends SumCoder {

        public SHA256() {
            super("SHA-256");
        }

        public SHA256(String charset) {
            super("SHA-256", charset);
        }

        // package-protected for JUnit testing
        static final String USAGE = "Usage: java " + SumCoder.class.getName() + "$" + SHA256.class.getSimpleName() + " <string>\n";
        static boolean main(String[] args, PrintStream out, PrintStream err) {
            boolean valid = false;
            if (args != null && args.length > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i=0; i < args.length; i++) {
                    if (i > 0) {
                        sb.append(' ');
                    }
                    sb.append(args[i]);
                }
                String s = sb.toString();
                out.print(new SHA256().encode(s));
                valid = true;
            }
            if (!valid) {
                err.print(USAGE);
            }
            return valid;
        }

    }

}