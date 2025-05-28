package com.example.keycloak_auth_service.controller;


import com.example.keycloak_auth_service.controller.impl.UserControllerImpl;
import com.example.keycloak_auth_service.model.dto.ListUsersResponse;
import com.example.keycloak_auth_service.model.dto.UserRequest;
import com.example.keycloak_auth_service.model.dto.UserResponse;
import com.example.keycloak_auth_service.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerImplTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserControllerImpl controller;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("PATCH /user/search/{email} should return OK and call updateUser")
    void updateUser() throws Exception {
        String token = "Bearer tokenval";
        String email = "user@example.com";
        UserRequest req = new UserRequest();
        req.setFirstName("New");
        UserResponse resp = new UserResponse();
        when(userService.updateUser(eq(token), eq(email), any(UserRequest.class))).thenReturn(resp);

        mockMvc.perform(patch("/user/search/{email}", email)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(userService).updateUser(eq(token), eq(email), any(UserRequest.class));
    }

    @Test
    @DisplayName("GET /user/search/by-email/{email} should return OK and call getUserByEmail")
    void getUserByEmail() throws Exception {
        String email = "byemail@example.com";
        UserResponse resp = new UserResponse();
        when(userService.getUserByEmail(email)).thenReturn(resp);

        mockMvc.perform(get("/user/search/by-email/{email}", email))
                .andExpect(status().isOk());

        verify(userService).getUserByEmail(email);
    }

    @Test
    @DisplayName("GET /user/search/by-id/{id} should return OK and call getUserById")
    void getUserById() throws Exception {
        UUID id = UUID.randomUUID();
        UserResponse resp = new UserResponse();
        when(userService.getUserById(id)).thenReturn(resp);

        mockMvc.perform(get("/user/search/by-id/{id}", id))
                .andExpect(status().isOk());

        verify(userService).getUserById(id);
    }

    @Test
    @DisplayName("GET /user/search?page=&size= should return OK and call getUsers")
    void getUsers() throws Exception {
        int page = 1;
        int size = 5;
        String search = "term";
        String role = "user";
        String status = "active";
        LocalDate fromDate = LocalDate.of(2020, 1, 1);
        LocalDate toDate = LocalDate.of(2020, 12, 31);
        ListUsersResponse resp = new ListUsersResponse();
        when(userService.getUsers(eq(page), eq(size), eq(search), eq(role), eq(status), eq(fromDate), eq(toDate)))
                .thenReturn(resp);

        mockMvc.perform(get("/user/search")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .param("search", search)
                        .param("role", role)
                        .param("status", status)
                        .param("fromDate", fromDate.toString())
                        .param("toDate", toDate.toString()))
                .andExpect(status().isOk());

        verify(userService).getUsers(page, size, search, role, status, fromDate, toDate);
    }
}