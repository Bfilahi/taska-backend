package com.filahi.taska.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record PasswordRequest(
        @NotEmpty(message = "Old password is mandatory")
        @Size(min = 5, max = 30, message = "Old password must be at least 5 characters long")
        String oldPassword,

        @NotEmpty(message = "New password is mandatory")
        @Size(min = 5, max = 30, message = "New password must be at least 5 characters long")
        String newPassword,

        @NotEmpty(message = "Confirm password is mandatory")
        @Size(min = 5, max = 30, message = "Confirm password must be at least 5 characters long")
        String confirmPassword
) {
}
