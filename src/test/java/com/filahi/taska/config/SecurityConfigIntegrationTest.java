package com.filahi.taska.config;

import com.filahi.taska.entity.User;
import com.filahi.taska.repository.UserRepository;
import com.filahi.taska.service.impl.JwtServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
public class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;


    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${spring.jwt.secret}")
    private String jwtSecret;

    @DisplayName("Should permit access to public endpoints without authentication")
    @Test
    public void shouldPermitAccessToPublicEndpointsWithoutAuthentication() throws Exception {
        String[] publicEndpoints = {
                "/api/auth/login",
                "/swagger-ui/index.html",
                "/v3/api-docs",
                "/swagger/resources",
                "/webjars",
                "/docs",
        };

        for(String endpoint: publicEndpoints){
            mockMvc.perform(MockMvcRequestBuilders.get(endpoint))
                    .andExpect(status().is(not(HttpStatus.UNAUTHORIZED.value())));
        }
    }

    @DisplayName("Should return 401 when accessing protected endpoint without authentication")
    @Test
    public void shouldReturn401WhenAccessingProtectedEndpointWithoutAuthentication() throws Exception {
        String protectedEndpoint = "/api/notes";

        mockMvc.perform(MockMvcRequestBuilders.get(protectedEndpoint))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Should permit access to protected endpoint with valid token")
    @Test
    public void shouldPermitAccessToProtectedEndpointWithValidToken() throws Exception {
        User user = new User(1L, "Mario", "Rossi", "mario.rossi@example.com", "Password123!", List.of());

        when(userRepository.findByEmail("mario.rossi@example.com")).thenReturn(Optional.of(user));

        JwtServiceImpl jwtService = new JwtServiceImpl();
        ReflectionTestUtils.setField(jwtService, "SECRET_KEY", jwtSecret);
        ReflectionTestUtils.setField(jwtService, "JWT_EXPIRATION", 86400000L);

        String token = jwtService.generateToken(new HashMap<>(), user);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/notes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(not(HttpStatus.UNAUTHORIZED.value())))
                .andExpect(status().is(not(HttpStatus.FORBIDDEN.value())));
    }

    @DisplayName("Should succeed on POST request without CSRF token")
    @Test
    public void shouldSucceedOnPostRequestWithoutCsrfToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/notes/note/1"))
                .andExpect(status().is(not(HttpStatus.FORBIDDEN.value())));
    }

    @DisplayName("Should not create session after successful request")
    @Test
    public void shouldNotCreateSessionAfterSuccessfulRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/notes"))
                .andExpect(request().sessionAttributeDoesNotExist("JSESSIONID"));
    }
}
