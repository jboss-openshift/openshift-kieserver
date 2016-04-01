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

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;

public class ComparableReleaseId implements ReleaseId, Comparable<ReleaseId> {

    private final ReleaseId releaseId;

    public ComparableReleaseId(String releaseId) {
        String[] gav = releaseId.split(":");
        this.releaseId = KieServices.Factory.get().newReleaseId(gav[0], gav[1], gav[2]);
    }

    public ComparableReleaseId(String groupId, String artifactId, String version) {
        this.releaseId = KieServices.Factory.get().newReleaseId(groupId, artifactId, version);
    }

    public ComparableReleaseId(ReleaseId releaseId) {
        this.releaseId = releaseId;
    }

    @Override
    public String getGroupId() {
        return releaseId.getGroupId();
    }

    @Override
    public String getArtifactId() {
        return releaseId.getArtifactId();
    }

    @Override
    public String getVersion() {
        return releaseId.getVersion();
    }

    @Override
    public String toExternalForm() {
        return releaseId.toExternalForm();
    }

    @Override
    public boolean isSnapshot() {
        return releaseId.isSnapshot();
    }

    @Override
    public String toString() {
        return releaseId.toString();
    }

    @Override
    public int compareTo(ReleaseId that) {
        if (this == that) {
            return 0;
        }
        ArtifactVersion thisVersion = new DefaultArtifactVersion(releaseId.toExternalForm());
        ArtifactVersion thatVersion = new DefaultArtifactVersion(that.toExternalForm());
        return thisVersion.compareTo(thatVersion);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ComparableReleaseId other = (ComparableReleaseId)obj;
        if (releaseId == null) {
            if (other.releaseId != null) {
                return false;
            }
        } else if (!releaseId.equals(other.releaseId)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((releaseId == null) ? 0 : releaseId.hashCode());
        return result;
    }

    public static ComparableReleaseId toComparable(ReleaseId releaseId) {
        if (releaseId instanceof ComparableReleaseId) {
            return (ComparableReleaseId)releaseId;
        } else {
            return new ComparableReleaseId(releaseId);
        }
    }

}
