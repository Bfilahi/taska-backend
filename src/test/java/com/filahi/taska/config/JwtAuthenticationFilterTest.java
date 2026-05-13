package com.filahi.taska.config;


import com.filahi.taska.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @Mock
    private FilterChain filterChain;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final String TOKEN = "mock-token";


    @AfterEach
    public void tearDown(){
        SecurityContextHolder.clearContext();
    }


    @DisplayName("Should continue filter chain when authorization header is null")
    @Test
    public void shouldContinueFilterChainWhenAuthorizationHeaderIsNull()
            throws ServletException, IOException {

        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(any());
        verifyNoInteractions(userDetailsService);
    }

    @DisplayName("Should continue filter chain when authorization header does not start with Bearer")
    @Test
    public void shouldContinueFilterChainWhenAuthorizationHeaderIsInvalid()
            throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(TOKEN);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(any());
        verifyNoInteractions(userDetailsService);
    }

    @DisplayName("Should authenticate user when token is valid")
    @Test
    public void shouldAuthenticateUserWhenTokenIsValid()
            throws ServletException, IOException {
        UserDetails userDetails = User.builder()
                .username("mario.rossi@example.com")
                .password("password")
                .roles("USER")
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + TOKEN);
        when(jwtService.extractUsername(TOKEN)).thenReturn("mario.rossi@example.com");
        when(userDetailsService.loadUserByUsername("mario.rossi@example.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid(TOKEN, userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService).extractUsername("mock-token");
        verify(userDetailsService).loadUserByUsername("mario.rossi@example.com");
        verify(jwtService).isTokenValid("mock-token", userDetails);
        verify(filterChain).doFilter(request, response);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
        assertEquals(userDetails, authentication.getPrincipal());
    }

    @DisplayName("Should not authenticate when extracted username is null")
    @Test
    public void shouldNotAuthenticateWhenUsernameIsNull()
            throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + TOKEN);
        when(jwtService.extractUsername(TOKEN)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verifyNoInteractions(userDetailsService);
    }

    @DisplayName("Should not authenticate when token is invalid")
    @Test
    public void shouldNotAuthenticateWhenTokenIsInvalid()
            throws ServletException, IOException {
        UserDetails userDetails = User.builder()
                .username("mario.rossi@example.com")
                .password("password")
                .roles("USER")
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer invalid");
        when(jwtService.extractUsername("invalid")).thenReturn("mario.rossi@example.com");
        when(jwtService.isTokenValid("invalid", userDetails)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
    }

    @DisplayName("Should set expired attribute when token is expired")
    @Test
    public void shouldSetExpiredAttributeWhenTokenIsExpired()
            throws ServletException, IOException {
        ExpiredJwtException expiredJwtException = mock(ExpiredJwtException.class);
        when(expiredJwtException.getMessage()).thenReturn("JWT token is expired");

        when(request.getHeader("Authorization")).thenReturn("Bearer " + TOKEN);
        when(jwtService.extractUsername(TOKEN)).thenThrow(expiredJwtException);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(request).setAttribute("expired", "JWT token is expired");
        verify(filterChain).doFilter(request, response);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
    }

    @DisplayName("Should set invalid attribute when token processing fails")
    @Test
    public void shouldSetInvalidAttributeWhenTokenIsInvalid()
            throws ServletException, IOException {
        RuntimeException exception = mock(RuntimeException.class);
        when(exception.getMessage()).thenReturn("JWT token is invalid");

        when(request.getHeader("Authorization")).thenReturn("Bearer " + TOKEN);
        when(jwtService.extractUsername(TOKEN)).thenThrow(exception);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(request).setAttribute("invalid", "JWT token is invalid");
        verify(filterChain).doFilter(request, response);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
    }
}
