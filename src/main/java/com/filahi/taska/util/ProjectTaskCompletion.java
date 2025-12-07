package com.filahi.taska.util;

import com.filahi.taska.entity.Project;
import com.filahi.taska.entity.Task;
import com.filahi.taska.enumeration.Status;
import com.filahi.taska.repository.ProjectRepository;
import com.filahi.taska.repository.TaskRepository;
import org.springframework.stereotype.Component;

@Component
public class ProjectTaskCompletion {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public ProjectTaskCompletion(ProjectRepository projectRepository, TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    public void handleTaskCompletion(Task task){
        if(task.getSubtasks().stream().allMatch(st -> st.getStatus().equals(Status.COMPLETED))){
            task.setStatus(Status.COMPLETED);
            this.taskRepository.save(task);
        }
        else{
            task.setStatus(Status.ACTIVE);
            this.taskRepository.save(task);
        }
    }

    public void handleProjectCompletion(Project project) {
        if(project.getTasks().stream().allMatch(task -> task.getStatus().equals(Status.COMPLETED))) {
            project.setStatus(Status.COMPLETED);
            this.projectRepository.save(project);
        }
        else{
            project.setStatus(Status.ACTIVE);
            this.projectRepository.save(project);
        }
    }
}
