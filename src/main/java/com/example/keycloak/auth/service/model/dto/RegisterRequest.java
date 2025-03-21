package com.example.keycloak.auth.service.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @Size(min = 8, max = 20)
    @NotEmpty
    private String username;

    @Size(min = 5, max = 50)
    @NotEmpty
    @Email
    private String email;

    @Size(min = 8, max = 20)
    @NotEmpty
    private String password;

    @NotEmpty
    private String firstName;

    @NotEmpty
    private String lastName;
}
