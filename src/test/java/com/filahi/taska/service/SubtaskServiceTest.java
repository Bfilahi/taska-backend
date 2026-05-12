package com.filahi.taska.service;

import com.filahi.taska.entity.*;
import com.filahi.taska.enumeration.Priority;
import com.filahi.taska.enumeration.Status;
import com.filahi.taska.repository.SubtaskRepository;
import com.filahi.taska.repository.TaskRepository;
import com.filahi.taska.request.SubtaskRequest;
import com.filahi.taska.response.SubtaskResponse;
import com.filahi.taska.service.impl.SubtaskServiceImpl;
import com.filahi.taska.util.PageableUtil;
import static org.junit.jupiter.api.Assertions.*;
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
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class SubtaskServiceTest {
    @Mock
    private SubtaskRepository subtaskRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectTaskCompletion projectTaskCompletion;

    @Mock
    private PageableUtil pageableUtil;

    @InjectMocks
    private SubtaskServiceImpl subtaskService;

    private User user;
    private Task task;
    private final int PAGE = 0;
    private final int SIZE = 10;
    private final long TASK_ID = 1;
    private final long SUBTASK_ID = 1;
    private Pageable pageable;
    private SubtaskRequest subtaskRequest;

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

        task = new Task();
        task.setId(TASK_ID);

        subtaskRequest = new SubtaskRequest(
                "Subtask title",
                "Subtask description",
                Priority.HIGH,
                Status.ACTIVE,
                LocalDate.of(LocalDate.now().getYear() + 1, 10, 10),
                TASK_ID
        );
    }

    private Subtask buildSubtask(long subtaskId) {
        Subtask subtask = new Subtask();
        subtask.setId(subtaskId);
        subtask.setTitle("Subtask-" + subtaskId);
        subtask.setTask(task);

        return subtask;
    }

    @DisplayName("Should return paginated SubtaskResponse list when task and user are valid")
    @Test
    public void getSubtaskByTaskIdValidTaskAndUserReturnsPaginatedResponse(){
        Subtask subtask1 = buildSubtask(1L);
        Subtask subtask2 = buildSubtask(2L);

        Page<Subtask> subtasks = new PageImpl<>(List.of(subtask1, subtask2));

        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(pageableUtil.getPageable(PAGE, SIZE, "", "")).thenReturn(pageable);
        when(subtaskRepository.findByUserAndTask(user, task, pageable)).thenReturn(subtasks);

        Page<SubtaskResponse> result = subtaskService.getSubtaskByTaskId(PAGE, SIZE, user, TASK_ID);

        assertNotNull(result);
    }

    @DisplayName("Should map each Subtask to SubtaskResponse correctly")
    @Test
    public void getSubtaskByTaskIdValidSubtasksMapsToSubtaskResponseCorrectly(){
        Subtask subtask1 = buildSubtask(1L);
        Subtask subtask2 = buildSubtask(2L);

        Page<Subtask> subtasks = new PageImpl<>(List.of(subtask1, subtask2));

        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(pageableUtil.getPageable(PAGE, SIZE, "", "")).thenReturn(pageable);
        when(subtaskRepository.findByUserAndTask(user, task, pageable)).thenReturn(subtasks);

        Page<SubtaskResponse> result = subtaskService.getSubtaskByTaskId(PAGE, SIZE, user, TASK_ID);

        assertNotNull(result);
        assertEquals(subtask1.getId(), result.getContent().get(0).id());
        assertEquals(subtask2.getId(), result.getContent().get(1).id());
    }

    @DisplayName("Should preserve total elements from repository in returned page")
    @Test
    public void getSubtaskByTaskIdValidPagePreservesTotalElementsFromRepository(){
        Subtask subtask1 = buildSubtask(1L);
        Subtask subtask2 = buildSubtask(2L);

        Page<Subtask> subtasks = new PageImpl<>(List.of(subtask1, subtask2), pageable, 2);

        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(pageableUtil.getPageable(PAGE, SIZE, "", "")).thenReturn(pageable);
        when(subtaskRepository.findByUserAndTask(user, task, pageable)).thenReturn(subtasks);

        Page<SubtaskResponse> result = subtaskService.getSubtaskByTaskId(PAGE, SIZE, user, TASK_ID);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
    }

    @DisplayName("Should throw an exception ResponseStatusException when task is not found")
    @Test
    public void getSubtaskByTaskIdTaskNotFoundThrowsResponseStatusException(){
        when(taskRepository.findById(TASK_ID)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        assertThrows(ResponseStatusException.class, () -> subtaskService.getSubtaskByTaskId(PAGE, SIZE, user, TASK_ID));
    }

    @DisplayName("Should return SubtaskResponse when user, taskId and subtaskId are valid")
    @Test
    public void getSubtaskValidUserTaskAndSubtaskReturnsSubtaskResponse(){
        Subtask subtask = buildSubtask(1L);

        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(subtaskRepository.findByUserAndTaskAndId(user, task, subtask.getId())).thenReturn(Optional.of(subtask));

        SubtaskResponse result = subtaskService.getSubtask(user, subtask.getId(), TASK_ID);

        assertNotNull(result);
        assertEquals(1, result.id());
        assertEquals("Subtask-" + subtask.getId(), result.title());
    }

    @DisplayName("Should throw 404 ResponseStatusException when task is not found")
    @Test
    public void getSubtaskTaskNotFoundThrowsResponseStatusException(){
        when(taskRepository.findById(TASK_ID)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        assertThrows(ResponseStatusException.class, () -> subtaskService.getSubtask(user, SUBTASK_ID, TASK_ID));
    }

    @DisplayName("Should throw 404 ResponseStatusException when subtask is not found")
    @Test
    public void getSubtaskSubtaskNotFoundThrowsResponseStatusException(){
        Subtask subtask = buildSubtask(1L);

        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(subtaskRepository.findByUserAndTaskAndId(user, task, subtask.getId()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Subtask not found"));

        assertThrows(ResponseStatusException.class, () -> subtaskService.getSubtask(user, subtask.getId(), TASK_ID));
    }

    @DisplayName("Should return SubtaskResponse when user and subtaskRequest are valid")
    @Test
    public void addNewSubtaskValidUserAndRequestReturnsSubtaskResponse(){
        when(taskRepository.findById(SUBTASK_ID)).thenReturn(Optional.of(task));

        subtaskService.addNewSubtask(user, subtaskRequest);

        ArgumentCaptor<Subtask> captor = ArgumentCaptor.forClass(Subtask.class);
        verify(subtaskRepository, times(1)).save(captor.capture());

        Subtask subtask = captor.getValue();
        assertAll(
                () -> assertEquals(0, subtask.getId()),
                () -> assertEquals(subtaskRequest.title(), subtask.getTitle()),
                () -> assertEquals(subtaskRequest.description(), subtask.getDescription()),
                () -> assertEquals(subtaskRequest.priority(), subtask.getPriority()),
                () -> assertEquals(subtaskRequest.dueDate(), subtask.getDueDate()),
                () -> assertEquals(Status.ACTIVE, subtask.getStatus()),
                () -> assertEquals(LocalDate.now(), subtask.getCreatedAt()),
                () -> assertEquals(user, subtask.getUser()),
                () -> assertEquals(task, subtask.getTask())
        );
    }

    @DisplayName("Should throw 404 ResponseStatusException when task is not found")
    @Test
    public void addNewSubtaskTaskNotFoundThrowsResponseStatusException(){
        when(taskRepository.findById(SUBTASK_ID)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Subtask not found"));

        assertThrows(ResponseStatusException.class, () -> subtaskService.addNewSubtask(user, subtaskRequest));
    }

    @DisplayName("Should update and return subtask response when request is valid")
    @Test
    public void updateSubtaskSuccess(){
        Subtask subtask = buildSubtask(1L);

        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(subtaskRepository.findByUserAndTaskAndId(user, task, subtask.getId())).thenReturn(Optional.of(subtask));

        SubtaskResponse result = subtaskService.updateSubtask(user, subtaskRequest, SUBTASK_ID);

        verify(subtaskRepository, times(1)).save(subtask);

        assertAll(
                () -> assertEquals(subtaskRequest.title(), result.title()),
                () -> assertEquals(subtaskRequest.description(), result.description()),
                () -> assertEquals(subtaskRequest.dueDate(), result.dueDate()),
                () -> assertEquals(subtaskRequest.priority(), result.priority()),
                () -> assertEquals(subtaskRequest.status(), result.status()),
                () -> assertEquals(subtaskRequest.taskId(), result.taskId())
        );
    }

    @DisplayName("Should throw an exception when task is not found")
    @Test
    public void updateSubtaskTaskNotFoundThrowsNotFound(){
        when(taskRepository.findById(TASK_ID)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        assertThrows(ResponseStatusException.class, () -> subtaskService.updateSubtask(user, subtaskRequest, SUBTASK_ID));

        verify(subtaskRepository, never()).save(any());
    }

    @DisplayName("Should throw an exception when new subtask is not found")
    @Test
    public void updateSubtaskWhenSubtaskNotFoundThrowsNotFound(){
        Subtask subtask = buildSubtask(1L);

        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(subtaskRepository.findByUserAndTaskAndId(user, task, subtask.getId()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Subtask not found"));

        assertThrows(ResponseStatusException.class, () -> subtaskService.updateSubtask(user, subtaskRequest, SUBTASK_ID));

        verify(subtaskRepository, never()).save(any());
    }

    @DisplayName("Should delete task when request is valid")
    @Test
    public void deleteSubtaskSuccess(){
        Subtask subtask = buildSubtask(1);

        when(taskRepository.findByUserAndId(user, TASK_ID)).thenReturn(Optional.of(task));
        when(subtaskRepository.findByUserAndTaskAndId(user, task, subtask.getId())).thenReturn(Optional.of(subtask));

        subtaskService.deleteSubtask(user, SUBTASK_ID, TASK_ID);

        verify(subtaskRepository, times(1)).delete(subtask);
    }

    @DisplayName("Should throw an exception when subtask is not found")
    @Test
    public void deleteSubtaskSubtaskNotFoundThrowsNotFound(){
        when(taskRepository.findByUserAndId(user, TASK_ID)).thenReturn(Optional.of(task));
        when(subtaskRepository.findByUserAndTaskAndId(user, task, SUBTASK_ID))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Subtask not found"));

        assertThrows(ResponseStatusException.class, () -> subtaskService.deleteSubtask(user, SUBTASK_ID, TASK_ID));

        verify(subtaskRepository, never()).delete(any());
    }

    @DisplayName("Should throw an exception when task is not found")
    @Test
    public void deleteSubtaskTaskNotFoundThrowsNotFound(){
        when(taskRepository.findByUserAndId(user, TASK_ID)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        assertThrows(ResponseStatusException.class, () -> subtaskService.deleteSubtask(user, SUBTASK_ID, TASK_ID));

        verify(subtaskRepository, never()).delete(any());
    }

    @DisplayName("Should toggle subtask from ACTIVE to COMPLETED successfully")
    @Test
    public void toggleSubtaskCompletionActiveToCompletedSuccess(){
        Subtask subtask = buildSubtask(1);
        subtask.setStatus(Status.ACTIVE);

        when(taskRepository.findByUserAndId(user, TASK_ID)).thenReturn(Optional.of(task));
        when(subtaskRepository.findByUserAndTaskAndId(user, task, subtask.getId())).thenReturn(Optional.of(subtask));

        SubtaskResponse result = subtaskService.toggleSubtaskCompletion(user, SUBTASK_ID, TASK_ID);

        verify(subtaskRepository, times(1)).save(subtask);
        assertEquals(Status.COMPLETED, result.status());
    }

    @DisplayName("Should toggle subtask from COMPLETED to ACTIVE successfully")
    @Test
    public void toggleSubtaskCompletionCompletedToActiveSuccess(){
        Subtask subtask = buildSubtask(1);
        subtask.setStatus(Status.COMPLETED);

        when(taskRepository.findByUserAndId(user, TASK_ID)).thenReturn(Optional.of(task));
        when(subtaskRepository.findByUserAndTaskAndId(user, task, subtask.getId())).thenReturn(Optional.of(subtask));

        SubtaskResponse result = subtaskService.toggleSubtaskCompletion(user, SUBTASK_ID, TASK_ID);

        verify(subtaskRepository, times(1)).save(subtask);

        assertEquals(Status.ACTIVE, result.status());
    }

    @DisplayName("Should throw an exception when task is not found")
    @Test
    public void toggleSubtaskCompletionTaskNotFoundThrowsNotFound(){
        when(taskRepository.findByUserAndId(user, TASK_ID))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        assertThrows(ResponseStatusException.class, () -> subtaskService.toggleSubtaskCompletion(user, SUBTASK_ID, TASK_ID));

        verify(subtaskRepository, never()).save(any());
    }

    @DisplayName("Should throw an exception when subtask is not found")
    @Test
    public void toggleSubtaskCompletionSubtaskNotFoundThrowsNotFound(){
        when(taskRepository.findByUserAndId(user, TASK_ID)).thenReturn(Optional.of(task));
        when(subtaskRepository.findByUserAndTaskAndId(user, task, SUBTASK_ID))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Subtask not found"));

        assertThrows(ResponseStatusException.class, () -> subtaskService.toggleSubtaskCompletion(user, SUBTASK_ID, TASK_ID));

        verify(subtaskRepository, never()).save(any());
    }

    @DisplayName("Should call handleTaskCompletion with the correct task")
    @Test
    public void toggleSubtaskCompletionCallsHandleTaskCompletionWithCorrectTask(){
        Subtask subtask = buildSubtask(1);
        subtask.setStatus(Status.ACTIVE);

        when(taskRepository.findByUserAndId(user, TASK_ID)).thenReturn(Optional.of(task));
        when(subtaskRepository.findByUserAndTaskAndId(user, task, subtask.getId())).thenReturn(Optional.of(subtask));

        subtaskService.toggleSubtaskCompletion(user, SUBTASK_ID, TASK_ID);

        verify(projectTaskCompletion, times(1)).handleTaskCompletion(task);

    }

}
