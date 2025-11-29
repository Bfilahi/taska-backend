package com.filahi.taska.service;

import com.filahi.taska.request.RegisterRequest;

public interface AuthenticationService {
    String signIn(String email, String password);
    void signUp(RegisterRequest registerRequest);
}
