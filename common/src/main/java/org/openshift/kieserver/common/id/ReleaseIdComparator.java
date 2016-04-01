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
package org.openshift.kieserver.common.id;

import static org.openshift.kieserver.common.id.ComparableReleaseId.toComparable;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.kie.api.builder.ReleaseId;

public class ReleaseIdComparator implements Comparator<ReleaseId> {

    public static enum SortDirection {
        ASCENDING,
        DESCENDING
    }

    private final SortDirection sortDirection;

    public ReleaseIdComparator() {
        this(SortDirection.ASCENDING);
    }

    public ReleaseIdComparator(SortDirection sortDirection) {
        this.sortDirection = sortDirection;
    }

    @Override
    public int compare(ReleaseId o1, ReleaseId o2) {
        if (SortDirection.ASCENDING.equals(sortDirection)) {
            return toComparable(o1).compareTo(toComparable(o2));
        }
        if (SortDirection.DESCENDING.equals(sortDirection)) {
            return toComparable(o2).compareTo(toComparable(o1));
        }
        throw new IllegalArgumentException("unknown sort direction");
    }

    public static String getEarliest(String... gavs) {
        return getFirstSorted(gavs, SortDirection.ASCENDING);
    }

    public static ReleaseId getEarliest(List<ReleaseId> releaseIds) {
        return getFirstSorted(releaseIds, SortDirection.ASCENDING);
    }

    public static String getLatest(String... gavs) {
        return getFirstSorted(gavs, SortDirection.DESCENDING);
    }

    public static ReleaseId getLatest(List<ReleaseId> releaseIds) {
        return getFirstSorted(releaseIds, SortDirection.DESCENDING);
    }

    private static String getFirstSorted(String[] gavs, SortDirection sortDirection) {
        if (gavs != null && gavs.length > 0) {
            List<ReleaseId> releaseIds = new ArrayList<ReleaseId>();
            for (String gav : gavs) {
                if (gav != null) {
                    gav = gav.trim();
                    if (gav.length() > 0) {
                        releaseIds.add(new ComparableReleaseId(gav));
                    }
                }
            }
            ReleaseId releaseId = getFirstSorted(releaseIds, sortDirection);
            if (releaseId != null) {
                return releaseId.toExternalForm();
            }
        }
        return null;
    }

    private static ReleaseId getFirstSorted(List<ReleaseId> releaseIds, SortDirection sortDirection) {
        if (releaseIds != null && releaseIds.size() > 0) {
            releaseIds.sort(new ReleaseIdComparator(sortDirection));
            return releaseIds.get(0);
        }
        return null;
    }

    public static void main(String[] args) {
        boolean valid = main(args, System.out, System.err);
        if (!valid) {
            System.exit(1);
        }
    }

    // package-protected for JUnit testing
    static final String USAGE = "Usage: java " + ReleaseIdComparator.class.getName() + " <earliest|latest> <gav1> <gav2> ...\n";
    static boolean main(String[] args, PrintStream out, PrintStream err) {
        boolean valid = false;
        if (args != null && args.length > 0) {
            String op = args[0].trim().toLowerCase();
            if ("earliest".equals(op)) {
                String[] gavs = new String[args.length-1];
                System.arraycopy(args, 1, gavs, 0, gavs.length);
                out.print(getEarliest(gavs));
                valid = true;
            } else if ("latest".equals(op)) {
                String[] gavs = new String[args.length-1];
                System.arraycopy(args, 1, gavs, 0, gavs.length);
                out.print(getLatest(gavs));
                valid = true;
            }
        }
        if (!valid) {
            err.print(USAGE);
        }
        return valid;
    }

}
