package com.filahi.taska.service.impl;

import com.filahi.taska.entity.Project;
import com.filahi.taska.entity.Task;
import com.filahi.taska.entity.User;
import com.filahi.taska.enumeration.Priority;
import com.filahi.taska.repository.ProjectRepository;
import com.filahi.taska.repository.TaskRepository;
import com.filahi.taska.request.TaskRequest;
import com.filahi.taska.response.TaskResponse;
import com.filahi.taska.service.TaskService;
import com.filahi.taska.util.BuildTaskResponse;
import com.filahi.taska.util.PageableUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final PageableUtil pageableUtil;
    private final BuildTaskResponse buildTaskResponse;


    public TaskServiceImpl(TaskRepository taskRepository, ProjectRepository projectRepository, PageableUtil pageableUtil, BuildTaskResponse buildTaskResponse) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.pageableUtil = pageableUtil;
        this.buildTaskResponse = buildTaskResponse;
    }

    @Override
    public Page<TaskResponse> getAllTasks(User user, long projectId, int page, int size) {
        Pageable pageable = this.pageableUtil.getPageable(page, size, "", "");
        Page<Task> tasks = this.taskRepository.findAllByUserAndProjectId(user, projectId, pageable);

        List<TaskResponse> taskResponses = tasks.stream().map(task -> this.buildTaskResponse.buildTaskResponse(task, user)).toList();
        return new PageImpl<>(taskResponses, pageable, tasks.getTotalElements());
    }

    @Override
    public TaskResponse getTaskById(long taskId, long projectId, User user) {
        Task task = this.taskRepository.findByUserAndProject_IdAndId(user, projectId, taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        return this.buildTaskResponse.buildTaskResponse(task, user);
    }

    @Override
    @Transactional
    public TaskResponse addTask(TaskRequest taskRequest, User user) {
        Project project = this.projectRepository.findById(taskRequest.projectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        Task newTask = new Task(
                0,
                taskRequest.title(),
                taskRequest.description(),
                taskRequest.priority(),
                taskRequest.dueDate(),
                false,
                LocalDate.now(),
                user,
                project,
                new ArrayList<>(),
                new ArrayList<>()
        );

        this.taskRepository.save(newTask);
        return this.buildTaskResponse.buildTaskResponse(newTask, user);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(TaskRequest taskRequest, User user, long projectId, long taskId) {
        Task task = this.taskRepository.findByUserAndProject_IdAndId(user, projectId, taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        Project project = this.projectRepository.findById(taskRequest.projectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        task.setTitle(taskRequest.title());
        task.setDescription(taskRequest.description());
        task.setDueDate(taskRequest.dueDate());
        task.setPriority(taskRequest.priority());
        task.setProject(project);

        this.taskRepository.save(task);
        return this.buildTaskResponse.buildTaskResponse(task, user);
    }

    @Override
    @Transactional
    public void deleteTask(User user, long projectId, long taskId) {
        Task task = this.taskRepository.findByUserAndProject_IdAndId(user, projectId, taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        this.taskRepository.delete(task);
    }

    @Override
    public Page<TaskResponse> searchTasks(User user, String keyword, int page, int size) {
        Pageable pageable = this.pageableUtil.getPageable(page, size, "", "");
        Page<Task> tasks = this.taskRepository.findByKeyword(user, keyword, pageable);

        List<TaskResponse> taskResponses = tasks.stream().map(task -> this.buildTaskResponse.buildTaskResponse(task, user)).toList();
        return new PageImpl<>(taskResponses, pageable, tasks.getTotalElements());
    }

    @Override
    @Transactional
    public TaskResponse updatePriority(User user, long taskId, long projectId, Priority priority) {
        Task task = this.taskRepository.findByUserAndProject_IdAndId(user, projectId, taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        task.setPriority(priority);
        this.taskRepository.save(task);
        return this.buildTaskResponse.buildTaskResponse(task, user);
    }
}
