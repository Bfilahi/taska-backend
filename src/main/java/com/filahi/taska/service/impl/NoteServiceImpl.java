package com.filahi.taska.service.impl;

import com.filahi.taska.entity.Note;
import com.filahi.taska.entity.Task;
import com.filahi.taska.entity.User;
import com.filahi.taska.repository.NoteRepository;
import com.filahi.taska.repository.TaskRepository;
import com.filahi.taska.response.NoteResponse;
import com.filahi.taska.service.NoteService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class NoteServiceImpl implements NoteService {
    private final TaskRepository taskRepository;
    private final NoteRepository noteRepository;

    public NoteServiceImpl(TaskRepository taskRepository, NoteRepository noteRepository) {
        this.taskRepository = taskRepository;
        this.noteRepository = noteRepository;
    }

    @Override
    public List<NoteResponse> getAllNotes(User user, long taskId) {
        Task task = this.taskRepository.findByUserAndId(user, taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        return task.getNotes().stream().map(note -> new NoteResponse(note.getId(), note.getNote())).toList();
    }

    @Override
    public NoteResponse getNote(User user, long noteId, long taskId) {
        Task task = this.taskRepository.findByUserAndId(user, taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        Note note = this.noteRepository.findByTaskAndId(task, noteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));

        return new NoteResponse(note.getId(), note.getNote());
    }

    @Override
    public NoteResponse addNewNote(User user, long taskId, String note) {
        Task task = this.taskRepository.findByUserAndId(user, taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        Note newNote = new Note(
                0,
                note,
                LocalDate.now(),
                user,
                task
        );

        this.noteRepository.save(newNote);
        return new NoteResponse(newNote.getId(), newNote.getNote());
    }

    @Override
    public void deleteNote(User user, long taskId, long noteId) {
        Task task = this.taskRepository.findByUserAndId(user, taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        Note note = this.noteRepository.findByTaskAndId(task, noteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));

        this.noteRepository.delete(note);
    }
}
