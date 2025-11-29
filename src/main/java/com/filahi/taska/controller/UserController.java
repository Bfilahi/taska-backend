package com.filahi.taska.controller;


import com.filahi.taska.entity.User;
import com.filahi.taska.response.UserResponse;
import com.filahi.taska.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User REST API Endpoints", description = "Operations related to authenticated user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @Operation(summary = "Get user information", description = "Get authenticated user's information")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/info")
    public UserResponse getUserInfo(@AuthenticationPrincipal User user) {
        return userService.getUserInfo(user);
    }

    @Operation(summary = "Delete user", description = "Delete authenticated user from database")
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping
    public void deleteUser(@AuthenticationPrincipal User user) {
        userService.deleteUser(user);
    }
}
