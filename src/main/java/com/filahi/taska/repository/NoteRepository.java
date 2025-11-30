package com.filahi.taska.repository;

import com.filahi.taska.entity.Note;
import com.filahi.taska.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    Optional<Note> findByTaskAndId(Task task, long noteId);
}
