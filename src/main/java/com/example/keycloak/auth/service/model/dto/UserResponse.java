package com.example.keycloak.auth.service.model.dto;

import com.example.keycloak.auth.service.util.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String patronymic;
    private String email;
    private String number;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtil.DATE_PATTERN_FORMAT)
    private LocalDate birthdate;
    private ImageUrlDTO image;
    private String role;
    private Boolean enabled;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtil.DATE_TIME_PATTERN_FORMAT)
    private LocalDateTime creationDate;
}