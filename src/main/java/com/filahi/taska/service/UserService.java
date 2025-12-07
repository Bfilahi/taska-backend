package com.filahi.taska.service;

import com.filahi.taska.entity.User;
import com.filahi.taska.request.PasswordRequest;
import com.filahi.taska.request.UserRequest;
import com.filahi.taska.response.UserResponse;

public interface UserService {
    UserResponse getUserInfo(User user);
    void deleteUser(User user);
    void updateProfile(User user, UserRequest userRequest);
    void updatePassword(User user, PasswordRequest passwordRequest);
}
