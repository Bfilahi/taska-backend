package com.filahi.taska.config;


import com.filahi.taska.entity.User;
import com.filahi.taska.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityConfigTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException authException;

    @Mock
    private PrintWriter writer;

    @Mock
    private HttpSecurity http;

    @InjectMocks
    private SecurityConfig securityConfig;


    private final String USERNAME = "mario.rossi@example.com";
    private final String frontendUrl = "http://localhost:4200";

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(securityConfig, "frontendUrl", "http://localhost:4200");
    }


    @DisplayName("Should return user details when user is found")
    @Test
    public void shouldReturnUserDetailsWhenUserIsFound() {
        User user = new User();
        user.setEmail(USERNAME);
        user.setPassword("Password123!");

        when(userRepository.findByEmail(USERNAME)).thenReturn(Optional.of(user));

        UserDetailsService userDetailsService  = securityConfig.userDetailsService();
        UserDetails result = userDetailsService.loadUserByUsername(USERNAME);

        assertNotNull(result);
        assertEquals(USERNAME, result.getUsername());
        verify(userRepository).findByEmail(USERNAME);
    }

    @DisplayName("Should throw UsernameNotFoundException when user is not found")
    @Test
    public void shouldThrowUsernameNotFoundExceptionWhenUserIsNotFound() {
        when(userRepository.findByEmail(USERNAME)).thenReturn(Optional.empty());

        UserDetailsService userDetailsService  = securityConfig.userDetailsService();

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(USERNAME));
    }

    @DisplayName("Should set response status to 401 when authentication fails")
    @Test
    public void shouldSetResponseStatusTo401WhenAuthenticationFails() throws IOException, ServletException {
        when(response.getWriter()).thenReturn(writer);

        AuthenticationEntryPoint authenticationEntryPoint = securityConfig.authenticationEntryPoint();
        authenticationEntryPoint.commence(request, response, authException);

        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
    }

    @DisplayName("Should set content type to application/json when authentication fails")
    @Test
    public void shouldSetContentTypeToApplicationJsonWhenAuthenticationFails() throws IOException, ServletException {
        when(response.getWriter()).thenReturn(writer);

        AuthenticationEntryPoint authenticationEntryPoint = securityConfig.authenticationEntryPoint();
        authenticationEntryPoint.commence(request, response, authException);

        verify(response).setContentType("application/json;charset=utf-8");
    }

    @DisplayName("Should clear WWW-Authenticate header when authentication fails")
    @Test
    public void shouldClearWwwAuthenticateHeaderWhenAuthenticationFails() throws IOException, ServletException {
        when(response.getWriter()).thenReturn(writer);

        AuthenticationEntryPoint authenticationEntryPoint = securityConfig.authenticationEntryPoint();
        authenticationEntryPoint.commence(request, response, authException);

        verify(response).setHeader("WWW-Authenticate", "");
    }

    @DisplayName("Should write unauthorized error message to response body when authentication fails")
    @Test
    public void shouldWriteUnauthorizedErrorMessageToResponseBodyWhenAuthenticationFails() throws IOException, ServletException {
        when(response.getWriter()).thenReturn(writer);

        AuthenticationEntryPoint authenticationEntryPoint = securityConfig.authenticationEntryPoint();
        authenticationEntryPoint.commence(request, response, authException);

        verify(response.getWriter()).write("{\"error\":\"Unauthorized access\"}");
    }

    @DisplayName("Should allow only frontend URL as origin")
    @Test
    public void shouldAllowOnlyFrontendUrlAsOrigin() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/");

        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        CorsConfiguration corsConfiguration = corsConfigurationSource.getCorsConfiguration(request);

        assertNotNull(corsConfiguration);
        assertEquals(List.of(frontendUrl), corsConfiguration.getAllowedOrigins());
    }

    @DisplayName("Should allow GET, POST, PUT, DELETE and OPTIONS methods")
    @Test
    public void shouldAllowGetPostPutDeleteAndOptionsMethods() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/");

        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        CorsConfiguration corsConfiguration = corsConfigurationSource.getCorsConfiguration(request);

        assertNotNull(corsConfiguration);
        assertEquals(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"), corsConfiguration.getAllowedMethods());
    }

    @DisplayName("Should allow all headers")
    @Test
    public void shouldAllowAllHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/");

        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        CorsConfiguration corsConfiguration = corsConfigurationSource.getCorsConfiguration(request);

        assertNotNull(corsConfiguration);
        assertEquals(List.of("*"), corsConfiguration.getAllowedHeaders());
    }

    @DisplayName("Should allow credentials")
    @Test
    public void shouldAllowCredentials() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/");

        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        CorsConfiguration corsConfiguration = corsConfigurationSource.getCorsConfiguration(request);

        assertNotNull(corsConfiguration);
        assertEquals(true, corsConfiguration.getAllowCredentials());
    }

    @DisplayName("Should register CORS configuration for all paths")
    @Test
    public void shouldRegisterCorsConfigurationForAllPaths() {
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        String[] paths = {"/", "/api/notes", "/api/auth/login", "/swagger-ui/index.html"};

        for (String path : paths) {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setServletPath(path);

            CorsConfiguration corsConfiguration = corsConfigurationSource.getCorsConfiguration(request);
            assertNotNull(corsConfiguration, "CORS configuration should be registered for path: " + path);
        }
    }
}
