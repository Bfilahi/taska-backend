package com.filahi.taska.service.impl;

import com.filahi.taska.entity.Subtask;
import com.filahi.taska.entity.Task;
import com.filahi.taska.entity.User;
import com.filahi.taska.repository.SubtaskRepository;
import com.filahi.taska.repository.TaskRepository;
import com.filahi.taska.request.SubtaskRequest;
import com.filahi.taska.response.SubtaskResponse;
import com.filahi.taska.service.SubtaskService;
import com.filahi.taska.util.PageableUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@Service
public class SubtaskServiceImpl implements SubtaskService {
    private final SubtaskRepository subtaskRepository;
    private final TaskRepository taskRepository;
    private final PageableUtil pageableUtil;

    public SubtaskServiceImpl(SubtaskRepository subtaskRepository, TaskRepository taskRepository, PageableUtil pageableUtil) {
        this.subtaskRepository = subtaskRepository;
        this.taskRepository = taskRepository;
        this.pageableUtil = pageableUtil;
    }


    @Override
    public Page<SubtaskResponse> getSubtaskByTaskId(int page, int size, User user, long taskId) {
        Task task = this.taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        Pageable pageable = this.pageableUtil.getPageable(page, size, "", "");
        Page<Subtask> subTasks = this.subtaskRepository.findByUserAndTask(user, task, pageable);

        List<SubtaskResponse> subTaskResponses = subTasks.stream().map(SubtaskServiceImpl::buildSubtaskResponse).toList();
        return new PageImpl<>(subTaskResponses, pageable, subTasks.getTotalElements());
    }

    @Override
    public SubtaskResponse getSubtask(User user, long subtaskId, long taskId) {
        Task task = this.taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        Subtask subtask = this.subtaskRepository.findByUserAndTaskAndId(user, task, subtaskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subtask not found"));

        return buildSubtaskResponse(subtask);
    }

    @Override
    @Transactional
    public SubtaskResponse addNewSubtask(User user, SubtaskRequest subtaskRequest) {
        Task task = this.taskRepository.findById(subtaskRequest.taskId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        Subtask subtask = new Subtask(
                0,
                subtaskRequest.title(),
                subtaskRequest.description(),
                subtaskRequest.priority(),
                subtaskRequest.dueDate(),
                false,
                user,
                task
        );

        this.subtaskRepository.save(subtask);
        return buildSubtaskResponse(subtask);
    }

    @Override
    @Transactional
    public SubtaskResponse updateSubtask(User user, SubtaskRequest subtaskRequest, long subtaskId) {
        Task task = this.taskRepository.findById(subtaskRequest.taskId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        Subtask subtask = this.subtaskRepository.findByUserAndTaskAndId(user, task, subtaskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subtask not found"));

        subtask.setTitle(subtaskRequest.title());
        subtask.setDescription(subtaskRequest.description());
        subtask.setPriority(subtaskRequest.priority());
        subtask.setDueDate(subtaskRequest.dueDate());
        subtask.setTask(task);

        this.subtaskRepository.save(subtask);
        return buildSubtaskResponse(subtask);
    }

    @Override
    @Transactional
    public void deleteSubtask(User user, long subtaskId, long taskId) {
        Task task = this.taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        Subtask subtask = this.subtaskRepository.findByUserAndTaskAndId(user, task, subtaskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subtask not found"));
        subtaskRepository.delete(subtask);
    }


    private static SubtaskResponse buildSubtaskResponse(Subtask subtask){
        return new SubtaskResponse(
                subtask.getId(),
                subtask.getTitle(),
                subtask.getDescription(),
                subtask.getPriority(),
                subtask.getDueDate(),
                subtask.isCompleted(),
                subtask.getTask().getId()
        );
    }
}
