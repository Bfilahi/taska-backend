package com.filahi.taska.service;

import com.filahi.taska.entity.User;
import com.filahi.taska.request.SubtaskRequest;
import com.filahi.taska.response.SubtaskResponse;
import org.springframework.data.domain.Page;

public interface SubtaskService {
    Page<SubtaskResponse> getSubtaskByTaskId(int page, int size, User user, long taskId);
    SubtaskResponse getSubtask(User user, long subtaskId, long taskId);
    SubtaskResponse addNewSubtask(User user, SubtaskRequest subTaskRequest);
    SubtaskResponse updateSubtask(User user, SubtaskRequest subTaskRequest, long subtaskId);
    void deleteSubtask(User user, long subtaskId, long taskId);
}
