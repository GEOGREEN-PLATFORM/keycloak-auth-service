package com.example.keycloak_auth_service.service;

import com.example.keycloak_auth_service.model.dto.RegisterRequest;
import com.example.keycloak_auth_service.model.dto.UserResponse;
import com.example.keycloak_auth_service.model.entity.UserRole;

public interface RegistrationService {
    UserResponse createUser(RegisterRequest request, UserRole userRole);

    void changeEnableStatus(String email, boolean isEnabled);

    void sendVerificationEmail(String token, String email);

    void forgotPassword(String email);
}