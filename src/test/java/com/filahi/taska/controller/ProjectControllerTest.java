package com.filahi.taska.controller;

import com.filahi.taska.enumeration.Priority;
import com.filahi.taska.enumeration.Status;
import com.filahi.taska.request.ProjectRequest;
import com.filahi.taska.response.ProjectResponse;
import com.filahi.taska.response.ProjectStatsResponse;
import com.filahi.taska.response.ProjectsStatsResponse;
import com.filahi.taska.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    private final String BASE_URL = "/api/projects";
    private final int PAGE = 0;
    private final int SIZE = 10;
    private Pageable pageable;
    private ProjectRequest projectRequest;


    @BeforeEach
    public void setup() {
        projectRequest = new ProjectRequest(
                "Project name from request",
                "Project description",
                LocalDate.of(LocalDate.now().getYear() + 1, 10, 10),
                Priority.MEDIUM,
                Status.ACTIVE
        );

        pageable = PageRequest.of(PAGE, SIZE);
    }

    private ProjectResponse buildProject(long projectId){
        return new ProjectResponse(
                projectId,
                "Project-" + projectId + " name",
                "Project description",
                LocalDate.now(),
                LocalDate.of(LocalDate.now().getYear() + 2, 10, 10),
                Priority.MEDIUM,
                Status.ACTIVE,
                30
        );
    }

    @DisplayName("Should return all projects")
    @Test
    @WithMockUser
    public void getAllProjectsTest() throws Exception {
        ProjectResponse project1 = buildProject(1L);
        ProjectResponse project2 = buildProject(2L);
        ProjectResponse project3 = buildProject(3L);

        Page<ProjectResponse> projects = new PageImpl<>(List.of(project1, project2, project3), pageable, 3);

        when(projectService.getAllProjects(eq(PAGE), eq(SIZE), any())).thenReturn(projects);

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL)
                .param("page", Integer.toString(PAGE))
                .param("size", Integer.toString(SIZE))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3));
    }

    @DisplayName("Should return projects stats")
    @Test
    @WithMockUser
    public void getProjectsStatsTest() throws Exception {
        ProjectsStatsResponse response = new ProjectsStatsResponse(
                3L,
                2L,
                1L
        );

        when(projectService.getProjectsStats(any())).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProjects").value(3))
                .andExpect(jsonPath("$.completedProjects").value(2))
                .andExpect(jsonPath("$.overdueProjects").value(1));
    }

    @DisplayName("Should return stats of a specific project")
    @Test
    @WithMockUser
    public void getProjectStatsTest() throws Exception {
        ProjectStatsResponse response = new ProjectStatsResponse(
                4L,
                2L,
                1L,
                1L
        );

        when(projectService.getProjectStats(any(), eq(1L))).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/stats/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasks").value(4))
                .andExpect(jsonPath("$.completedTasks").value(2))
                .andExpect(jsonPath("$.tasksInProgress").value(1L))
                .andExpect(jsonPath("$.overdueTasks").value(1));
    }

    @DisplayName("Should return a specific project")
    @Test
    @WithMockUser
    public void getProjectByIdTest() throws Exception {
        ProjectResponse project = buildProject(1L);

        when(projectService.getProjectById(any(),  eq(1L))).thenReturn(project);

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/project/1"))
                .andExpect(status().isOk());
    }

    @DisplayName("Should throw an exception when no project is found")
    @Test
    @WithMockUser
    public void getProjectByIdNoProjectFoundTest() throws Exception {
        when(projectService.getProjectById(any(),  eq(1L)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/project/1"))
                .andExpect(status().isNotFound());
    }

    @DisplayName("Should return overdue projects")
    @Test
    @WithMockUser
    public void getOverdueProjectsTest() throws Exception {
        ProjectResponse project1 = buildProject(1L);
        ProjectResponse project2 = buildProject(2L);

        Page<ProjectResponse> projects = new PageImpl<>(List.of(project1, project2), pageable, 2);

        when(projectService.getOverdueProjects(eq(PAGE), eq(SIZE), any())).thenReturn(projects);

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/p-overdue")
                        .param("page", Integer.toString(PAGE))
                        .param("size", Integer.toString(SIZE))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @DisplayName("Should add new project and return it")
    @Test
    @WithMockUser
    public void addNewProjectTest() throws Exception {
        ProjectResponse project = buildProject(1L);

        when(projectService.addNewProject(any(), eq(projectRequest))).thenReturn(project);

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/new-project")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(projectRequest))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Project-1 name"));
    }

    @DisplayName("Should update existing project and return it")
    @Test
    @WithMockUser
    public void updateProjectTest() throws Exception {
        ProjectResponse project = buildProject(4L);

        when(projectService.updateProject(any(), eq(projectRequest), eq(4L))).thenReturn(project);

        mockMvc.perform(MockMvcRequestBuilders.put(BASE_URL + "/update/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(projectRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Project-4 name"));
    }

    @DisplayName("Should throw na exception when no project was found")
    @Test
    @WithMockUser
    public void updateProjectNoProjectFoundTest() throws Exception {
        when(projectService.updateProject(any(), eq(projectRequest), eq(4L)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        mockMvc.perform(MockMvcRequestBuilders.put(BASE_URL + "/update/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(projectRequest))
                )
                .andExpect(status().isNotFound());
    }

    @DisplayName("Should delete an existing project")
    @Test
    @WithMockUser
    public void deleteProjectTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/delete/{projectId}",4L))
                .andExpect(status().isOk());
    }

    @DisplayName("Should return projects that contain a specific keyword")
    @Test
    @WithMockUser
    public void searchProjectsTest() throws Exception {
        String keyword = "Mock-keyword";

        ProjectResponse project1 = buildProject(1L);
        ProjectResponse project2 = buildProject(2L);
        ProjectResponse project3 = buildProject(3L);
        ProjectResponse project4 = buildProject(4L);

        Page<ProjectResponse> projects = new PageImpl<>(List.of(project1, project2, project3, project4), pageable, 4);

        when(projectService.searchProjects(any(), eq(keyword), eq(PAGE), eq(SIZE))).thenReturn(projects);

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/project/search/{keyword}", keyword)
                        .param("page", Integer.toString(PAGE))
                        .param("size", Integer.toString(SIZE))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(4));
    }
}
