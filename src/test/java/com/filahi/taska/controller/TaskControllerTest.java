package com.filahi.taska.controller;


import com.filahi.taska.entity.Task;
import com.filahi.taska.enumeration.Priority;
import com.filahi.taska.enumeration.Status;
import com.filahi.taska.request.TaskRequest;
import com.filahi.taska.response.TaskResponse;
import com.filahi.taska.service.TaskService;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private Task task;

    @MockitoBean
    private TaskService taskService;


    private final String BASE_URL = "/api/tasks";
    private final int PAGE = 0;
    private final int SIZE = 10;
    private final long PROJECT_ID = 1L;
    private Pageable pageable;
    private TaskRequest taskRequest;


    @BeforeEach
    public void setUp(){
        task = new Task();

        pageable = PageRequest.of(PAGE, SIZE);

        taskRequest = new TaskRequest(
                "Task title from taskRequest",
                "Task description",
                Priority.HIGH,
                Status.ACTIVE,
                LocalDate.of(LocalDate.now().getYear() + 1, 10, 10),
                PROJECT_ID
        );
    }


    private TaskResponse buildTaskResponse(long taskId) {
        return new TaskResponse(
                taskId,
                "Task-" + taskId,
                "Task-" + taskId + " description",
                Priority.HIGH,
                Status.ACTIVE,
                LocalDate.of(LocalDate.now().getYear() + 1, 10, 10),
                PROJECT_ID,
                2);
    }

    @DisplayName("Should return all project tasks successfully")
    @Test
    @WithMockUser
    public void getAllTasksTest() throws Exception {
        TaskResponse task1 = buildTaskResponse(1);
        TaskResponse task2 = buildTaskResponse(2);

        Page<TaskResponse> tasks = new PageImpl<>(List.of(task1, task2), pageable, 2);

        when(taskService.getAllTasks(any(), eq(PROJECT_ID), eq(PAGE), eq(SIZE))).thenReturn(tasks);

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", Integer.toString(PAGE))
                        .param("size", Integer.toString(SIZE))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Task-1"))
                .andExpect(jsonPath("$.content[1].title").value("Task-2"));
    }

    @DisplayName("Should return the correct task successfully")
    @Test
    @WithMockUser
    public void getTaskByIdTest() throws Exception {
        TaskResponse task = buildTaskResponse(1);

        when(taskService.getTaskById(eq(1L), eq(PROJECT_ID), any())).thenReturn(task);

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/task/1")
                .param("projectId", Long.toString(PROJECT_ID))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Task-1"));
    }

    @DisplayName("Should throw an exception when task is not found")
    @Test
    @WithMockUser
    public void getTaskByIdTaskNotFoundTest() throws Exception {
        when(taskService.getTaskById(eq(1L), eq(PROJECT_ID), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/task/1")
                .param("projectId", Long.toString(PROJECT_ID))
        )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()));
    }

    @DisplayName("Should return overdue tasks")
    @Test
    @WithMockUser
    public void getOverdueTasksTest() throws Exception {
        TaskResponse task1 = buildTaskResponse(1);
        TaskResponse task2 = buildTaskResponse(2);

        Page<TaskResponse> tasks = new PageImpl<>(List.of(task1, task2), pageable, 2);

        when(taskService.getOverdueTasks(eq(PAGE), eq(SIZE), any(), eq(PROJECT_ID))).thenReturn(tasks);

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/t-overdue/" + PROJECT_ID)
                .param("page", Integer.toString(PAGE))
                .param("size", Integer.toString(SIZE))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Task-1"))
                .andExpect(jsonPath("$.content[1].title").value("Task-2"));
    }

    @DisplayName("Should add new task and return it")
    @Test
    @WithMockUser
    public void addTaskTest() throws Exception {
        TaskResponse task = buildTaskResponse(1);

        when(taskService.addTask(eq(taskRequest), any())).thenReturn(task);

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/new-task")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(taskRequest))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @DisplayName("Should update existing task and return it")
    @Test
    @WithMockUser
    public void updateTaskTest() throws Exception {
        TaskResponse task = buildTaskResponse(1);

        when(taskService.updateTask(eq(taskRequest), any(), eq(PROJECT_ID), eq(1L))).thenReturn(task);

        mockMvc.perform(MockMvcRequestBuilders.put(BASE_URL + "/update/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(taskRequest))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Task-1"));
    }

    @DisplayName("Should throw an exception when task is not found")
    @Test
    @WithMockUser
    public void updateTaskTaskNotFoundTest() throws Exception {
        when(taskService.updateTask(eq(taskRequest), any(), eq(PROJECT_ID), eq(1L)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        mockMvc.perform(MockMvcRequestBuilders.put(BASE_URL + "/update/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(taskRequest))
        )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()));
    }

    @DisplayName("Should delete task")
    @Test
    @WithMockUser
    public void deleteTaskTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/delete/1")
                .param("projectId", Long.toString(PROJECT_ID))
        )
                .andExpect(status().isOk());
    }

    @DisplayName("Should return tasks based on keyword")
    @Test
    @WithMockUser
    public void searchTasksTest() throws Exception {
        String keyword = "mock keyword";

        TaskResponse task1 = buildTaskResponse(1);
        TaskResponse task2 = buildTaskResponse(2);
        TaskResponse task3 = buildTaskResponse(3);

        Page<TaskResponse> tasks = new PageImpl<>(List.of(task1, task2, task3), pageable, 3);

        when(taskService.searchTasks(any(), eq(keyword), eq(PAGE), eq(SIZE))).thenReturn(tasks);

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/task/search/" + keyword)
                .param("page", Integer.toString(PAGE))
                .param("size", Integer.toString(SIZE))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3));
    }

    @DisplayName("Should return a task with completion toggled")
    @Test
    @WithMockUser
    public void toggleTaskCompletionTest() throws Exception {
        TaskResponse task = buildTaskResponse(1);

        when(taskService.toggleTaskCompletion(any(), eq(1L), eq(PROJECT_ID))).thenReturn(task);

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/toggle/1")
                .param("projectId", Long.toString(PROJECT_ID))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(Status.ACTIVE.toString()));
    }
}
