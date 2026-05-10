package com.filahi.taska.service;

import com.filahi.taska.entity.Authority;
import com.filahi.taska.entity.User;
import com.filahi.taska.repository.UserRepository;
import com.filahi.taska.request.PasswordRequest;
import com.filahi.taska.request.UserRequest;
import com.filahi.taska.response.UserResponse;
import com.filahi.taska.service.impl.UserServiceImpl;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    public void setUp(){
        user = new User();
        user.setId(1);
        user.setFirstName("Mario");
        user.setLastName("Rossi");
        user.setEmail("mario.rossi@example.com");
        user.setPassword("encodedOldPassword");
        user.setAuthorities(List.of(new Authority("ROLE_USER")));
    }

    @DisplayName("getUserInfo test")
    @Test
    public void getUserInfoTest(){
        UserResponse response = userService.getUserInfo(user);

        assertEquals(1, response.id());
        assertEquals("Mario", response.firstName());
        assertEquals("Rossi", response.lastName());
        assertEquals("mario.rossi@example.com", response.email());
        assertEquals(user.getAuthorities(), response.authorities());
    }

    @DisplayName("deleteUser test")
    @Test
    public void deleteUserTest(){
        userService.deleteUser(user);

        verify(userRepository, times(1)).delete(user);
    }

    @DisplayName("updateProfile test")
    @Test
    public void updateProfileTest(){
        UserRequest request = new UserRequest("Adam", "Neri", "adam.neri@example.com");

        userService.updateProfile(user, request);

        assertEquals("Adam", user.getFirstName(), "Should update the first name to 'Adam'");
        assertEquals("Neri", user.getLastName(), "Should update the last name to 'Neri'");
        assertEquals("adam.neri@example.com", user.getEmail(), "Should upadate the email to 'adam.neri@example.com'");
    }

    @DisplayName("update password successfully")
    @Test
    public void updatePasswordTest(){
        PasswordRequest passwordRequest = new PasswordRequest("oldPassword", "newPassword", "newPassword");

        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        userService.updatePassword(user, passwordRequest);

        assertEquals("encodedNewPassword", user.getPassword(), "Should update password to 'encodedNewPassword'");
        verify(userRepository, times(1)).save(user);
    }

    @DisplayName("update password, old password is invalid")
    @Test
    public void updatePasswordInvalidOldPassword(){
        PasswordRequest passwordRequest = new PasswordRequest("wrongPassword", "newPassword", "newPassword");

        when(passwordEncoder.matches("wrongPassword", "encodedOldPassword")).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> userService.updatePassword(user, passwordRequest));
        verify(userRepository, never()).save(any());
    }

    @DisplayName("update password, new passwords don't match")
    @Test
    public void updatePasswordPasswordsDontMatch(){
        PasswordRequest passwordRequest = new PasswordRequest("oldPassword", "newPassword123", "newPassword");

        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> userService.updatePassword(user, passwordRequest));
        verify(userRepository, never()).save(any());
    }

    @DisplayName("update password, new password is the same as old password")
    @Test
    public void updatePasswordSameAsOldPassword(){
        PasswordRequest passwordRequest = new PasswordRequest("oldPassword", "oldPassword", "oldPassword");

        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> userService.updatePassword(user, passwordRequest));
        verify(userRepository, never()).save(any());
    }
}
