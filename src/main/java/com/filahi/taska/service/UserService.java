package com.filahi.taska.service;

import com.filahi.taska.entity.User;
import com.filahi.taska.response.UserResponse;

public interface UserService {
    UserResponse getUserInfo(User user);
    void deleteUser(User user);
}
