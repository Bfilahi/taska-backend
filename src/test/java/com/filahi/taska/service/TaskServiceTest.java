package com.filahi.taska.service;

import com.filahi.taska.entity.*;
import com.filahi.taska.enumeration.Priority;
import com.filahi.taska.enumeration.Status;
import com.filahi.taska.repository.ProjectRepository;
import com.filahi.taska.repository.TaskRepository;
import com.filahi.taska.request.TaskRequest;
import com.filahi.taska.response.TaskResponse;
import com.filahi.taska.service.impl.TaskServiceImpl;
import com.filahi.taska.util.PageableUtil;
import static org.junit.jupiter.api.Assertions. *;
import static org.mockito.Mockito.*;

import com.filahi.taska.util.ProjectTaskCompletion;
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

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {
    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private PageableUtil pageableUtil;

    @Mock
    private ProjectTaskCompletion projectTaskCompletion;

    @InjectMocks
    private TaskServiceImpl taskService;

    private User user;
    private Project project;
    private TaskRequest taskRequest;
    private final long PROJECT_ID = 1;
    private final long TASK_ID = 1;
    private final int PAGE = 0;
    private final int SIZE = 10;
    private Pageable pageable;
    private final String keyword = "mock-keyword";



    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1L);
        user.setFirstName("Mario");
        user.setLastName("Rossi");
        user.setEmail("mario.rossi@example.com");
        user.setPassword("encodedOldPassword");
        user.setAuthorities(List.of(new Authority("ROLE_USER")));

        pageable = PageRequest.of(PAGE, SIZE);

        project = new Project();
        project.setId(PROJECT_ID);

        taskRequest = new TaskRequest(
                "Task title",
                "Task description",
                Priority.HIGH,
                Status.ACTIVE,
                LocalDate.of(LocalDate.now().getYear() + 1, 10, 5),
                PROJECT_ID
        );
    }

    private Task buildMockTask(long taskId){
        Task task = new Task();
        task.setId(taskId);
        task.setTitle("Task-" + taskId);
        task.setProject(project);
        task.setSubtasks(new ArrayList<>());
        return task;
    }

    @DisplayName("Get all tasks test, success")
    @Test
    public void getAllTasksTest(){
        Task task1 = buildMockTask(1L);
        Task task2 = buildMockTask(2L);
        Page<Task> tasks = new PageImpl<>(List.of(task1, task2), pageable, 2);

        when(pageableUtil.getPageable(PAGE, SIZE, "", "")).thenReturn(pageable);
        when(taskRepository.findAllByUserAndProject_Id(user, PROJECT_ID, pageable)).thenReturn(tasks);

        Page<TaskResponse> result = taskService.getAllTasks(user, PROJECT_ID, PAGE, SIZE);

        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.getTotalElements(), "Result should have 2 elements");
    }

    @DisplayName("Get all tasks test, no tasks exist test")
    @Test
    public void getAllTasksNoTasksExistTest(){
        Page<Task> tasks = new PageImpl<>(List.of(), pageable, 0);

        when(pageableUtil.getPageable(PAGE, SIZE, "", "")).thenReturn(pageable);
        when(taskRepository.findAllByUserAndProject_Id(user, PROJECT_ID, pageable)).thenReturn(tasks);

        Page<TaskResponse> result = taskService.getAllTasks(user, PROJECT_ID, PAGE, SIZE);

        assertNotNull(result, "Result should not be null");
        assertEquals(0, result.getTotalElements(), "Result should have 0 elements");
    }

    @DisplayName("Get task by id, success")
    @Test
    public void getTaskByIdTest(){
        Task task = buildMockTask(1L);

        when(taskRepository.findByUserAndProject_IdAndId(user, PROJECT_ID, TASK_ID)).thenReturn(Optional.of(task));

        TaskResponse result = taskService.getTaskById(TASK_ID, PROJECT_ID, user);

        assertEquals(TASK_ID, result.id(), "Should return id of " + TASK_ID);
        assertEquals(task.getTitle(), result.title(), "Should return title of " + task.getTitle());
        assertEquals(task.getProject().getId(), result.projectId(), "Project should return project id of " + task.getProject().getId());
        assertEquals(task.getSubtasks().size(), result.subtasks(), "Number of subtasks should be " + task.getSubtasks().size());
    }

    @DisplayName("Get task by id, task not found")
    @Test
    public void getTaskByIdTaskNotFoundTest(){
        when(taskRepository.findByUserAndProject_IdAndId(user, PROJECT_ID, TASK_ID)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        assertThrows(ResponseStatusException.class, () -> taskService.getTaskById(TASK_ID, PROJECT_ID, user));
    }

    @DisplayName("Should return paged overdue tasks for valid user and project")
    @Test
    public void getOverdueTasksValidUserAndProjectReturnsPagedTasks(){
        Task task1 = buildMockTask(1L);
        Task task2 = buildMockTask(2L);

        Page<Task> tasks = new PageImpl<>(List.of(task1, task2), pageable, 2);

        when(projectRepository.findByUserAndId(user, PROJECT_ID)).thenReturn(Optional.of(project));
        when(pageableUtil.getPageable(PAGE, SIZE, "", "")).thenReturn(pageable);
        when(taskRepository.findByUserAndProject_IdAndStatusAndDueDateBefore(user, PROJECT_ID, Status.ACTIVE, LocalDate.now(), pageable))
                .thenReturn(tasks);

        Page<TaskResponse> result = taskService.getOverdueTasks(PAGE, SIZE, user, PROJECT_ID);

        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.getTotalElements(), "Result should have 2 elements");
    }

    @DisplayName("Should correctly map tasks to TaskResponse")
    @Test
    public void getOverdueTasksValidTasksMapsToTaskResponse(){
        Task task1 = buildMockTask(1L);

        Page<Task> tasks = new PageImpl<>(List.of(task1), pageable, 2);

        when(projectRepository.findByUserAndId(user, PROJECT_ID)).thenReturn(Optional.of(project));
        when(pageableUtil.getPageable(PAGE, SIZE, "", "")).thenReturn(pageable);
        when(taskRepository.findByUserAndProject_IdAndStatusAndDueDateBefore(user, PROJECT_ID, Status.ACTIVE, LocalDate.now(), pageable))
                .thenReturn(tasks);

        Page<TaskResponse> result = taskService.getOverdueTasks(PAGE, SIZE, user, PROJECT_ID);

        assertEquals(1, result.stream().findFirst().get().id(), "Should return id of 1");
        assertEquals("Task-1", result.stream().findFirst().get().title(), "Should return title of task-1");
    }

    @DisplayName("Should preserve total elements from repository in returned page")
    @Test
    public void getOverdueTasksPaginationPreservesTotalElements(){
        Task task1 = buildMockTask(1L);
        Task task2 = buildMockTask(2L);

        Page<Task> tasks = new PageImpl<>(List.of(task1, task2), pageable, 2);

        when(projectRepository.findByUserAndId(user, PROJECT_ID)).thenReturn(Optional.of(project));
        when(pageableUtil.getPageable(PAGE, SIZE, "", "")).thenReturn(pageable);
        when(taskRepository.findByUserAndProject_IdAndStatusAndDueDateBefore(user, PROJECT_ID, Status.ACTIVE, LocalDate.now(), pageable))
                .thenReturn(tasks);

        Page<TaskResponse> result = taskService.getOverdueTasks(PAGE, SIZE, user, PROJECT_ID);

        assertEquals(2, result.getTotalElements(), "Total elements should be 2");
    }

    @DisplayName("Should preserve pageable in returned page")
    @Test
    public void getOverdueTasksPaginationPreservesPageable(){
        Task task1 = buildMockTask(1L);
        Task task2 = buildMockTask(2L);

        Page<Task> tasks = new PageImpl<>(List.of(task1, task2), pageable, 2);

        when(projectRepository.findByUserAndId(user, PROJECT_ID)).thenReturn(Optional.of(project));
        when(pageableUtil.getPageable(PAGE, SIZE, "", "")).thenReturn(pageable);
        when(taskRepository.findByUserAndProject_IdAndStatusAndDueDateBefore(user, PROJECT_ID, Status.ACTIVE, LocalDate.now(), pageable))
                .thenReturn(tasks);

        Page<TaskResponse> result = taskService.getOverdueTasks(PAGE, SIZE, user, PROJECT_ID);

        assertEquals(pageable, result.getPageable(), "Pageable should match");
    }

    @DisplayName("Should throw an exception when project does not belong to user")
    @Test
    public void getOverdueTasksProjectNotOwnedByUserThrowsNotFound(){
        when(projectRepository.findByUserAndId(user, PROJECT_ID)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        assertThrows(ResponseStatusException.class, () -> taskService.getOverdueTasks(PAGE, SIZE, user, PROJECT_ID));
    }

    @DisplayName("Should create and save task with correct field values")
    @Test
    public void shouldCreateAndSaveTaskWithCorrectFieldValues() {
        when(projectRepository.findByUserAndId(user, PROJECT_ID)).thenReturn(Optional.of(project));

        taskService.addTask(taskRequest, user);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository, times(1)).save(captor.capture());

        Task savedTask = captor.getValue();
        assertAll(
                () -> assertEquals(taskRequest.title(), savedTask.getTitle()),
                () -> assertEquals(taskRequest.description(), savedTask.getDescription()),
                () -> assertEquals(taskRequest.priority(), savedTask.getPriority()),
                () -> assertEquals(Status.ACTIVE, savedTask.getStatus()),
                () -> assertEquals(LocalDate.now(), savedTask.getCreatedAt()),
                () -> assertEquals(user, savedTask.getUser()),
                () -> assertEquals(project, savedTask.getProject())
        );
    }

    @DisplayName("Should return correctly built TaskResponse")
    @Test
    public void shouldReturnCorrectlyBuiltTaskResponse() {
        when(projectRepository.findByUserAndId(user, PROJECT_ID)).thenReturn(Optional.of(project));

        TaskResponse result = taskService.addTask(taskRequest, user);

        assertAll(
                () -> assertEquals(taskRequest.title(), result.title()),
                () -> assertEquals(taskRequest.description(), result.description()),
                () -> assertEquals(taskRequest.priority(), result.priority()),
                () -> assertEquals(taskRequest.dueDate(), result.dueDate())
        );
    }

    @DisplayName("Should throw an exception when project not found")
    @Test
    public void shouldThrowExceptionWhenProjectNotFound() {
        when(projectRepository.findByUserAndId(user, PROJECT_ID))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        assertThrows(ResponseStatusException.class, () -> taskService.addTask(taskRequest, user));

        verify(taskRepository, never()).save(any());
    }

    @DisplayName("Should call handleProjectCompletion with correct project")
    @Test
    public void shouldCallHandleProjectCompletionWithCorrectProject() {
        when(projectRepository.findByUserAndId(user, PROJECT_ID)).thenReturn(Optional.of(project));

        taskService.addTask(taskRequest, user);

        verify(projectTaskCompletion, times(1)).handleProjectCompletion(project);
    }

    @DisplayName("Should update and return task response when request is valid")
    @Test
    public void updateTaskSuccess(){
        Task task = buildMockTask(1);

        when(taskRepository.findByUserAndProject_IdAndId(user, PROJECT_ID, TASK_ID)).thenReturn(Optional.of(task));
        when(projectRepository.findByUserAndId(user, PROJECT_ID)).thenReturn(Optional.of(project));

        TaskResponse result = taskService.updateTask(taskRequest, user, PROJECT_ID, TASK_ID);

        verify(taskRepository, times(1)).save(task);

        assertAll(
                () -> assertEquals(taskRequest.title(), result.title()),
                () -> assertEquals(taskRequest.description(), result.description()),
                () -> assertEquals(taskRequest.dueDate(), result.dueDate()),
                () -> assertEquals(taskRequest.priority(), result.priority()),
                () -> assertEquals(taskRequest.status(), result.status()),
                () -> assertEquals(taskRequest.projectId(), result.projectId())
        );
    }

    @DisplayName("Should throw an exception when task is not found")
    @Test
    public void updateTaskTaskNotFoundThrowsNotFound(){
        when(taskRepository.findByUserAndProject_IdAndId(user, PROJECT_ID, TASK_ID))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        assertThrows(ResponseStatusException.class, () -> taskService.updateTask(taskRequest, user, PROJECT_ID, TASK_ID));

        verify(taskRepository, never()).save(any());
    }

    @DisplayName("Should throw an exception when new project is not found")
    @Test
    public void updateTaskProjectNotFoundThrowsNotFound(){
        Task task = buildMockTask(1);

        when(taskRepository.findByUserAndProject_IdAndId(user, PROJECT_ID, TASK_ID)).thenReturn(Optional.of(task));
        when(projectRepository.findByUserAndId(user, PROJECT_ID))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        assertThrows(ResponseStatusException.class, () -> taskService.updateTask(taskRequest, user, PROJECT_ID, TASK_ID));

        verify(taskRepository, never()).save(any());
    }

    @DisplayName("Should call handleProjectCompletion even when project does not change")
    @Test
    public void updateTaskSameProjectCallsHandleProjectCompletion(){
        Task task = buildMockTask(1);

        when(taskRepository.findByUserAndProject_IdAndId(user, PROJECT_ID, TASK_ID)).thenReturn(Optional.of(task));
        when(projectRepository.findByUserAndId(user, PROJECT_ID)).thenReturn(Optional.of(project));

        taskService.updateTask(taskRequest, user, PROJECT_ID, TASK_ID);

        verify(taskRepository, times(1)).save(task);
        verify(projectTaskCompletion, times(1)).handleProjectCompletion(project);
    }

    @DisplayName("Should delete task when request is valid")
    @Test
    public void deleteTaskSuccess(){
        Task task = buildMockTask(1);

        when(taskRepository.findByUserAndProject_IdAndId(user, PROJECT_ID, TASK_ID)).thenReturn(Optional.of(task));

        taskService.deleteTask(user, PROJECT_ID, TASK_ID);

        verify(taskRepository, times(1)).delete(task);
    }

    @DisplayName("Should throw an exception when task is not found")
    @Test
    public void deleteTaskTaskNotFoundThrowsNotFound(){
        when(taskRepository.findByUserAndProject_IdAndId(user, PROJECT_ID, TASK_ID))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        assertThrows(ResponseStatusException.class, () -> taskService.deleteTask(user, PROJECT_ID, TASK_ID));

        verify(taskRepository, never()).delete(any());
    }

    @DisplayName("Should return page of task responses when tasks match keyword")
    @Test
    public void searchTasksSuccess(){
        Task task1 = buildMockTask(1);
        Task task2 = buildMockTask(2);

        Page<Task> tasks = new PageImpl<>(List.of(task1, task2), pageable, 2);

        when(pageableUtil.getPageable(PAGE, SIZE, "", "")).thenReturn(pageable);
        when(taskRepository.findByKeyword(user, keyword, pageable)).thenReturn(tasks);

        Page<TaskResponse> result = taskService.searchTasks(user, keyword, PAGE, SIZE);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
    }

    @DisplayName("Should return empty page when no tasks match keyword")
    @Test
    public void searchTasksNoResultsReturnsEmptyPage(){
        Page<Task> tasks = new PageImpl<>(List.of(), pageable, 0);

        when(pageableUtil.getPageable(PAGE, SIZE, "", "")).thenReturn(pageable);
        when(taskRepository.findByKeyword(user, keyword, pageable)).thenReturn(tasks);

        Page<TaskResponse> result = taskService.searchTasks(user, keyword, PAGE, SIZE);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @DisplayName("Should return page with correct pageable and total elements")
    @Test
    public void searchTasksReturnsCorrectPageMetadata(){
        Task task1 = buildMockTask(1);
        Task task2 = buildMockTask(2);

        Page<Task> tasks = new PageImpl<>(List.of(task1, task2), pageable, 2);

        when(pageableUtil.getPageable(PAGE, SIZE, "", "")).thenReturn(pageable);
        when(taskRepository.findByKeyword(user, keyword, pageable)).thenReturn(tasks);

        Page<TaskResponse> result = taskService.searchTasks(user, keyword, PAGE, SIZE);

        assertNotNull(result);
        assertEquals(pageable, result.getPageable());
    }

    @DisplayName("Should map each task to a task response")
    @Test
    public void searchTasksMapsEachTaskToTaskResponse(){
        Task task1 = buildMockTask(1);
        Task task2 = buildMockTask(2);

        Page<Task> tasks = new PageImpl<>(List.of(task1, task2), pageable, 2);

        when(pageableUtil.getPageable(PAGE, SIZE, "", "")).thenReturn(pageable);
        when(taskRepository.findByKeyword(user, keyword, pageable)).thenReturn(tasks);

        Page<TaskResponse> result = taskService.searchTasks(user, keyword, PAGE, SIZE);

        assertNotNull(result);
        assertEquals(task1.getId(), result.getContent().get(0).id());
        assertEquals(task2.getId(), result.getContent().get(1).id());
    }

    @DisplayName("Should toggle task from ACTIVE to COMPLETED successfully")
    @Test
    public void toggleTaskCompletionActiveToCompletedSuccess(){
        Task task = buildMockTask(1);
        task.setStatus(Status.ACTIVE);

        when(projectRepository.findByUserAndId(user, PROJECT_ID)).thenReturn(Optional.of(project));
        when(taskRepository.findByUserAndProject_IdAndId(user, PROJECT_ID, TASK_ID)).thenReturn(Optional.of(task));

        taskService.toggleTaskCompletion(user, TASK_ID, PROJECT_ID);

        verify(taskRepository, times(1)).save(task);
    }

    @DisplayName("Should toggle task from COMPLETED to ACTIVE successfully")
    @Test
    public void toggleTaskCompletionCompletedToActiveSuccess(){
        Task task = buildMockTask(1);
        task.setStatus(Status.ACTIVE);

        when(projectRepository.findByUserAndId(user, PROJECT_ID)).thenReturn(Optional.of(project));
        when(taskRepository.findByUserAndProject_IdAndId(user, PROJECT_ID, TASK_ID)).thenReturn(Optional.of(task));

        TaskResponse result = taskService.toggleTaskCompletion(user, TASK_ID, PROJECT_ID);

        assertEquals(Status.COMPLETED, result.status());
    }

    @DisplayName("Should throw an exception when project is not found")
    @Test
    public void toggleTaskCompletionProjectNotFoundThrowsNotFound(){
        when(projectRepository.findByUserAndId(user, PROJECT_ID))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        assertThrows(ResponseStatusException.class, () -> taskService.toggleTaskCompletion(user, TASK_ID, PROJECT_ID));

        verify(taskRepository, never()).save(any());
    }

    @DisplayName("Should throw an exception when task is not found")
    @Test
    public void toggleTaskCompletionTaskNotFoundThrowsNotFound(){
        when(projectRepository.findByUserAndId(user, PROJECT_ID)).thenReturn(Optional.of(project));
        when(taskRepository.findByUserAndProject_IdAndId(user, PROJECT_ID, TASK_ID))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        assertThrows(ResponseStatusException.class, () -> taskService.toggleTaskCompletion(user, TASK_ID, PROJECT_ID));

        verify(taskRepository, never()).save(any());
    }

    @DisplayName("Should call handleProjectCompletion with the correct project")
    @Test
    public void toggleTaskCompletionCallsHandleProjectCompletionWithCorrectProject(){
        Task task = buildMockTask(1);
        task.setStatus(Status.ACTIVE);

        when(projectRepository.findByUserAndId(user, PROJECT_ID)).thenReturn(Optional.of(project));
        when(taskRepository.findByUserAndProject_IdAndId(user, PROJECT_ID, TASK_ID)).thenReturn(Optional.of(task));

        taskService.toggleTaskCompletion(user, TASK_ID, PROJECT_ID);

        verify(projectTaskCompletion, times(1)).handleProjectCompletion(project);
    }

}
