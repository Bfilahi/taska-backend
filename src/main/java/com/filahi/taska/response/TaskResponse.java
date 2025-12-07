package com.filahi.taska.response;

import com.filahi.taska.enumeration.Priority;
import com.filahi.taska.enumeration.Status;

import java.time.LocalDate;

public record TaskResponse(
        long id,
        String title,
        String description,
        Priority priority,
        Status status,
        LocalDate dueDate,
        long projectId,
        long subtasks
) {
}
