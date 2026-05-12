package com.filahi.taska.service;

import com.filahi.taska.entity.User;
import com.filahi.taska.repository.UserRepository;
import com.filahi.taska.request.RegisterRequest;
import com.filahi.taska.service.impl.AuthenticationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private User user;
    private final String EMAIL = "mario.rossi@example.com";
    private final String PASSWORD = "Password123!";
    private final String TOKEN = "MOCK-TOKEN";
    private RegisterRequest registerRequest;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setEmail(EMAIL);
        user.setPassword(PASSWORD);

        registerRequest = new RegisterRequest(
                "Mario",
                "Rossi",
                EMAIL,
                PASSWORD
        );
    }


    @DisplayName("Should sign in user successfully")
    @Test
    public void shouldSignInUserSuccessfully() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(new HashMap<>(), user)).thenReturn(TOKEN);

        String result = authenticationService.signIn(EMAIL, PASSWORD);

        assertEquals(TOKEN, result);
    }

    @DisplayName("Should authenticate user using authentication manager")
    @Test
    public void shouldAuthenticateUserUsingAuthenticationManager() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(new HashMap<>(), user)).thenReturn(TOKEN);

        authenticationService.signIn(EMAIL, PASSWORD);

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(EMAIL, PASSWORD)
        );
    }

    @DisplayName("Should throw exception when user does not exist")
    @Test
    public void shouldThrowExceptionWhenUserDoesNotExist() {
        when(userRepository.findByEmail(EMAIL))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid credentials"));

        assertThrows(ResponseStatusException.class, () -> authenticationService.signIn(EMAIL, PASSWORD));
    }

    @DisplayName("Should not generate token when user is not found")
    @Test
    public void shouldNotGenerateTokenWhenUserIsNotFound() {
        when(userRepository.findByEmail(EMAIL))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid credentials"));

        assertThrows(ResponseStatusException.class, () -> authenticationService.signIn(EMAIL, PASSWORD));

        verify(jwtService, never()).generateToken(any(), any());
    }

    @DisplayName("Should sign up user successfully")
    @Test
    public void shouldSignUpUserSuccessfully() {
        when(userRepository.findByEmail(registerRequest.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.password())).thenReturn(TOKEN);

        authenticationService.signUp(registerRequest);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();

        assertAll(
                () -> assertEquals(0, savedUser.getId()),
                () -> assertEquals(registerRequest.firstName(), savedUser.getFirstName()),
                () -> assertEquals(registerRequest.lastName(), savedUser.getLastName()),
                () -> assertEquals(TOKEN, savedUser.getPassword()),
                () -> assertEquals("ROLE_USER", savedUser.getAuthorities().get(0).getAuthority())
        );
    }

    @DisplayName("Should throw exception when email already exists")
    @Test
    public void shouldThrowExceptionWhenEmailAlreadyExists() {
        when(userRepository.findByEmail(registerRequest.email()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists"));

        assertThrows(ResponseStatusException.class, () -> authenticationService.signUp(registerRequest));
    }
}
