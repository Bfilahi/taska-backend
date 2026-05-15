package com.filahi.taska.controller;


import com.filahi.taska.response.NoteResponse;
import com.filahi.taska.service.NoteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
public class NoteControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NoteService noteService;


    private final String BASE_URL = "/api/notes";


    private NoteResponse buildNote(long noteId) {
        return new NoteResponse(noteId, "Mock note for note-" + noteId, LocalDate.now());
    }

    @DisplayName("Should return all notes")
    @Test
    @WithMockUser
    public void getAllNotesTest() throws Exception {
        NoteResponse note1 = buildNote(1L);
        NoteResponse note2 = buildNote(2L);

        List<NoteResponse> notes = List.of(note1, note2);

        when(noteService.getAllNotes(any(), eq(1L))).thenReturn(notes);

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/{taskId}", 1))
                .andExpect(status().isOk());
    }

    @DisplayName("Should return a specific note")
    @Test
    @WithMockUser
    public void getNoteTest() throws Exception {
        NoteResponse note = buildNote(1L);

        when(noteService.getNote(any(), eq(1L), eq(1L))).thenReturn(note);

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/note/{noteId}", 1)
                        .param("taskId", "1"))
                .andExpect(status().isOk());
    }

    @DisplayName("Should throw an exception when not is not found")
    @Test
    @WithMockUser
    public void getNoteNoNoteFoundTest() throws Exception {
        when(noteService.getNote(any(), eq(1L), eq(1L)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/note/{noteId}", 1)
                        .param("taskId", "1"))
                .andExpect(status().isNotFound());
    }

    @DisplayName("Should add a new note and return it")
    @Test
    @WithMockUser
    public void addNewNoteTest() throws Exception {
        NoteResponse note = buildNote(1L);

        when(noteService.addNewNote(any(), eq(1L), eq("mock note"))).thenReturn(note);

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/note/{taskId}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(note))
        )
                .andExpect(status().isCreated());
    }

    @DisplayName("Should delete an existing note")
    @Test
    @WithMockUser
    public void deleteNoteTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/note/{noteId}", 1)
                .param("taskId", "1")
        )
                .andExpect(status().isOk());
    }
}
