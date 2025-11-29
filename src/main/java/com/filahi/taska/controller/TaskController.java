package com.filahi.taska.controller;

import com.filahi.taska.entity.User;
import com.filahi.taska.enumeration.Priority;
import com.filahi.taska.request.TaskRequest;
import com.filahi.taska.response.TaskResponse;
import com.filahi.taska.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Task REST API Endpoints", description = "Operations related to tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "Get all project's tasks", description = "Get a list of all project's tasks providing a project ID")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{projectId}")
    public Page<TaskResponse> getAllTasks(@AuthenticationPrincipal User user,
                                          @PathVariable long projectId,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "7") int size) {
        return this.taskService.getAllTasks(user, projectId, page, size);
    }
    // Instead of getAllTasks(), you should have a getTasksForProject().

    @Operation(summary = "Get a task", description = "Get a task by id")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/task/{taskId}")
    public TaskResponse getTaskById(@PathVariable long taskId,
                                    @RequestParam long projectId,
                                    @AuthenticationPrincipal User user) {
        return this.taskService.getTaskById(taskId, projectId, user);
    }

    @Operation(summary = "Add new task", description = "Add new task to the database")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/new-task")
    public TaskResponse addTask(@Valid @RequestBody TaskRequest taskRequest,
                                @AuthenticationPrincipal User user) {
        return this.taskService.addTask(taskRequest, user);
    }

    @Operation(summary = "Update task", description = "Update existing task")
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/update/{taskId}")
    public TaskResponse updateTask(@Valid @RequestBody TaskRequest taskRequest,
                                   @AuthenticationPrincipal User user,
                                   @PathVariable long taskId) {
        return this.taskService.updateTask(taskRequest, user, taskRequest.projectId(), taskId);
    }

    @Operation(summary = "Delete task", description = "Delete task from database")
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/delete/{taskId}")
    public void deleteTask(@AuthenticationPrincipal User user,
                           @RequestParam long projectId,
                           @PathVariable long taskId){
        this.taskService.deleteTask(user, projectId, taskId);
    }

    @Operation(summary = "Search task", description = "Search task based on keyword")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/task/search/{keyword}")
    public Page<TaskResponse> searchTasks(@AuthenticationPrincipal User user,
                                          @PathVariable String keyword,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "7") int size){
        return this.taskService.searchTasks(user, keyword, page, size);
    }

    @Operation(summary = "Update task priority", description = "Set the priority level for a task")
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{taskId}/priority")
    public TaskResponse updatePriority(@AuthenticationPrincipal User user,
                                       @PathVariable long taskId,
                                       @RequestParam long projectId,
                                       @RequestParam Priority priority){
        return this.taskService.updatePriority(user, taskId, projectId, priority);
    }
}
