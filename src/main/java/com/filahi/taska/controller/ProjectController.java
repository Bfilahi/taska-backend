package com.filahi.taska.controller;

import com.filahi.taska.entity.User;
import com.filahi.taska.request.ProjectRequest;
import com.filahi.taska.response.ProjectResponse;
import com.filahi.taska.response.ProjectStatsResponse;
import com.filahi.taska.response.ProjectsStatsResponse;
import com.filahi.taska.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/projects")
@Tag(name = "Project REST API Endpoints", description = "Operations related to projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }


    @Operation(summary = "Get all projects", description = "Get a list of authenticated user's projects")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public Page<ProjectResponse> getAllProjects(@AuthenticationPrincipal User user,
                                                @RequestParam(required = false, defaultValue = "0") int page,
                                                @RequestParam(required = false, defaultValue = "7") int size){
        return this.projectService.getAllProjects(page, size, user);
    }

    @Operation(summary = "Get projects stats", description = "Retrieve projects stats")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/stats")
    public ProjectsStatsResponse getProjectsStats(@AuthenticationPrincipal User user){
        return this.projectService.getProjectsStats(user);
    }

//    @Operation(summary = "Get project stats", description = "Retrieve project stats providing project ID")
//    @ResponseStatus(HttpStatus.OK)
//    @GetMapping("/stats/{projectId}")
//    public ProjectStatsResponse getProjectStats(@AuthenticationPrincipal User user,
//                                                @PathVariable long projectId){
//        return this.projectService.getProjectStats(user, projectId);
//    }

    @Operation(summary = "Get a project", description = "Get a project provided project ID")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/project/{projectId}")
    public ProjectResponse getProjectById(@PathVariable long projectId,
                                          @AuthenticationPrincipal User user){
        return this.projectService.getProjectById(user, projectId);
    }

    @Operation(summary = "Add new project", description = "Add new project to database")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/new-project")
    public ProjectResponse addNewProject(@AuthenticationPrincipal User user,
                                         @Valid @RequestBody ProjectRequest request){
        return this.projectService.addNewProject(user, request);
    }

    @Operation(summary = "Update task", description = "Update a task for authenticated user")
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/update/{projectId}")
    public ProjectResponse updateProject(@Valid @RequestBody ProjectRequest request,
                                         @AuthenticationPrincipal User user,
                                         @PathVariable long projectId){
        return this.projectService.updateProject(user, request, projectId);
    }

    @Operation(summary = "Delete task", description = "Delete a task from database")
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/delete/{projectId}")
    public void deleteProject(@AuthenticationPrincipal User user,
                              @PathVariable long projectId){
        this.projectService.deleteProject(user, projectId);
    }

    @Operation(summary = "Search projects", description = "Search for projects given keyword")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/project/search/{keyword}")
    public Page<ProjectResponse> searchProjects(@PathVariable String keyword,
                                                @AuthenticationPrincipal User user,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "7") int size){
        return this.projectService.searchProjects(user, keyword, page, size);
    }
}
