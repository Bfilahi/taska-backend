package com.filahi.taska.request;

import com.filahi.taska.enumeration.Priority;
import com.filahi.taska.enumeration.Status;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record ProjectRequest(
        @NotEmpty(message = "Name is mandatory")
        @Size(min = 3, max = 200, message = "Name must be at least 3 characters long")
        String name,

        @NotEmpty(message = "Description is mandatory")
        @Size(min = 3, max = 200, message = "Description must be at least 3 characters long")
        String description,

        @FutureOrPresent(message = "Due date must be today or in the future")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dueDate,

        Priority priority,

        Status status
) {
}
