package com.filahi.taska.service;

import com.filahi.taska.entity.*;
import com.filahi.taska.repository.NoteRepository;
import com.filahi.taska.repository.TaskRepository;
import com.filahi.taska.response.NoteResponse;
import com.filahi.taska.service.impl.NoteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private NoteServiceImpl noteService;

    private User user;
    private Task task;
    private Note note;
    private final long TASK_ID = 1;
    private final long NOTE_ID = 1;
    private final String MOCK_NOTE = "mock note";

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1L);
        user.setFirstName("Mario");
        user.setLastName("Rossi");
        user.setEmail("mario.rossi@example.com");
        user.setPassword("encodedOldPassword");
        user.setAuthorities(List.of(new Authority("ROLE_USER")));

        note = new Note();
        note.setId(1);
        note.setNote("Mock note");

        task = new Task();
        task.setNotes(List.of(note));
    }

    @DisplayName("Get all notes test, success")
    @Test
    public void getAllNotesTest(){
        when(taskRepository.findByUserAndId(user, TASK_ID)).thenReturn(Optional.of(task));

        List<NoteResponse> notes = noteService.getAllNotes(user, TASK_ID);

        assertNotNull(notes);
    }

    @DisplayName("Should throw an exception when task is not found")
    @Test
    public void getAllNotesNoTaskFoundTest(){
        when(taskRepository.findByUserAndId(user, TASK_ID))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        assertThrows(ResponseStatusException.class, () -> noteService.getAllNotes(user, TASK_ID));
    }

    @DisplayName("Returns correct note response when task and note exist")
    @Test
    public void returnsCorrectNoteResponseWhenTaskAndNoteExist() {
        when(taskRepository.findByUserAndId(user, TASK_ID)).thenReturn(Optional.of(task));
        when(noteRepository.findByTaskAndId(task, NOTE_ID)).thenReturn(Optional.of(note));

        NoteResponse result  = noteService.getNote(user, NOTE_ID, TASK_ID);

        assertEquals(note.getNote(), result.note());
    }

    @DisplayName("Throws 404 when task is not found for user")
    @Test
    public void throws404WhenTaskIsNotFoundForUser() {
        when(taskRepository.findByUserAndId(user, TASK_ID))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        assertThrows(ResponseStatusException.class, () -> noteService.getNote(user, NOTE_ID, TASK_ID));
    }

    @DisplayName("Throws 404 when note is not found for task")
    @Test
    public void throws404WhenNoteIsNotFoundForTask() {
        when(taskRepository.findByUserAndId(user, TASK_ID)).thenReturn(Optional.of(task));
        when(noteRepository.findByTaskAndId(task, NOTE_ID))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));

        assertThrows(ResponseStatusException.class, () -> noteService.getNote(user, NOTE_ID, TASK_ID));
    }

    @DisplayName("Returns correct note response after saving note")
    @Test
    public void returnsCorrectNoteResponseAfterSavingNote() {
        when(taskRepository.findByUserAndId(user, TASK_ID)).thenReturn(Optional.of(task));

        noteService.addNewNote(user, TASK_ID, MOCK_NOTE);

        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(captor.capture());

        Note noteSaved = captor.getValue();
        assertAll(
                () -> assertEquals(0, noteSaved.getId()),
                () -> assertEquals(MOCK_NOTE, noteSaved.getNote()),
                () -> assertEquals(LocalDate.now(), noteSaved.getCreatedAt()),
                () -> assertEquals(user, noteSaved.getUser()),
                () -> assertEquals(task, noteSaved.getTask())
        );
    }

    @Test
    @DisplayName("Throws 404 when task is not found for user")
    public void addNoteThrows404WhenTaskIsNotFoundForUser() {
        when(taskRepository.findByUserAndId(user, TASK_ID))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        assertThrows(ResponseStatusException.class, () -> noteService.addNewNote(user, NOTE_ID, MOCK_NOTE));
    }

    @Test
    @DisplayName("Does not save note when task is not found")
    public void doesNotSaveNoteWhenTaskIsNotFound() {
        when(taskRepository.findByUserAndId(user, TASK_ID))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        assertThrows(ResponseStatusException.class, () -> noteService.addNewNote(user, NOTE_ID, MOCK_NOTE));

        verify(noteRepository, never()).save(any());
    }

    @DisplayName("Should delete note successfully")
    @Test
    public void shouldDeleteNoteSuccessfully() {
        when(taskRepository.findByUserAndId(user, TASK_ID)).thenReturn(Optional.of(task));
        when(noteRepository.findByTaskAndId(task, NOTE_ID)).thenReturn(Optional.of(note));

        noteService.deleteNote(user,TASK_ID, NOTE_ID);

        verify(noteRepository).delete(note);
    }

    @DisplayName("Should throw exception when task is not found")
    @Test
    public void shouldThrowExceptionWhenTaskNotFound() {
        when(taskRepository.findByUserAndId(user, TASK_ID))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        assertThrows(ResponseStatusException.class, () -> noteService.getNote(user, NOTE_ID, TASK_ID));
    }

    @DisplayName("Should throw exception when note is not found")
    @Test
    public void shouldThrowExceptionWhenNoteNotFound() {
        when(taskRepository.findByUserAndId(user, TASK_ID)).thenReturn(Optional.of(task));
        when(noteRepository.findByTaskAndId(task, NOTE_ID))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));

        assertThrows(ResponseStatusException.class, () -> noteService.getNote(user, NOTE_ID, TASK_ID));
    }

    @DisplayName("Should call delete exactly once")
    @Test
    public void shouldCallDeleteExactlyOnce() {
        when(taskRepository.findByUserAndId(user, TASK_ID)).thenReturn(Optional.of(task));
        when(noteRepository.findByTaskAndId(task, NOTE_ID)).thenReturn(Optional.of(note));

        noteService.deleteNote(user,TASK_ID, NOTE_ID);

        verify(noteRepository, times(1)).delete(note);
    }
}
