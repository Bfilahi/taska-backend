package com.filahi.taska.service.impl;

import com.filahi.taska.entity.User;
import com.filahi.taska.repository.UserRepository;
import com.filahi.taska.request.PasswordRequest;
import com.filahi.taska.request.UserRequest;
import com.filahi.taska.response.UserResponse;
import com.filahi.taska.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public UserResponse getUserInfo(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getAuthorities()
        );
    }

    @Override
    @Transactional
    public void deleteUser(User user) {
        this.userRepository.delete(user);
    }

    @Override
    public void updateProfile(User user, UserRequest userRequest) {
        user.setFirstName(userRequest.firstName());
        user.setLastName(userRequest.lastName());
        user.setEmail(userRequest.email());

        this.userRepository.save(user);
    }

    @Override
    public void updatePassword(User user, PasswordRequest passwordRequest) {
        if(!isOldPasswordValid(user.getPassword(), passwordRequest.oldPassword()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old password is invalid");

        if(!doPasswordsMatch(passwordRequest.newPassword(), passwordRequest.confirmPassword()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords don't match");

        if(!arePasswordsDifferent(passwordRequest.oldPassword(), passwordRequest.newPassword()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old and new passwords should match");

        user.setPassword(passwordEncoder.encode(passwordRequest.newPassword()));
        this.userRepository.save(user);
    }

    private boolean isOldPasswordValid(String currentPassword, String oldPassword) {
        return this.passwordEncoder.matches(oldPassword, currentPassword);
    }

    private boolean doPasswordsMatch(String newPassword, String confirmPassword){
        return newPassword.equals(confirmPassword);
    }

    private boolean arePasswordsDifferent(String oldPassword, String newPassword){
        return !oldPassword.equals(newPassword);
    }
}
