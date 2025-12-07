package com.filahi.taska.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotEmpty(message = "First name is mandatory")
        @Size(min = 3, max = 30, message = "First name must be at least 3 character long")
        String firstName,

        @NotEmpty(message = "Last name is mandatory")
        @Size(min = 3, max = 30, message = "Last name must be at least 3 character long")
        String lastName,

        @NotEmpty(message = "Email is mandatory")
        @Email(message = "Invalid email format")
        String email
) {
}
