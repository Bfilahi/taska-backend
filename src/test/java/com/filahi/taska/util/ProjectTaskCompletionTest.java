package com.filahi.taska.util;

import com.filahi.taska.entity.Project;
import com.filahi.taska.entity.Subtask;
import com.filahi.taska.entity.Task;
import com.filahi.taska.enumeration.Status;
import com.filahi.taska.repository.ProjectRepository;
import com.filahi.taska.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class ProjectTaskCompletionTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectTaskCompletion projectTaskCompletion;

    private Task task;
    private Project project;

    @BeforeEach
    public void setup(){
        Subtask subtask1 = new Subtask();
        Subtask subtask2 = new Subtask();

        task = new Task();
        task.setId(1L);
        task.setStatus(Status.ACTIVE);
        task.setSubtasks(List.of(subtask1,subtask2));

        project = new Project();
        project.setId(1L);
        project.setStatus(Status.ACTIVE);
        project.setTasks(List.of(task));
    }


    @DisplayName("Should set task status to COMPLETED when all subtasks are completed")
    @Test
    public void shouldSetTaskCompletedWhenAllSubtasksAreCompleted() {
        task.getSubtasks().forEach(st -> st.setStatus(Status.COMPLETED));

        projectTaskCompletion.handleTaskCompletion(task);

        verify(taskRepository, times(1)).save(task);

        assertEquals(Status.COMPLETED, task.getStatus());
    }

    @DisplayName("Should set task status to ACTIVE when at least one subtask is not completed")
    @Test
    public void shouldSetTaskActiveWhenAtLeastOneSubtaskIsNotCompleted() {
        task.getSubtasks().getFirst().setStatus(Status.ACTIVE);

        projectTaskCompletion.handleTaskCompletion(task);

        verify(taskRepository, times(1)).save(task);
        assertEquals(Status.ACTIVE, task.getStatus());
    }

    @DisplayName("Should set task status to COMPLETED when task has no subtasks")
    @Test
    public void shouldSetTaskCompletedWhenNoSubtasksExist() {
        Task task = new Task();
        task.setSubtasks(List.of());

        projectTaskCompletion.handleTaskCompletion(task);

        verify(taskRepository, times(1)).save(task);

        assertEquals(Status.COMPLETED, task.getStatus());
    }

    @DisplayName("Should set project status to COMPLETED when all tasks are completed")
    @Test
    public void shouldSetProjectCompletedWhenAllTasksAreCompleted() {
        project.getTasks().forEach(st -> st.setStatus(Status.COMPLETED));

        projectTaskCompletion.handleProjectCompletion(project);

        verify(projectRepository, times(1)).save(project);

        assertEquals(Status.COMPLETED, project.getStatus());
    }

    @DisplayName("Should set project status to ACTIVE when at least one task is not completed")
    @Test
    public void shouldSetProjectActiveWhenAtLeastOneTaskIsNotCompleted() {
        project.getTasks().getFirst().setStatus(Status.ACTIVE);

        projectTaskCompletion.handleProjectCompletion(project);

        verify(projectRepository, times(1)).save(project);

        assertEquals(Status.ACTIVE, project.getStatus());
    }

    @DisplayName("Should set project status to COMPLETED when project has no tasks")
    @Test
    public void shouldSetProjectCompletedWhenNoTasksExist() {
        Project project = new Project();
        project.setTasks(List.of());

        projectTaskCompletion.handleProjectCompletion(project);

        verify(projectRepository, times(1)).save(project);

        assertEquals(Status.COMPLETED, project.getStatus());
    }
}
