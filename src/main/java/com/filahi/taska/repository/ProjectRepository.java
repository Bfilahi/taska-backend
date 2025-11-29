package com.filahi.taska.repository;

import com.filahi.taska.entity.Project;
import com.filahi.taska.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Page<Project> findByUser(User user, Pageable pageable);
    Optional<Project> findByUserAndId(User user, long projectId);

    @Query("""
        SELECT p FROM Project p WHERE p.user=:user AND
            (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """)
    Page<Project> searchByKeyword(@Param("user") User user, @Param("keyword") String keyword, Pageable pageable);




    long countByUser(User user);
    long countByUserAndIsCompletedTrue(User user);
}
