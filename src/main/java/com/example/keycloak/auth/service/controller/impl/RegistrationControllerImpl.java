package com.example.keycloak.auth.service.controller.impl;

import com.example.keycloak.auth.service.controller.RegistrationController;
import com.example.keycloak.auth.service.model.dto.RegisterRequest;
import com.example.keycloak.auth.service.service.RegistrationServiceImpl;
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
@RequestMapping("/auth")
public class RegistrationControllerImpl implements RegistrationController {
    private final RegistrationServiceImpl registrationServiceImpl;

    @PostMapping("/register-user")
    @Override
    public ResponseEntity<Void> createUser(@Valid @RequestBody RegisterRequest request) {
        registrationServiceImpl.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/register-operator")
    @Override
    public ResponseEntity<Void> createOperator(@Valid @RequestBody RegisterRequest request) {
        registrationServiceImpl.createOperator(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/verify-email/{email}")
    @Override
    public ResponseEntity<Void> email(@PathVariable("email") String id) {
        registrationServiceImpl.sendVerificationEmail(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}