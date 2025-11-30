package com.filahi.taska.controller;

import com.filahi.taska.entity.User;
import com.filahi.taska.request.SubtaskRequest;
import com.filahi.taska.response.SubtaskResponse;
import com.filahi.taska.service.SubtaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/subtasks")
@Tag(name = "Subtask REST API Endpoints", description = "Operations related to subtask")
public class SubtaskController {
    private final SubtaskService subtaskService;

    public SubtaskController(SubtaskService subtaskService) {
        this.subtaskService = subtaskService;
    }


    @Operation(summary = "Get subtasks for a given task", description = "Get all subtasks for a given task given task ID")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{taskId}/subtasks")
    public Page<SubtaskResponse> getSubtaskByTaskId(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "7") int size,
                                                    @AuthenticationPrincipal User user,
                                                    @PathVariable long taskId) {
        return this.subtaskService.getSubtaskByTaskId(page, size, user, taskId);
    }

    @Operation(summary = "Get subtask", description = "Get a subtask given subtask ID and task ID")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/subtask/{subtaskId}")
    public SubtaskResponse getSubtask(@AuthenticationPrincipal User user,
                                      @PathVariable long subtaskId,
                                      @RequestParam long taskId) {
        return this.subtaskService.getSubtask(user, subtaskId, taskId);
    }

    @Operation(summary = "Add new subtask for a given task", description = "Add new subtask for a given task to database")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/new-subtask")
    public SubtaskResponse addNewSubtask(@AuthenticationPrincipal User user,
                                         @Valid @RequestBody SubtaskRequest subtaskRequest) {
        return this.subtaskService.addNewSubtask(user, subtaskRequest);
    }

    @Operation(summary = "Update subtask for a given task", description = "Update subtask for a given task given task and subtask IDs")
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{subtaskId}")
    public SubtaskResponse updateSubtask(@AuthenticationPrincipal User user,
                                         @Valid @RequestBody SubtaskRequest subtaskRequest,
                                         @PathVariable long subtaskId) {
        return this.subtaskService.updateSubtask(user, subtaskRequest, subtaskId);
    }

    @Operation(summary = "Delete a subtask", description = "Delete a subtask from database")
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{subtaskId}")
    public void deleteSubtask(@AuthenticationPrincipal User user,
                              @PathVariable long subtaskId,
                              @RequestParam long taskId) {
        this.subtaskService.deleteSubtask(user, subtaskId, taskId);
    }
}
