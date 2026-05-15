package com.filahi.taska.controller;


import com.filahi.taska.enumeration.Priority;
import com.filahi.taska.enumeration.Status;
import com.filahi.taska.request.SubtaskRequest;
import com.filahi.taska.response.SubtaskResponse;
import com.filahi.taska.service.SubtaskService;
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
public class SubtaskControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubtaskService subtaskService;

    private final String BASE_URL = "/api/subtasks";
    private final int PAGE = 0;
    private final int SIZE = 10;
    private final long TASK_ID = 1L;
    private Pageable pageable;
    private SubtaskRequest subtaskRequest;


    @BeforeEach
    public void setUp(){
        pageable = PageRequest.of(PAGE, SIZE);

        subtaskRequest = new SubtaskRequest(
                "Subtask title from request",
                "Subtask description",
                Priority.HIGH,
                Status.ACTIVE,
                LocalDate.of(LocalDate.now().getYear() + 1, 10, 10),
                TASK_ID
        );



    }

    private SubtaskResponse buildSubtask(long subtaskId){
        return new SubtaskResponse(
                subtaskId,
                "Subtask-" + subtaskId + " title",
                "Subtask description",
                Priority.HIGH,
                Status.ACTIVE,
                LocalDate.of(LocalDate.now().getYear() + 1, 10, 10),
                TASK_ID
        );
    }

    @DisplayName("Should return all subtasks for a given task")
    @Test
    @WithMockUser
    public void getSubtaskByTaskIdTest() throws Exception {
        SubtaskResponse subtask1 = buildSubtask(1L);
        SubtaskResponse subtask2 = buildSubtask(2L);
        SubtaskResponse subtask3 = buildSubtask(3L);

        Page<SubtaskResponse> subtasks = new PageImpl<>(List.of(subtask1, subtask2, subtask3), pageable, 3);

        when(subtaskService.getSubtaskByTaskId(eq(PAGE), eq(SIZE), any(), eq(TASK_ID))).thenReturn(subtasks);

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/1/subtasks")
                .param("page", Integer.toString(PAGE))
                .param("size", Integer.toString(SIZE))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].title").value("Subtask-1 title"))
                .andExpect(jsonPath("$.content[1].title").value("Subtask-2 title"))
                .andExpect(jsonPath("$.content[2].title").value("Subtask-3 title"));
    }

    @DisplayName("Should return the correct subtask")
    @Test
    @WithMockUser
    public void getSubtaskTest() throws Exception {
        SubtaskResponse subtask = buildSubtask(1L);

        when(subtaskService.getSubtask(any(), eq(subtask.id()), eq(TASK_ID))).thenReturn(subtask);

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/subtask/1")
                .param("taskId", Long.toString(TASK_ID))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Subtask-1 title"));
    }

    @DisplayName("Should throw an exception when task is not found")
    @Test
    @WithMockUser
    public void getSubtaskTaskNotFoundTest() throws Exception {
        when(subtaskService.getSubtask(any(), eq(1L), eq(TASK_ID)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/subtask/1")
                        .param("taskId", Long.toString(TASK_ID))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()));
    }

    @DisplayName("Should add new subtask")
    @Test
    @WithMockUser
    public void addNewSubtaskTest() throws Exception {
        SubtaskResponse subtask = buildSubtask(1L);

        when(subtaskService.addNewSubtask(any(), eq(subtaskRequest))).thenReturn(subtask);

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/new-subtask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(subtaskRequest))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Subtask-1 title"));
    }

    @DisplayName("Should update existing subtask")
    @Test
    @WithMockUser
    public void updateSubtaskTest() throws Exception {
        SubtaskResponse task = buildSubtask(TASK_ID);

        when(subtaskService.updateSubtask(any(), eq(subtaskRequest), eq(TASK_ID))).thenReturn(task);

        mockMvc.perform(MockMvcRequestBuilders.put(BASE_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(subtaskRequest))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Subtask-1 title"));
    }

    @DisplayName("Should throw an exception when task is not found")
    @Test
    @WithMockUser
    public void updateSubtaskTaskNotFoundTest() throws Exception {
        when(subtaskService.updateSubtask(any(), eq(subtaskRequest), eq(TASK_ID)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        mockMvc.perform(MockMvcRequestBuilders.put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(subtaskRequest))
                )
                .andExpect(status().isNotFound());
    }

    @DisplayName("Should delete a subtask")
    @Test
    @WithMockUser
    public void deleteSubtaskTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/1")
                .param("taskId", Long.toString(TASK_ID))
        )
                .andExpect(status().isOk());
    }

    @DisplayName("Should toggle subtask completion")
    @Test
    @WithMockUser
    public void toggleSubtaskCompletionTest() throws Exception {
        SubtaskResponse subtask = buildSubtask(1L);

        when(subtaskService.toggleSubtaskCompletion(any(), eq(1L), eq(TASK_ID))).thenReturn(subtask);

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/toggle/1")
                .param("taskId", Long.toString(TASK_ID))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(subtask.status().toString()));
    }

    @DisplayName("Should throw an exception when task is not found")
    @Test
    @WithMockUser
    public void toggleSubtaskCompletionTaskNotFoundTest() throws Exception {
        when(subtaskService.toggleSubtaskCompletion(any(), eq(1L), eq(TASK_ID)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/toggle/1")
                .param("taskId", Long.toString(TASK_ID))
        )
                .andExpect(status().isNotFound());
    }
}
