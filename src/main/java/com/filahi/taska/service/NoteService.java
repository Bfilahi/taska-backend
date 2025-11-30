package com.filahi.taska.service;

import com.filahi.taska.entity.User;
import com.filahi.taska.response.NoteResponse;

import java.util.List;

public interface NoteService {
    List<NoteResponse> getAllNotes(User user, long taskId);
    NoteResponse getNote(User user, long noteId, long taskId);
    NoteResponse addNewNote(User user, long taskId, String note);
    void deleteNote(User user, long taskId, long noteId);
}
