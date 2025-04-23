package com.example.keycloak.auth.service.model.entity;

import com.example.keycloak.auth.service.model.dto.ImageUrlDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class UserEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "patronymic")
    private String patronymic;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "number")
    private String number;

    @Column(name = "birthdate")
    private LocalDate birthdate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "profile_photo_id")
    private ImageUrlDTO image;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @CreationTimestamp
    @Column(name = "creation_date")
    private LocalDateTime creationDate;
}