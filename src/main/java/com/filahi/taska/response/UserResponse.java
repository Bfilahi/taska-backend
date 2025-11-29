package com.filahi.taska.response;

import com.filahi.taska.entity.Authority;

import java.util.List;

public record UserResponse(
        long id,
        String firstName,
        String lastName,
        String email,
        String profileImage,
        List<Authority> authorities
) {
}
