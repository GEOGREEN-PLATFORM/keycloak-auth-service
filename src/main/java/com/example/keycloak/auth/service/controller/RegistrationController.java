package com.example.keycloak.auth.service.controller;

import com.example.keycloak.auth.service.model.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "/auth", description = "Регистрация пользователей")
public interface RegistrationController {
    ResponseEntity<Void> createUser(RegisterRequest request);

    ResponseEntity<Void> createOperator(RegisterRequest request);

    ResponseEntity<Void> email(String email);
}
