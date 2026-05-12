package com.filahi.taska.service;

import com.filahi.taska.entity.*;
import com.filahi.taska.enumeration.Priority;
import com.filahi.taska.enumeration.Status;
import com.filahi.taska.repository.ProjectRepository;
import com.filahi.taska.repository.TaskRepository;
import com.filahi.taska.request.ProjectRequest;
import com.filahi.taska.response.ProjectResponse;
import com.filahi.taska.response.ProjectStatsResponse;
import com.filahi.taska.response.ProjectsStatsResponse;
import com.filahi.taska.service.impl.ProjectServiceImpl;
import com.filahi.taska.util.PageableUtil;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {


    @Mock
    private PageableUtil pageableUtil;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private User user;
    private final long PROJECT_ID = 1;
    private final int PAGE = 0;
    private final int SIZE = 10;
    private final String keyword = "mock-keyword";
    private Pageable pageable;
    private ProjectRequest projectRequest;


    @BeforeEach
    public void setUp(){
        user = new User();
        user.setId(1L);
        user.setFirstName("Mario");
        user.setLastName("Rossi");
        user.setEmail("mario.rossi@example.com");
        user.setPassword("encodedOldPassword");
        user.setAuthorities(List.of(new Authority("ROLE_USER")));

        pageable = PageRequest.of(PAGE, SIZE);

        projectRequest = new ProjectRequest(
                "Project name",
                "Project description",
                LocalDate.of(LocalDate.now().getYear() + 1, 10, 10),
                Priority.HIGH,
                Status.ACTIVE
        );
    }

    private Project buildMockProject(long projectId){
        Project project = new Project();
        project.setId(projectId);
        project.setName("Project-" + projectId);

        return project;
    }


    @DisplayName("Get all projects test, success")
    @Test
    public void getAllProjectsTest(){
        Project project1 = buildMockProject(1L);
        Project project2 = buildMockProject(2L);

        Page<Project> projects = new PageImpl<>(List.of(project1,project2), pageable, 2);

        when(pageableUtil.getPageable(PAGE, SIZE, "", "")).thenReturn(pageable);
        when(projectRepository.findByUser(user, pageable)).thenReturn(projects);

        Page<ProjectResponse> result = projectService.getAllProjects(PAGE, SIZE, user);

        assertNotNull(result);
        assertEquals(2, projects.getTotalElements());
    }

    @DisplayName("Get all projects test, no Projects exist test")
    @Test
    public void getAllProjectsNoProjectsExistTest(){
        Page<Project> projects = new PageImpl<>(List.of(), pageable, 0);

        when(pageableUtil.getPageable(PAGE, SIZE, "", "")).thenReturn(pageable);
        when(projectRepository.findByUser(user, pageable)).thenReturn(projects);

        Page<ProjectResponse> result = projectService.getAllProjects(PAGE, SIZE, user);

        assertNotNull(result);
        assertEquals(0, projects.getTotalElements());
    }

    @DisplayName("Get project by id, success")
    @Test
    public void getProjectByIdTest(){
        Project project = buildMockProject(1L);

        when(projectRepository.findByUserAndId(user, project.getId())).thenReturn(Optional.of(project));

        ProjectResponse result = projectService.getProjectById(user, project.getId());

        assertEquals(project.getId(), result.id());
        assertEquals(project.getName(), result.name());
    }

    @DisplayName("Get project by id, project not found")
    @Test
    public void getProjectByIdProjectNotFoundTest(){
        when(projectRepository.findByUserAndId(user, PROJECT_ID))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        assertThrows(ResponseStatusException.class, () -> projectService.getProjectById(user, PROJECT_ID));
    }

    @DisplayName("Should return ProjectResponse when user and projectRequest are valid")
    @Test
    public void addNewProjectValidUserAndRequestReturnsProjectResponse(){
        projectService.addNewProject(user, projectRequest);

        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(captor.capture());

        Project project =  captor.getValue();
        assertAll(
                () -> assertEquals(0, project.getId()),
                () -> assertEquals(projectRequest.name(), project.getName()),
                () -> assertEquals(projectRequest.dueDate(), project.getDueDate()),
                () -> assertEquals(Status.ACTIVE, project.getStatus()),
                () -> assertEquals(LocalDate.now(), project.getCreatedAt()),
                () -> assertEquals(projectRequest.priority(), project.getPriority()),
                () -> assertEquals(user, project.getUser()),
                () -> assertEquals(new ArrayList<>(), project.getTasks())
        );
    }

    @DisplayName("Should update and return project response when request is valid")
    @Test
    public void updateProjectSuccess(){
        Project project = buildMockProject(PROJECT_ID);

        when(projectRepository.findByUserAndId(user, PROJECT_ID)).thenReturn(Optional.of(project));

        ProjectResponse result = projectService.updateProject(user, projectRequest, PROJECT_ID);

        verify(projectRepository).save(project);

        assertAll(
                () -> assertEquals(projectRequest.name(), result.name()),
                () -> assertEquals(projectRequest.description(), result.description()),
                () -> assertEquals(projectRequest.dueDate(), result.dueDate()),
                () -> assertEquals(projectRequest.priority(), result.priority()),
                () -> assertEquals(projectRequest.status(), result.status())
        );
    }

    @DisplayName("Should throw an exception when project is not found")
    @Test
    public void updateProjectProjectNotFoundThrowsNotFound(){
        when(projectRepository.findByUserAndId(user, PROJECT_ID))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        assertThrows(ResponseStatusException.class, () -> projectService.updateProject(user, projectRequest, PROJECT_ID));

        verify(projectRepository, never()).save(any());
    }

    @DisplayName("Should delete project when request is valid")
    @Test
    public void deleteProjectSuccess(){
        Project project = buildMockProject(PROJECT_ID);

        when(projectRepository.findByUserAndId(user, PROJECT_ID)).thenReturn(Optional.of(project));

        projectService.deleteProject(user, PROJECT_ID);

        verify(projectRepository, times(1)).delete(project);
    }

    @DisplayName("Should throw an exception when project is not found")
    @Test
    public void deleteProjectProjectNotFoundThrowsNotFound(){
        when(projectRepository.findByUserAndId(user, PROJECT_ID))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        assertThrows(ResponseStatusException.class, () -> projectService.deleteProject(user, PROJECT_ID));

        verify(projectRepository, never()).delete(any());
    }

    @DisplayName("Should return page of project responses when projects match keyword")
    @Test
    public void searchProjectsSuccess(){
        Project project1 = buildMockProject(1);
        Project project2 = buildMockProject(2);

        Page<Project> projects = new PageImpl<>(List.of(project1, project2), pageable, 2);

        when(pageableUtil.getPageable(PAGE, SIZE, "", "")).thenReturn(pageable);
        when(projectRepository.searchByKeyword(user, keyword, pageable)).thenReturn(projects);

        Page<ProjectResponse> result = projectService.searchProjects(user, keyword, PAGE, SIZE);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
    }

    @DisplayName("Should return empty page when no projects match keyword")
    @Test
    public void searchProjectsNoResultsReturnsEmptyPage(){
        Page<Project> projects = new PageImpl<>(List.of(), pageable, 0);

        when(pageableUtil.getPageable(PAGE, SIZE, "", "")).thenReturn(pageable);
        when(projectRepository.searchByKeyword(user, keyword, pageable)).thenReturn(projects);

        Page<ProjectResponse> result = projectService.searchProjects(user, keyword, PAGE, SIZE);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @DisplayName("Should return page with correct pageable and total elements")
    @Test
    public void searchProjectsReturnsCorrectPageMetadata(){
        Project project1 = buildMockProject(1);
        Project project2 = buildMockProject(2);

        Page<Project> projects = new PageImpl<>(List.of(project1, project2), pageable, 2);

        when(pageableUtil.getPageable(PAGE, SIZE, "", "")).thenReturn(pageable);
        when(projectRepository.searchByKeyword(user, keyword, pageable)).thenReturn(projects);

        Page<ProjectResponse> result = projectService.searchProjects(user, keyword, PAGE, SIZE);

        assertNotNull(result);
        assertEquals(pageable, result.getPageable());
    }

    @DisplayName("Should map each project to a project response")
    @Test
    public void searchProjectsMapsEachProjectToProjectResponse(){
        Project project1 = buildMockProject(1);
        Project project2 = buildMockProject(2);

        Page<Project> projects = new PageImpl<>(List.of(project1, project2), pageable, 2);

        when(pageableUtil.getPageable(PAGE, SIZE, "", "")).thenReturn(pageable);
        when(projectRepository.searchByKeyword(user, keyword, pageable)).thenReturn(projects);

        Page<ProjectResponse> result = projectService.searchProjects(user, keyword, PAGE, SIZE);

        assertNotNull(result);
        assertEquals(project1.getId(), result.getContent().get(0).id());
        assertEquals(project2.getId(), result.getContent().get(1).id());
    }

    @DisplayName("Returns correct stats when user has projects")
    @Test
    public void returnsCorrectStatsWhenUserHasProjects() {
        when(projectRepository.countByUser(user)).thenReturn(3L);
        when(projectRepository.countByUserAndStatus(user, Status.COMPLETED)).thenReturn(2L);
        when(projectRepository.countByUserAndStatusAndDueDateBefore(user, Status.ACTIVE, LocalDate.now())).thenReturn(1L);

        ProjectsStatsResponse result = projectService.getProjectsStats(user);

        assertEquals(3, result.totalProjects());
        assertEquals(2, result.completedProjects());
        assertEquals(1, result.overdueProjects());
    }

    @Test
    @DisplayName("Returns all zeros when user has no projects")
    public void returnsAllZerosWhenUserHasNoProjects() {
        when(projectRepository.countByUser(user)).thenReturn(0L);
        when(projectRepository.countByUserAndStatus(user, Status.COMPLETED)).thenReturn(0L);
        when(projectRepository.countByUserAndStatusAndDueDateBefore(user, Status.ACTIVE, LocalDate.now())).thenReturn(0L);

        ProjectsStatsResponse result = projectService.getProjectsStats(user);

        assertEquals(0, result.totalProjects());
        assertEquals(0, result.completedProjects());
        assertEquals(0, result.overdueProjects());
    }

    @Test
    @DisplayName("Returns correct stats when user has tasks in project")
    public void returnsCorrectStatsWhenUserHasTasksInProject() {
        when(taskRepository.countByUserAndProject_Id(user, PROJECT_ID)).thenReturn(3L);
        when(taskRepository.countByUserAndProject_IdAndStatus(user, PROJECT_ID, Status.COMPLETED)).thenReturn(2L);
        when(taskRepository.countByUserAndProject_IdAndStatusAndDueDateBefore(user, PROJECT_ID, Status.ACTIVE, LocalDate.now())).thenReturn(1L);

        ProjectStatsResponse result = projectService.getProjectStats(user, PROJECT_ID);

        assertEquals(3, result.totalTasks());
        assertEquals(2, result.completedTasks());
        assertEquals(1, result.overdueTasks());
    }

    @Test
    @DisplayName("Returns all zeros when user has no tasks in project")
    public void returnsAllZerosWhenUserHasNoTasksInProject() {
        when(taskRepository.countByUserAndProject_Id(user, PROJECT_ID)).thenReturn(0L);
        when(taskRepository.countByUserAndProject_IdAndStatus(user, PROJECT_ID, Status.COMPLETED)).thenReturn(0L);
        when(taskRepository.countByUserAndProject_IdAndStatusAndDueDateBefore(user, PROJECT_ID, Status.ACTIVE, LocalDate.now())).thenReturn(0L);

        ProjectStatsResponse result = projectService.getProjectStats(user, PROJECT_ID);

        assertEquals(0, result.totalTasks());
        assertEquals(0, result.completedTasks());
        assertEquals(0, result.overdueTasks());
    }

    @DisplayName("Should return paged overdue projects for valid user and project")
    @Test
    public void getOverdueProjectsValidUserAndProjectReturnsPagedProjects(){
        Project project1 = buildMockProject(1L);
        Project project2 = buildMockProject(2L);

        Page<Project> projects = new PageImpl<>(List.of(project1, project2), pageable, 2);

        when(pageableUtil.getPageable(PAGE, SIZE, "", "")).thenReturn(pageable);
        when(projectRepository.findByUserAndStatusAndDueDateBefore(user, Status.ACTIVE, LocalDate.now(), pageable)).thenReturn(projects);

        Page<ProjectResponse> result = projectService.getOverdueProjects(PAGE, SIZE, user);

        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.getTotalElements(), "Result should have 2 elements");
    }

    @DisplayName("Should preserve pageable in returned page")
    @Test
    public void getOverdueProjectsPaginationPreservesPageable(){
        Project project1 = buildMockProject(1L);
        Project project2 = buildMockProject(2L);

        Page<Project> projects = new PageImpl<>(List.of(project1, project2), pageable, 2);

        when(pageableUtil.getPageable(PAGE, SIZE, "", "")).thenReturn(pageable);
        when(projectRepository.findByUserAndStatusAndDueDateBefore(user, Status.ACTIVE, LocalDate.now(), pageable))
                .thenReturn(projects);

        Page<ProjectResponse> result = projectService.getOverdueProjects(PAGE, SIZE, user);

        assertEquals(pageable, result.getPageable(), "Pageable should match");
    }

}
