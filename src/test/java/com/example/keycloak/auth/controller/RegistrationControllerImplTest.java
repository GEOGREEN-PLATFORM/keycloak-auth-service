package com.example.keycloak.auth.controller;

import com.example.keycloak.auth.service.controller.impl.RegistrationControllerImpl;
import com.example.keycloak.auth.service.model.dto.RegisterRequest;
import com.example.keycloak.auth.service.model.dto.UserResponse;
import com.example.keycloak.auth.service.model.entity.UserRole;
import com.example.keycloak.auth.service.service.RegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RegistrationControllerImplTest {

    private MockMvc mockMvc;

    @Mock
    private RegistrationService registrationService;

    @InjectMocks
    private RegistrationControllerImpl controller;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createUser() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@example.com");
        req.setPassword("password123");
        req.setFirstName("firstName");
        req.setLastName("lastName");


        UserResponse resp = new UserResponse();
        when(registrationService.createUser(eq(req), eq(UserRole.user))).thenReturn(resp);

        mockMvc.perform(post("/user/register/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        verify(registrationService).createUser(eq(req), eq(UserRole.user));
    }

    @Test
    void createOperator_withAdminRole() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("op@example.com");
        req.setPassword("password123");
        req.setFirstName("firstName");
        req.setLastName("lastName");
        UserResponse resp = new UserResponse();
        when(registrationService.createUser(eq(req), eq(UserRole.operator))).thenReturn(resp);

        mockMvc.perform(post("/user/register/operator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        verify(registrationService).createUser(eq(req), eq(UserRole.operator));
    }

    @Test
    void createAdmin_withAdminRole() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("adm@example.com");
        req.setPassword("password123");
        req.setFirstName("firstName");
        req.setLastName("lastName");
        UserResponse resp = new UserResponse();
        when(registrationService.createUser(eq(req), eq(UserRole.admin))).thenReturn(resp);

        mockMvc.perform(post("/user/register/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        verify(registrationService).createUser(eq(req), eq(UserRole.admin));
    }

    @Test
    void verifyEmail_withUserRole() throws Exception {
        String email = "verify@example.com";
        String token = "Bearer tokenvalue";

        mockMvc.perform(post("/user/register/verify-email/{email}", email)
                        .header("Authorization", token))
                .andExpect(status().isOk());

        verify(registrationService).sendVerificationEmail(eq(token), eq(email));
    }

    @Test
    void changeEnabledStatus_withAdminRole() throws Exception {
        String email = "status@example.com";
        boolean isEnabled = true;

        mockMvc.perform(post("/user/register/{email}/enabled/{isEnabled}", email, isEnabled))
                .andExpect(status().isOk());

        verify(registrationService).changeEnableStatus(eq(email), eq(isEnabled));
    }

    @Test
    void forgotPassword() throws Exception {
        String email = "forgot@example.com";

        mockMvc.perform(post("/user/register/forgot-password/{email}", email))
                .andExpect(status().isOk());

        verify(registrationService).forgotPassword(eq(email));
    }
}