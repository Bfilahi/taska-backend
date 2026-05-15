package com.filahi.taska.controller;


import com.filahi.taska.request.RegisterRequest;
import com.filahi.taska.service.AuthenticationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;


    private final String BASE_URL = "/api/auth";
    private final String EMAIL = "mario.rossi@example.com";
    private final String PASSWORD = "Password123!";

    @DisplayName("Should return token after sign in")
    @Test
    public void signInTest() throws Exception {
        when(authenticationService.signIn(EMAIL, PASSWORD)).thenReturn("mock token");

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/sign-in")
                .param("email", EMAIL)
                .param("password", PASSWORD)
        )
                .andExpect(status().isOk());
    }

    @DisplayName("Should sign up successfully")
    @Test
    public void signUpTest() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "mario",
                "rossi",
                EMAIL,
                PASSWORD
        );

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request))
        )
                .andExpect(status().isCreated());
    }
}
