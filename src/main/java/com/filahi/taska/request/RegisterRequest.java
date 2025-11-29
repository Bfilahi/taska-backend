package com.filahi.taska.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotEmpty(message = "First name is mandatory")
        @Size(min = 3, max = 20, message = "First name must be at least 3 characters long")
        String firstName,

        @NotEmpty(message = "Last name is mandatory")
        @Size(min = 3, max = 20, message = "Last name must be at least 3 characters long")
        String lastName,

        @NotEmpty(message = "Email is mandatory")
        @Email(message = "Invalid email format")
        String email,

        @NotEmpty(message = "Password name is mandatory")
        @Size(min = 5, max = 20, message = "Password must be at least 5 characters long")
        String password
) {
}
