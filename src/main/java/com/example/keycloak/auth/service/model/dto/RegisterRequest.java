package com.example.keycloak.auth.service.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class RegisterRequest {
    @NotEmpty
    private String firstName;

    @NotEmpty
    private String lastName;

    private String patronymic;

    @Size(min = 5, max = 50)
    @NotEmpty
    @Email
    private String email;

    @Size(min = 8, max = 20)
    @NotEmpty
    private String password;

    private String number;

    private OffsetDateTime birthdate;

    private ImageUrlDTO image;
}
