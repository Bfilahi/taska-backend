package com.filahi.taska.service.impl;

import com.filahi.taska.entity.Project;
import com.filahi.taska.entity.Task;
import com.filahi.taska.entity.User;
import com.filahi.taska.enumeration.Status;
import com.filahi.taska.repository.ProjectRepository;
import com.filahi.taska.repository.TaskRepository;
import com.filahi.taska.request.TaskRequest;
import com.filahi.taska.response.TaskResponse;
import com.filahi.taska.service.TaskService;
import com.filahi.taska.util.PageableUtil;
import com.filahi.taska.util.ProjectTaskCompletion;
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
    private final ProjectTaskCompletion projectTaskCompletion;


    public TaskServiceImpl(TaskRepository taskRepository, ProjectRepository projectRepository, PageableUtil pageableUtil, ProjectTaskCompletion projectTaskCompletion) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.pageableUtil = pageableUtil;
        this.projectTaskCompletion = projectTaskCompletion;
    }

    @Override
    public Page<TaskResponse> getAllTasks(User user, long projectId, int page, int size) {
        Pageable pageable = this.pageableUtil.getPageable(page, size, "", "");
        Page<Task> tasks = this.taskRepository.findAllByUserAndProject_Id(user, projectId, pageable);

        List<TaskResponse> taskResponses = tasks.stream().map(task -> buildTaskResponse(task, user)).toList();
        return new PageImpl<>(taskResponses, pageable, tasks.getTotalElements());
    }

    @Override
    public TaskResponse getTaskById(long taskId, long projectId, User user) {
        Task task = this.taskRepository.findByUserAndProject_IdAndId(user, projectId, taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        return buildTaskResponse(task, user);
    }

    @Override
    public Page<TaskResponse> getOverdueTasks(int page, int size, User user, long projectId) {
        Project project = this.projectRepository.findByUserAndId(user, projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        Pageable pageable = this.pageableUtil.getPageable(page, size, "", "");
        Page<Task> tasks = this.taskRepository.findByUserAndProject_IdAndStatusAndDueDateBefore(
                user, project.getId(), Status.ACTIVE, LocalDate.now(), pageable
        );

        List<TaskResponse> overdueTasks = tasks.stream().map(task -> buildTaskResponse(task, user)).toList();
        return new PageImpl<>(overdueTasks, pageable, tasks.getTotalElements());
    }

    @Override
    @Transactional
    public TaskResponse addTask(TaskRequest taskRequest, User user) {
        Project project = this.projectRepository.findByUserAndId(user, taskRequest.projectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        Task newTask = new Task(
                0,
                taskRequest.title(),
                taskRequest.description(),
                taskRequest.priority(),
                taskRequest.dueDate(),
                Status.ACTIVE,
                LocalDate.now(),
                user,
                project,
                new ArrayList<>(),
                new ArrayList<>()
        );

        this.taskRepository.save(newTask);
        this.projectTaskCompletion.handleProjectCompletion(project);
        return buildTaskResponse(newTask, user);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(TaskRequest taskRequest, User user, long projectId, long taskId) {
        Task task = this.taskRepository.findByUserAndProject_IdAndId(user, projectId, taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        Project project = this.projectRepository.findByUserAndId(user, taskRequest.projectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        task.setTitle(taskRequest.title());
        task.setDescription(taskRequest.description());
        task.setDueDate(taskRequest.dueDate());
        task.setPriority(taskRequest.priority());
        task.setStatus(taskRequest.status());
        task.setProject(project);

        this.taskRepository.save(task);
        this.projectTaskCompletion.handleProjectCompletion(project);
        return buildTaskResponse(task, user);
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

        List<TaskResponse> taskResponses = tasks.stream().map(task -> buildTaskResponse(task, user)).toList();
        return new PageImpl<>(taskResponses, pageable, tasks.getTotalElements());
    }

    @Override
    public TaskResponse toggleTaskCompletion(User user, long taskId, long projectId) {
        Project project = this.projectRepository.findByUserAndId(user, projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        Task task = this.taskRepository.findByUserAndProject_IdAndId(user, projectId, taskId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        if(task.getStatus().equals(Status.ACTIVE))
            task.setStatus(Status.COMPLETED);
        else
            task.setStatus(Status.ACTIVE);

        if(task.getStatus().equals(Status.COMPLETED))
            task.getSubtasks().forEach(taskSubtask -> taskSubtask.setStatus(Status.COMPLETED));
        else
            task.getSubtasks().forEach(taskSubtask -> taskSubtask.setStatus(Status.ACTIVE));

        this.taskRepository.save(task);
        this.projectTaskCompletion.handleProjectCompletion(project);
        return buildTaskResponse(task, user);
    }

    private static TaskResponse buildTaskResponse(Task task, User user) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getStatus(),
                task.getDueDate(),
                task.getProject().getId(),
                task.getSubtasks().size()
        );
    }
}
