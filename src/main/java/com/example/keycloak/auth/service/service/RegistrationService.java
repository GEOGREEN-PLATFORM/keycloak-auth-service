package com.example.keycloak.auth.service.service;

import com.example.keycloak.auth.service.model.dto.RegisterRequest;

public interface RegistrationService {
    void createUser(RegisterRequest request);
    void createOperator(RegisterRequest request);
    void sendVerificationEmail(String userId);
}
