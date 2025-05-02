package com.example.keycloak.auth.service.controller.impl;

import com.example.keycloak.auth.service.model.dto.RegisterRequest;
import com.example.keycloak.auth.service.model.dto.UserResponse;
import com.example.keycloak.auth.service.model.entity.UserRole;
import com.example.keycloak.auth.service.service.RegistrationServiceImpl;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.keycloak.auth.service.util.AuthorizationStringUtil.ADMIN;
import static com.example.keycloak.auth.service.util.AuthorizationStringUtil.AUTHORIZATION;
import static com.example.keycloak.auth.service.util.AuthorizationStringUtil.USER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/register")
@Tag(name = "/user/register", description = "Регистрация пользователей")
@SecurityRequirement(name = AUTHORIZATION)
public class RegistrationControllerImpl {
    private final RegistrationServiceImpl registrationServiceImpl;

    @PostMapping("/user")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody RegisterRequest request) {
        var response = registrationServiceImpl.createUser(request, UserRole.user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @RolesAllowed({ADMIN})
    @PostMapping("/operator")
    public ResponseEntity<UserResponse> createOperator(@Valid @RequestBody RegisterRequest request) {
        var response = registrationServiceImpl.createUser(request, UserRole.operator);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @RolesAllowed({ADMIN})
    @PostMapping("/admin")
    public ResponseEntity<UserResponse> createAdmin(@Valid @RequestBody RegisterRequest request) {
        var response = registrationServiceImpl.createUser(request, UserRole.admin);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @RolesAllowed({USER})
    @PostMapping("/verify-email/{email}")
    public ResponseEntity<Void> verifyEmail(@RequestHeader(AUTHORIZATION) String token, @PathVariable("email") String email) {
        registrationServiceImpl.sendVerificationEmail(token, email);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RolesAllowed({ADMIN})
    @PostMapping("/{email}/enabled/{isEnabled}")
    public ResponseEntity<Void> changeEnabledStatus(@PathVariable("email") String email, @PathVariable("isEnabled") Boolean isEnabled) {
        registrationServiceImpl.changeEnableStatus(email, isEnabled);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/forgot-password/{email}")
    public ResponseEntity<Void> forgotPassword(@PathVariable("email") String email) {
        registrationServiceImpl.forgotPassword(email);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}