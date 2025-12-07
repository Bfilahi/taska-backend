package com.filahi.taska.service.impl;

import com.filahi.taska.entity.Project;
import com.filahi.taska.entity.Subtask;
import com.filahi.taska.entity.Task;
import com.filahi.taska.entity.User;
import com.filahi.taska.enumeration.Status;
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
                request.dueDate(),
                Status.ACTIVE,
                LocalDate.now(),
                request.priority(),
                user,
                new ArrayList<>()
        );
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
        project.setStatus(request.status());

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
        long numCompletedProjects = this.projectRepository.countByUserAndStatus(user, Status.COMPLETED);
        long numOverdueProjects = this.projectRepository.countByUserAndStatusAndDueDateBefore(user, Status.ACTIVE, LocalDate.now());
        return new ProjectsStatsResponse(numProjects, numCompletedProjects, numOverdueProjects);
    }

    @Override
    public ProjectStatsResponse getProjectStats(User user, long projectId) {
        long numTasks = this.taskRepository.countByUserAndProject_Id(user, projectId);
        long numCompletedTasks = this.taskRepository.countByUserAndProject_IdAndStatus(user, projectId, Status.COMPLETED);
        long numOverdueTasks = this.taskRepository.countByUserAndProject_IdAndStatusAndDueDateBefore(user, projectId, Status.ACTIVE, LocalDate.now());
        long numInProgressTasks = numTasks - numCompletedTasks - numOverdueTasks;

        return new ProjectStatsResponse(
                numTasks,
                numCompletedTasks,
                numInProgressTasks,
                numOverdueTasks
        );
    }

    @Override
    public Page<ProjectResponse> getOverdueProjects(int page, int size, User user) {
        Pageable pageable = this.pageableUtil.getPageable(page, size, "", "");
        Page<Project> projects = this.projectRepository.findByUserAndStatusAndDueDateBefore(user, Status.ACTIVE, LocalDate.now(), pageable);
        List<ProjectResponse> projectResponses = projects.stream().map(ProjectServiceImpl::buildProjectsResponse).toList();
        return new PageImpl<>(projectResponses, pageable, projects.getTotalElements());
    }

    private static ProjectResponse buildProjectsResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedAt(),
                project.getDueDate(),
                project.getPriority(),
                project.getStatus(),
                calculateProgress(project.getTasks())
        );
    }

    private static int calculateProgress(List<Task> tasks){
        if(tasks == null || tasks.isEmpty())
            return 0;

        int totalWork = 0;
        int completedWork = 0;

        for(Task task : tasks){
            List<Subtask> subtasks = task.getSubtasks();

            if(subtasks == null || subtasks.isEmpty()){
                totalWork++;
                if(task.getStatus().equals(Status.COMPLETED))
                    completedWork++;
            }
            else{
                totalWork += subtasks.size();
                completedWork += (int) subtasks.stream().filter(st -> st.getStatus().equals(Status.COMPLETED)).count();
            }
        }
        return totalWork == 0 ? 0 : (int) ((completedWork * 100.0) / totalWork);
    }
}
