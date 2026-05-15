package com.filahi.taska.controller;


import com.filahi.taska.entity.Authority;
import com.filahi.taska.request.PasswordRequest;
import com.filahi.taska.request.UserRequest;
import com.filahi.taska.response.UserResponse;
import com.filahi.taska.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private final String BASE_URL = "/api/user";


    private UserResponse buildUser(long userId) {
        return new UserResponse(
                userId,
                "mario",
                "rossi",
                "mario.rossi@example.com",
                List.of(new Authority("ROLE_USER"))
        );
    }

    @DisplayName("Should return user's info")
    @Test
    @WithMockUser
    public void getUserInfoTest() throws Exception {
        UserResponse user = buildUser(1L);

        when(userService.getUserInfo(any())).thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @DisplayName("Should update profile successfully")
    @Test
    @WithMockUser
    public void updateProfileTest() throws Exception {
        UserRequest userRequest = new UserRequest(
                "mario",
                "rossi",
                "mario.rossi@example.com"
        );

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/profile-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userRequest))
        )
                .andExpect(status().isOk());
    }

    @DisplayName("Should update password successfully")
    @Test
    @WithMockUser
    public void updatePasswordTest() throws Exception {
        PasswordRequest passwordRequest = new PasswordRequest(
                "oldPassword",
                "newPassword",
                "newPassword"
        );

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/password-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(passwordRequest))
        )
                .andExpect(status().isOk());
    }

    @DisplayName("Should delete user successfully")
    @Test
    @WithMockUser
    public void deleteUserTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL))
                .andExpect(status().isOk());
    }
}
