package com.filahi.taska.response;

import com.filahi.taska.enumeration.Priority;

import java.time.LocalDate;

public record ProjectResponse(
        long id,
        String name,
        String description,
        LocalDate dueDate,
        Priority priority,
        boolean isCompleted
//        int progress
) {
}
