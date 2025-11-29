package com.filahi.taska.request;

import com.filahi.taska.enumeration.Priority;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record SubtaskRequest(
        @NotEmpty(message = "Title is mandatory")
        @Size(min = 3, max = 200, message = "Title must be at least 3 characters long")
        String title,

        @NotEmpty
        @Size(min = 3, max = 200, message = "Description must be at least 3 characters long")
        String description,

        Priority priority,

        @FutureOrPresent(message = "Due date must be today or in the future")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dueDate,

        @Min(value = 1, message = "Task ID must be greater or equal to 1")
        long taskId
) {
}
