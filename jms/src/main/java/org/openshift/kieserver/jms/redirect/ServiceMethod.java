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
package org.openshift.kieserver.jms.redirect;

import java.util.ArrayList;
import java.util.List;

import org.kie.server.api.commands.DescriptorCommand;
import org.kie.server.api.marshalling.ModelWrapper;
import org.kie.server.api.model.Wrapped;

public final class ServiceMethod {

    private final String service;
    private final String method;
    private final int containerId;
    private final int processInstanceId;
    private final int processInstanceIds;
    private final int correlationKey;
    private final int taskInstanceId;
    private final int workItemId;

    public ServiceMethod(
            String service,
            String method,
            int containerId,
            int processInstanceId,
            int processInstanceIds,
            int correlationKey,
            int taskInstanceId,
            int workItemId) {
        this.service = service;
        this.method = method;
        this.containerId = containerId;
        this.processInstanceId = processInstanceId;
        this.processInstanceIds = processInstanceIds;
        this.correlationKey = correlationKey;
        this.taskInstanceId = taskInstanceId;
        this.workItemId = workItemId;
    }

    public String getContainerId(DescriptorCommand dc) {
        return getString(dc, containerId);
    }

    public boolean setContainerId(DescriptorCommand dc, String redirectContainerId) {
        return setArgument(dc, containerId, redirectContainerId);
    }

    public Long getProcessInstanceId(DescriptorCommand dc) {
        return getLong(dc, processInstanceId);
    }

    public List<Long> getProcessInstanceIds(DescriptorCommand dc) {
        return getLongs(dc, processInstanceIds);
    }

    public String getCorrelationKey(DescriptorCommand dc) {
        return getString(dc, correlationKey);
    }

    public Long getTaskInstanceId(DescriptorCommand dc) {
        return getLong(dc, taskInstanceId);
    }

    public Long getWorkItemId(DescriptorCommand dc) {
        return getLong(dc, workItemId);
    }

    private String getString(DescriptorCommand dc, int i) {
        Object o = getArgument(dc, i);
        return o != null ? String.valueOf(o) : null;
    }

    private Long getLong(DescriptorCommand dc, int i) {
        return toLong(getArgument(dc, i));
    }

    private List<Long> getLongs(DescriptorCommand dc, int i) {
        Object arg = getArgument(dc, i);
        if (arg instanceof List) {
            List<Long> longs = new ArrayList<Long>();
            for (Object o : (List<?>)arg) {
                Long l = toLong(o);
                if (l != null) {
                    longs.add(l);
                }
            }
            return longs;
        }
        return null;
    }

    private Long toLong(Object o) {
        if (o != null) {
            if (o instanceof Long) {
                return (Long)o;
            }
            if (o instanceof Number) {
                return Long.valueOf(((Number)o).longValue());
            }
            if (o instanceof String) {
                return Long.valueOf(Long.parseLong((String)o));
            }
        }
        return null;
    }

    private Object getArgument(DescriptorCommand dc, int index) {
        if (dc != null && index > -1) {
            validate(dc);
            List<Object> args = dc.getArguments();
            if (args.size() > index) {
                Object arg = args.get(index);
                if (arg instanceof Wrapped) {
                    return ((Wrapped<?>)arg).unwrap();
                }
                return arg;
            }
        }
        return null;
    }

    private boolean setArgument(DescriptorCommand dc, int index, Object arg) {
        if (dc != null && index > -1) {
            validate(dc);
            List<Object> args = dc.getArguments();
            if (args.size() > index) {
                if (args.get(index) instanceof Wrapped) {
                    arg = ModelWrapper.wrap(arg);
                }
                args.set(index, arg);
                return true;
            }
        }
        return false;
    }

    private void validate(DescriptorCommand dc) {
        if (!dc.getService().equals(service)) {
            throw new IllegalArgumentException(String.format("DescriptorCommand service mismatch: %s != %s", dc.getService(), service));
        }
        if (!dc.getMethod().equals(method)) {
            throw new IllegalArgumentException(String.format("DescriptorCommand method mismatch: %s != %s", dc.getMethod(), method));
        }
    }

    @Override
    public String toString() {
        StringBuilder format = new StringBuilder("%s: service=%s, method=%s");
        String args = toArguments();
        if (!args.isEmpty()) {
            format.append(", ");
            format.append(args);
        }
        return String.format(format.toString(), ServiceMethod.class.getSimpleName(), service, method);
    }

    String toArguments() {
        StringBuilder format = new StringBuilder();
        List<Object> args = new ArrayList<Object>();
        if (containerId > -1) {
            if (args.size() > 0) {
                format.append(", ");
            }
            format.append("containerId=%s");
            args.add(containerId);
        }
        if (processInstanceId > -1) {
            if (args.size() > 0) {
                format.append(", ");
            }
            format.append("processInstanceId=%s");
            args.add(processInstanceId);
        }
        if (processInstanceIds > -1) {
            if (args.size() > 0) {
                format.append(", ");
            }
            format.append("processInstanceIds=%s");
            args.add(processInstanceIds);
        }
        if (correlationKey > -1) {
            if (args.size() > 0) {
                format.append(", ");
            }
            format.append("correlationKey=%s");
            args.add(correlationKey);
        }
        if (taskInstanceId > -1) {
            if (args.size() > 0) {
                format.append(", ");
            }
            format.append("taskInstanceId=%s");
            args.add(taskInstanceId);
        }
        if (workItemId > -1) {
            if (args.size() > 0) {
                format.append(", ");
            }
            format.append("workItemId=%s");
            args.add(workItemId);
        }
        return String.format(format.toString(), args.toArray());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + containerId;
        result = prime * result + correlationKey;
        result = prime * result + ((method == null) ? 0 : method.hashCode());
        result = prime * result + processInstanceId;
        result = prime * result + processInstanceIds;
        result = prime * result + ((service == null) ? 0 : service.hashCode());
        result = prime * result + taskInstanceId;
        result = prime * result + workItemId;
        return result;
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
        ServiceMethod other = (ServiceMethod) obj;
        if (containerId != other.containerId) {
            return false;
        }
        if (correlationKey != other.correlationKey) {
            return false;
        }
        if (method == null) {
            if (other.method != null) {
                return false;
            }
        } else if (!method.equals(other.method)) {
            return false;
        }
        if (processInstanceId != other.processInstanceId) {
            return false;
        }
        if (processInstanceIds != other.processInstanceIds) {
            return false;
        }
        if (service == null) {
            if (other.service != null) {
                return false;
            }
        } else if (!service.equals(other.service)) {
            return false;
        }
        if (taskInstanceId != other.taskInstanceId) {
            return false;
        }
        if (workItemId != other.workItemId) {
            return false;
        }
        return true;
    }

}
