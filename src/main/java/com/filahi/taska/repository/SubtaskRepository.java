package com.filahi.taska.repository;

import com.filahi.taska.entity.Subtask;
import com.filahi.taska.entity.Task;
import com.filahi.taska.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface SubtaskRepository extends JpaRepository<Subtask, Long> {
    Page<Subtask> findByUserAndTask(User user, Task subtask, Pageable pageable);
    Optional<Subtask> findByUserAndTaskAndId(User user, Task task, long subtaskId);
}
