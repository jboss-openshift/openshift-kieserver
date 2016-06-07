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
package org.openshift.kieserver.jms.redirect.services;

import java.util.List;

import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.jbpm.UserTaskServiceBase;
import org.openshift.kieserver.jms.redirect.RedirectIndex;

public class UserTaskService extends UserTaskServiceBase {

    private UserTaskService(org.jbpm.services.api.UserTaskService userTaskService, KieServerRegistry context) {
        super(userTaskService, context);
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public void activate(String containerId, Number taskId, String userId) {
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public void claim(String containerId, Number taskId, String userId) {
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public void complete(String containerId, Number taskId, String userId, String payload, String marshallerType) {
    }

    //@Override exists in 6.4.0 but not 6.3.0
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public void completeAutoProgress(String containerId, Number taskId, String userId, String payload, String marshallerType) {
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public void delegate(String containerId, Number taskId, String userId, String targetUserId) {
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public void exit(String containerId, Number taskId, String userId) {
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public void fail(String containerId, Number taskId, String userId, String payload, String marshallerType) {
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public void forward(String containerId, Number taskId, String userId, String targetUserId) {
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public void release(String containerId, Number taskId, String userId) {
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public void resume(String containerId, Number taskId, String userId) {
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public void skip(String containerId, Number taskId, String userId) {
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public void start(String containerId, Number taskId, String userId) {
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public void stop(String containerId, Number taskId, String userId) {
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public void suspend(String containerId, Number taskId, String userId) {
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public void nominate(String containerId, Number taskId, String userId, List<String> potentialOwners) {
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public void setPriority(String containerId, Number taskId, String priorityPayload, String marshallingType) {
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public void setExpirationDate(String containerId, Number taskId, String datePayload, String marshallingType) {
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public void setSkipable(String containerId, Number taskId, String skipablePayload, String marshallingType) {
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public void setName(String containerId, Number taskId, String namePayload, String marshallingType) {
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public void setDescription(String containerId, Number taskId, String descriptionPayload, String marshallingType) {
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public String saveContent(String containerId, Number taskId, String payload, String marshallingType) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public String getTaskOutputContentByTaskId(String containerId, Number taskId, String marshallingType) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public String getTaskInputContentByTaskId(String containerId, Number taskId, String marshallingType) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public void deleteContent(String containerId, Number taskId, Number contentId) {
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public String addComment(String containerId, Number taskId, String payload, String marshallingType) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public void deleteComment(String containerId, Number taskId, Number commentId) {
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public String getCommentsByTaskId(String containerId, Number taskId, String marshallingType) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public String getCommentById(String containerId, Number taskId, Number commentId, String marshallingType) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public String addAttachment(String containerId, Number taskId, String userId, String name, String attachmentPayload, String marshallingType) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public void deleteAttachment(String containerId, Number taskId, Number attachmentId) {
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public String getAttachmentById(String containerId, Number taskId, Number attachmentId, String marshallingType) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public String getAttachmentContentById(String containerId, Number taskId, Number attachmentId, String marshallingType) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public String getAttachmentsByTaskId(String containerId, Number taskId, String marshallingType) {
        return null;
    }

    @Override
    @RedirectIndex(containerId=0, taskInstanceId=1)
    public String getTask(String containerId, Number taskId, boolean withInput, boolean withOutput, boolean withAssignments, String marshallingType) {
        return null;
    }

}
