package com.example.keycloak_auth_service.controller.impl;

import com.example.keycloak_auth_service.model.dto.RegisterRequest;
import com.example.keycloak_auth_service.model.dto.UserResponse;
import com.example.keycloak_auth_service.model.entity.UserRole;
import com.example.keycloak_auth_service.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
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

import static com.example.keycloak_auth_service.util.AuthorizationStringUtil.ADMIN;
import static com.example.keycloak_auth_service.util.AuthorizationStringUtil.AUTHORIZATION;
import static com.example.keycloak_auth_service.util.AuthorizationStringUtil.USER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/register")
@Tag(name = "/user/register", description = "Регистрация пользователей")
@SecurityRequirement(name = AUTHORIZATION)
public class RegistrationControllerImpl {
    private final RegistrationService registrationService;

    @Operation(
            summary = "Регистрация пользователя с ролью user"
    )
    @PostMapping("/user")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody RegisterRequest request) {
        var response = registrationService.createUser(request, UserRole.user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Регистрация пользователя с ролью operator"
    )
    @RolesAllowed({ADMIN})
    @PostMapping("/operator")
    public ResponseEntity<UserResponse> createOperator(@Valid @RequestBody RegisterRequest request) {
        var response = registrationService.createUser(request, UserRole.operator);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Регистрация пользователя с ролью admin"
    )
    @RolesAllowed({ADMIN})
    @PostMapping("/admin")
    public ResponseEntity<UserResponse> createAdmin(@Valid @RequestBody RegisterRequest request) {
        var response = registrationService.createUser(request, UserRole.admin);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Верификация почты с помощью письма на почту"
    )
    @RolesAllowed({USER})
    @PostMapping("/verify-email/{email}")
    public ResponseEntity<Void> verifyEmail(@RequestHeader(AUTHORIZATION) String token, @PathVariable("email") String email) {
        registrationService.sendVerificationEmail(token, email);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(
            summary = "Блокировка/разблокировка пользователя"
    )
    @RolesAllowed({ADMIN})
    @PostMapping("/{email}/enabled/{isEnabled}")
    public ResponseEntity<Void> changeEnabledStatus(@PathVariable("email") String email, @PathVariable("isEnabled") Boolean isEnabled) {
        registrationService.changeEnableStatus(email, isEnabled);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(
            summary = "Сброс пароля с помощью письма на почту"
    )
    @PostMapping("/forgot-password/{email}")
    public ResponseEntity<Void> forgotPassword(@PathVariable("email") String email) {
        registrationService.forgotPassword(email);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}