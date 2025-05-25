package com.example.keycloak.auth.mapper;


import com.example.keycloak.auth.service.mapper.UserMapper;
import com.example.keycloak.auth.service.model.dto.ImageUrlDTO;
import com.example.keycloak.auth.service.model.dto.RegisterRequest;
import com.example.keycloak.auth.service.model.dto.UserResponse;
import com.example.keycloak.auth.service.model.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private UserMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(UserMapper.class);
    }

    @Test
    void testToUserRepresentation() {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setEmail("john.doe@example.com");
        req.setNumber("1234567890");
        req.setPatronymic("Michael");
        OffsetDateTime dt = OffsetDateTime.of(1990, 5, 20, 15, 30, 0, 0, ZoneOffset.UTC);
        req.setBirthdate(dt);
        ImageUrlDTO img = new ImageUrlDTO(UUID.randomUUID(), UUID.randomUUID());
        req.setImage(img);

        org.keycloak.representations.idm.UserRepresentation rep = mapper.toUserRepresentation(req);

        assertThat(rep.getFirstName()).isEqualTo("John");
        assertThat(rep.getLastName()).isEqualTo("Doe");
        assertThat(rep.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(rep.isEnabled()).isTrue();
        assertThat(rep.isEmailVerified()).isFalse();
    }

    @Test
    void testToUserEntity() {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Alice");
        req.setLastName("Smith");
        req.setEmail("alice.smith@example.com");
        req.setNumber("0987654321");
        req.setPatronymic("Maria");
        OffsetDateTime odt = OffsetDateTime.of(1985, 12, 10, 0, 0, 0, 0, ZoneOffset.UTC);
        req.setBirthdate(odt);
        ImageUrlDTO img = new ImageUrlDTO(UUID.randomUUID(), UUID.randomUUID());
        req.setImage(img);

        org.keycloak.representations.idm.UserRepresentation rep = new org.keycloak.representations.idm.UserRepresentation();
        rep.setEnabled(false);

        UserEntity entity = mapper.toUserEntity(req, rep);

        assertThat(entity.getFirstName()).isEqualTo("Alice");
        assertThat(entity.getLastName()).isEqualTo("Smith");
        assertThat(entity.getEmail()).isEqualTo("alice.smith@example.com");
        assertThat(entity.getNumber()).isEqualTo("0987654321");
        assertThat(entity.getPatronymic()).isEqualTo("Maria");
        assertThat(entity.getBirthdate()).isEqualTo(LocalDate.of(1985, 12, 10));
        assertThat(entity.getImage()).isEqualTo(img);
        assertThat(entity.getEnabled()).isFalse();
        assertThat(entity.getIsEmailVerified()).isFalse();
    }

    @Test
    void testToUserResponse() {
        UserEntity entity = UserEntity.builder()
                .id(UUID.randomUUID())
                .firstName("Bob")
                .lastName("Brown")
                .patronymic("Lee")
                .email("bob.brown@example.com")
                .number("5551234")
                .birthdate(LocalDate.of(1975, 7, 4))
                .image(new ImageUrlDTO(UUID.randomUUID(), UUID.randomUUID()))
                .role("admin")
                .enabled(true)
                .creationDate(OffsetDateTime.of(2020, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC))
                .updateDate(OffsetDateTime.of(2020, 1, 2, 12, 30, 0, 0, ZoneOffset.UTC))
                .isEmailVerified(true)
                .build();

        UserResponse resp = mapper.toUserResponse(entity);

        assertThat(resp.getId()).isEqualTo(entity.getId());
        assertThat(resp.getFirstName()).isEqualTo("Bob");
        assertThat(resp.getLastName()).isEqualTo("Brown");
        assertThat(resp.getPatronymic()).isEqualTo("Lee");
        assertThat(resp.getEmail()).isEqualTo("bob.brown@example.com");
        assertThat(resp.getNumber()).isEqualTo("5551234");
        assertThat(resp.getBirthdate()).isEqualTo(
                OffsetDateTime.of(1975, 7, 4, 0, 0, 0, 0, ZoneOffset.UTC)
        );
        assertThat(resp.getImage()).isEqualTo(entity.getImage());
        assertThat(resp.getRole()).isEqualTo("admin");
        assertThat(resp.getEnabled()).isTrue();
        assertThat(resp.getCreationDate()).isEqualTo(entity.getCreationDate());
        assertThat(resp.getUpdateDate()).isEqualTo(entity.getUpdateDate());
        assertThat(resp.getIsEmailVerified()).isTrue();
    }
}