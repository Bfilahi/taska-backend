package com.filahi.taska.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.filahi.taska.enumeration.Priority;
import com.filahi.taska.enumeration.Status;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;


@Table(name = "projects")
@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDate createdAt;

    @UpdateTimestamp
    private LocalDate updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Task> tasks;

    public Project() {
    }

    public Project(long id, String name, String description, LocalDate dueDate, Status status, LocalDate createdAt, Priority priority, User user, List<Task> tasks) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status;
        this.createdAt = createdAt;
        this.priority = priority;
        this.user = user;
        this.tasks = tasks;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", dueDate=" + dueDate +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", priority=" + priority +
                ", user=" + user +
                ", tasks=" + tasks +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return id == project.id && Objects.equals(name, project.name) && Objects.equals(description, project.description) && Objects.equals(dueDate, project.dueDate) && status == project.status && Objects.equals(createdAt, project.createdAt) && Objects.equals(updatedAt, project.updatedAt) && priority == project.priority && Objects.equals(user, project.user) && Objects.equals(tasks, project.tasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, dueDate, status, createdAt, updatedAt, priority, user, tasks);
    }
}
