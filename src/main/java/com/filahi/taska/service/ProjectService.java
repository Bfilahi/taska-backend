package com.filahi.taska.service;

import com.filahi.taska.entity.User;
import com.filahi.taska.request.ProjectRequest;
import com.filahi.taska.response.ProjectResponse;
import com.filahi.taska.response.ProjectStatsResponse;
import com.filahi.taska.response.ProjectsStatsResponse;
import org.springframework.data.domain.Page;

public interface ProjectService {
    Page<ProjectResponse> getAllProjects(int page, int size, User user);
    ProjectResponse getProjectById(User user, long projectId);
    ProjectResponse addNewProject(User user, ProjectRequest request);
    ProjectResponse updateProject(User user, ProjectRequest request, long projectId);
    void deleteProject(User user, long projectId);
    Page<ProjectResponse> searchProjects(User user, String keyword, int page, int size);
    ProjectsStatsResponse getProjectsStats(User user);
//    ProjectStatsResponse getProjectStats(User user, long projectId);
}
