package com.filahi.taska.repository;

import com.filahi.taska.entity.Task;
import com.filahi.taska.entity.User;
import com.filahi.taska.enumeration.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findAllByUserAndProject_Id(User user, Long projectId, Pageable pageable);
    Optional<Task> findByUserAndProject_IdAndId(User user, long projectId, long taskId);
    Optional<Task> findByUserAndId(User user, long taskId);

    @Query("""
        SELECT t FROM Task t WHERE (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
                    AND t.user=:user
        """)
    Page<Task> findByKeyword(@Param("user") User user, @Param("keyword") String keyword, Pageable pageable);
    Page<Task> findByUserAndProject_IdAndStatusAndDueDateBefore(User user, long projectId, Status status, LocalDate dueDate, Pageable pageable);

    long countByUserAndProject_Id(User user, long projectId);
    long countByUserAndProject_IdAndStatus(User user, long projectId, Status status);
    long countByUserAndProject_IdAndStatusAndDueDateBefore(User user, long projectId, Status status, LocalDate dueDate);
}
