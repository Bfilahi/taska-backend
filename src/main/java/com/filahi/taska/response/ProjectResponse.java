package com.filahi.taska.response;

import com.filahi.taska.enumeration.Priority;
import com.filahi.taska.enumeration.Status;

import java.time.LocalDate;

public record ProjectResponse(
        long id,
        String name,
        String description,
        LocalDate startDate,
        LocalDate dueDate,
        Priority priority,
        Status status,
        int progress
) {
}
