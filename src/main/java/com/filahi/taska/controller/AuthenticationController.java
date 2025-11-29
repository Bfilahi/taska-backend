package com.filahi.taska.controller;

import com.filahi.taska.request.RegisterRequest;
import com.filahi.taska.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication REST API Endpoints", description = "Operations related to sign-in/up")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/sign-in")
    @Operation(summary = "User sign-in", description = "Submit email & password to authenticate user")
    public String signIn(@RequestParam String email, @RequestParam String password) {
        return this.authenticationService.signIn(email, password);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/sign-up")
    @Operation(summary = "User sign-up", description = "Create new user in database")
    public void signUp(@Valid @RequestBody RegisterRequest registerRequest) {
        this.authenticationService.signUp(registerRequest);
    }
}
