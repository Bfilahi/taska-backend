package com.filahi.taska.util;

import com.filahi.taska.entity.Task;
import com.filahi.taska.entity.User;
import com.filahi.taska.response.TaskResponse;
import org.springframework.stereotype.Component;


@Component
public class BuildTaskResponse {

    public TaskResponse buildTaskResponse(Task task, User user) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getDueDate(),
                task.isCompleted(),
                task.getProject().getId()
        );
    }
}
