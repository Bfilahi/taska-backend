package com.filahi.taska.service;

import com.filahi.taska.entity.User;
import com.filahi.taska.request.TaskRequest;
import com.filahi.taska.response.TaskResponse;
import org.springframework.data.domain.Page;

public interface TaskService {
    Page<TaskResponse> getAllTasks(User user, long projectId, int page, int size);
    TaskResponse getTaskById(long taskId, long projectId, User user);
    Page<TaskResponse> getOverdueTasks(int page, int size, User user, long projectId);
    TaskResponse addTask(TaskRequest taskRequest, User user);
    TaskResponse updateTask(TaskRequest taskRequest, User user, long projectId, long taskId);
    void deleteTask(User user, long projectId, long taskId);
    Page<TaskResponse> searchTasks(User user, String keyword, int page, int size);
    TaskResponse toggleTaskCompletion(User user, long taskId, long projectId);
}
