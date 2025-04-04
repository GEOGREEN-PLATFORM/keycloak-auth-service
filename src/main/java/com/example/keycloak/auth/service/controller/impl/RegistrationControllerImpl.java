package com.example.keycloak.auth.service.controller.impl;

import com.example.keycloak.auth.service.model.dto.RegisterRequest;
import com.example.keycloak.auth.service.model.dto.UserResponse;
import com.example.keycloak.auth.service.model.entity.UserRole;
import com.example.keycloak.auth.service.service.RegistrationServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/register")
@Tag(name = "/register", description = "Регистрация пользователей")
public class RegistrationControllerImpl{
    private final RegistrationServiceImpl registrationServiceImpl;

    @PostMapping("/user")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody RegisterRequest request) {
        var response = registrationServiceImpl.createUser(request, UserRole.user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @RolesAllowed({"admin"})
    @PostMapping("/operator")
    public ResponseEntity<UserResponse> createOperator(@Valid @RequestBody RegisterRequest request) {
        var response = registrationServiceImpl.createUser(request, UserRole.operator);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/admin")
    public ResponseEntity<UserResponse> createAdmin(@Valid @RequestBody RegisterRequest request) {
        var response = registrationServiceImpl.createUser(request, UserRole.admin);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/verify-email/{email}")
    public ResponseEntity<Void> verifyEmail(@PathVariable("email") String id) {
        registrationServiceImpl.sendVerificationEmail(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

//    ./
}