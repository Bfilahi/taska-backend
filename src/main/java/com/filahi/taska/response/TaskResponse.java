package com.filahi.taska.response;

import com.filahi.taska.enumeration.Priority;

import java.time.LocalDate;

public record TaskResponse(
        long id,
        String title,
        String description,
        Priority priority,
        LocalDate dueDate,
        boolean isCompleted,
        long projectId
//        long subtasks
) {
}
