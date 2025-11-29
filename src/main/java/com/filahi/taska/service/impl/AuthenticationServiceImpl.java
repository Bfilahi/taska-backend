package com.filahi.taska.service.impl;

import com.filahi.taska.entity.Authority;
import com.filahi.taska.entity.User;
import com.filahi.taska.repository.UserRepository;
import com.filahi.taska.request.RegisterRequest;
import com.filahi.taska.service.AuthenticationService;
import com.filahi.taska.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationServiceImpl(UserRepository userRepository, AuthenticationManager authenticationManager, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }



    @Override
    public String signIn(String email, String password) {
        this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        User user = this.userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid credentials"));

        return this.jwtService.generateToken(new HashMap<>(), user);
    }

    @Override
    @Transactional
    public void signUp(RegisterRequest registerRequest) {
        if(isEmailTaken(registerRequest.email()))
            throw new  ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");

        User newUser = buildNewUser(registerRequest);
        this.userRepository.save(newUser);
    }




    private User buildNewUser(RegisterRequest registerRequest) {
        return new User(
                0,
                registerRequest.firstName(),
                registerRequest.lastName(),
                registerRequest.email(),
                this.passwordEncoder.encode(registerRequest.password()),
                "https://ui-avatars.com/api/?name=" + registerRequest.firstName() +
                "+" + registerRequest.lastName() + "&background=random&color=random",
                getAuthorities());
    }

    private boolean isEmailTaken(String email) {
        return this.userRepository.findByEmail(email).isPresent();
    }

    private List<Authority> getAuthorities(){
        List<Authority> authorities = new ArrayList<>();
        authorities.add(new Authority("ROLE_USER"));

        return authorities;
    }
}
