package com.filahi.taska.controller;


import com.filahi.taska.entity.User;
import com.filahi.taska.response.NoteResponse;
import com.filahi.taska.service.NoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@Tag(name = "Note REST API Endpoints", description = "Operations related to notes")
public class NoteController {
    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }


    @Operation(summary = "Get all notes", description = "Get all tasks's notes")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{taskId}")
    public List<NoteResponse> getAllNotes(@AuthenticationPrincipal User user,
                                          @PathVariable long taskId) {
        return this.noteService.getAllNotes(user, taskId);
    }

    @Operation(summary = "Get a note", description = "Get a note given note ID and task ID")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/note/{noteId}")
    public NoteResponse getNote(@AuthenticationPrincipal User user,
                                @PathVariable long noteId,
                                @RequestParam long taskId) {
        return this.noteService.getNote(user, noteId, taskId);
    }

    @Operation(summary = "Add new note", description = "Add new note to a task")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/note/{taskId}")
    public NoteResponse addNewNote(@AuthenticationPrincipal User user,
                                   @PathVariable long taskId,
                                   @RequestBody String note){
        return this.noteService.addNewNote(user, taskId, note);
    }

    @Operation(summary = "Delete a note", description = "Delete a note given note ID and task ID")
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/note/{noteId}")
    public void deleteNote(@AuthenticationPrincipal User user,
                           @PathVariable long noteId,
                           @RequestParam long taskId) {
        this.noteService.deleteNote(user, taskId, noteId);
    }
}
