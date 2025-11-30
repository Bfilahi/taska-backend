package com.filahi.taska.service.impl;

import com.filahi.taska.entity.Project;
import com.filahi.taska.entity.Task;
import com.filahi.taska.entity.User;
import com.filahi.taska.repository.ProjectRepository;
import com.filahi.taska.repository.TaskRepository;
import com.filahi.taska.request.ProjectRequest;
import com.filahi.taska.response.ProjectResponse;
import com.filahi.taska.response.ProjectStatsResponse;
import com.filahi.taska.response.ProjectsStatsResponse;
import com.filahi.taska.service.ProjectService;
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
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final PageableUtil pageableUtil;


    public ProjectServiceImpl(ProjectRepository projectRepository, TaskRepository taskRepository, PageableUtil pageableUtil) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.pageableUtil = pageableUtil;
    }


    @Override
    public Page<ProjectResponse> getAllProjects(int page, int size, User user) {
        Pageable pageable = this.pageableUtil.getPageable(page, size, "", "");
        Page<Project> projects = this.projectRepository.findByUser(user, pageable);

        List<ProjectResponse> projectResponses = projects.stream().map(ProjectServiceImpl::buildProjectsResponse).toList();
        return new PageImpl<>(projectResponses, pageable, projects.getTotalElements());
    }

    @Override
    public ProjectResponse getProjectById(User user, long projectId) {
        Project project = this.projectRepository.findByUserAndId(user, projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        return buildProjectsResponse(project);
    }

    @Override
    @Transactional
    public ProjectResponse addNewProject(User user, ProjectRequest request) {
        Project project = new Project(
                0,
                request.name(),
                request.description(),
                LocalDate.now(),
                false,
                request.priority(),
                user,
                new ArrayList<>()
        );
        // Due date should be provided by the user.
        this.projectRepository.save(project);
        return buildProjectsResponse(project);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(User user, ProjectRequest request, long projectId) {
        Project project = this.projectRepository.findByUserAndId(user, projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        project.setName(request.name());
        project.setDescription(request.description());
        project.setDueDate(request.dueDate());
        project.setPriority(request.priority());

        this.projectRepository.save(project);
        return buildProjectsResponse(project);
    }

    @Override
    @Transactional
    public void deleteProject(User user, long projectId) {
        Project project = this.projectRepository.findByUserAndId(user, projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        this.projectRepository.delete(project);
    }

    @Override
    public Page<ProjectResponse> searchProjects(User user, String keyword, int page, int size) {
        Pageable pageable = this.pageableUtil.getPageable(page, size, "", "");
        Page<Project> projects = this.projectRepository.searchByKeyword(user, keyword, pageable);

        List<ProjectResponse> projectResponses = projects.stream().map(ProjectServiceImpl::buildProjectsResponse).toList();
        return new PageImpl<>(projectResponses, pageable, projects.getTotalElements());
    }

    @Override
    public ProjectsStatsResponse getProjectsStats(User user) {
        long numProjects = this.projectRepository.countByUser(user);
        long numCompletedProjects = this.projectRepository.countByUserAndIsCompletedTrue(user);

        return new ProjectsStatsResponse(numProjects, numCompletedProjects);
    }

    @Override
    public ProjectStatsResponse getProjectStats(User user, long projectId) {
        long numTasks = this.taskRepository.countByUserAndProjectId(user, projectId);
        long numCompletedTasks = this.taskRepository.countByUserAndProjectIdAndIsCompletedTrue(user, projectId);

        return new ProjectStatsResponse(
                numTasks,
                numCompletedTasks,
                0
        );
        // Fix tasksInProgress in ProjectStatsResponse
    }

    private static ProjectResponse buildProjectsResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getDueDate(),
                project.getPriority(),
                project.isCompleted(),
                calculateProgress(project.getTasks())
        );
    }

    private static int calculateProgress(List<Task> tasks){
        int totalTasks = tasks.size();
        int completedTasks = tasks.stream().filter(Task::isCompleted).toList().size();
        if(totalTasks == 0)
            return 0;
        return (int)((float) completedTasks / totalTasks) * 100;
    }

}
