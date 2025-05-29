package com.example.keycloak_auth_service.exception;

import lombok.Getter;

@Getter
public class KeycloakException extends RuntimeException {
    private final int status;

    public KeycloakException(int status) {
        this.status = status;
    }
}