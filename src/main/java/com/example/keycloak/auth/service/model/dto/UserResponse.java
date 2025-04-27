package com.example.keycloak.auth.service.model.dto;

import com.example.keycloak.auth.service.util.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static com.example.keycloak.auth.service.util.DateUtil.ISO_8601_DATE_TIME_MILLIS_PATTERN;
import static com.example.keycloak.auth.service.util.DateUtil.UTC;

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
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = ISO_8601_DATE_TIME_MILLIS_PATTERN,
            timezone = UTC
    )
    private OffsetDateTime creationDate;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = ISO_8601_DATE_TIME_MILLIS_PATTERN,
            timezone = UTC
    )
    private OffsetDateTime updateDate;
}