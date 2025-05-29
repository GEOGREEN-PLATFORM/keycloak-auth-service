package com.example.keycloak_auth_service.service;


import com.example.keycloak_auth_service.exception.CustomAccessDeniedException;
import com.example.keycloak_auth_service.mapper.UserMapper;
import com.example.keycloak_auth_service.model.dto.ListUsersResponse;
import com.example.keycloak_auth_service.model.dto.UserRequest;
import com.example.keycloak_auth_service.model.dto.UserResponse;
import com.example.keycloak_auth_service.model.entity.UserEntity;
import com.example.keycloak_auth_service.model.entity.UserRole;
import com.example.keycloak_auth_service.repository.UserRepository;
import com.example.keycloak_auth_service.service.impl.UserServiceImpl;
import com.example.keycloak_auth_service.util.JwtParserUtil;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private JwtParserUtil jwtParserUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private UserEntity existing;
    private String token;
    private String email;

    @BeforeEach
    void setUp() {
        email = "user@test.com";
        token = "token";
        existing = UserEntity.builder()
                .id(UUID.randomUUID())
                .email(email)
                .firstName("First")
                .lastName("Last")
                .patronymic("Patr")
                .number("123")
                .birthdate(LocalDate.of(2000, 1, 1))
                .role(UserRole.user.name())
                .enabled(true)
                .build();
    }

    @Test
    void testUpdateUserSelf() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existing));
        when(jwtParserUtil.extractEmailFromJwt(token)).thenReturn(email);
        UserRequest req = new UserRequest();
        req.setFirstName("NewFirst");
        req.setNumber("999");

        when(userRepository.saveAndFlush(existing)).thenReturn(existing);
        when(userMapper.toUserResponse(existing)).thenReturn(new UserResponse());

        UserResponse resp = userService.updateUser(token, email, req);
        assertThat(resp).isNotNull();
        assertThat(existing.getFirstName()).isEqualTo("NewFirst");
        assertThat(existing.getNumber()).isEqualTo("999");
    }

    @Test
    void testUpdateUserAdminOperator() {
        existing.setRole(UserRole.operator.name());
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existing));
        when(jwtParserUtil.extractEmailFromJwt(token)).thenReturn("admin@test.com");
        when(jwtParserUtil.extractRoleFromJwt(token)).thenReturn(UserRole.admin.name());

        UserRequest req = new UserRequest();
        req.setLastName("AdminMod");

        when(userRepository.saveAndFlush(existing)).thenReturn(existing);
        when(userMapper.toUserResponse(existing)).thenReturn(new UserResponse());

        UserResponse resp = userService.updateUser(token, email, req);
        assertThat(resp).isNotNull();
        assertThat(existing.getLastName()).isEqualTo("AdminMod");
    }

    @Test
    void testUpdateUserAccessDenied() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existing));
        when(jwtParserUtil.extractEmailFromJwt(token)).thenReturn("other@test.com");
        when(jwtParserUtil.extractRoleFromJwt(token)).thenReturn(UserRole.user.name());

        UserRequest req = new UserRequest();
        assertThatThrownBy(() -> userService.updateUser(token, email, req))
                .isInstanceOf(CustomAccessDeniedException.class);
    }

    @Test
    void testUpdateUserNotFound() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.updateUser(token, email, new UserRequest()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void testGetUserByEmail() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existing));
        when(userMapper.toUserResponse(existing)).thenReturn(new UserResponse());
        UserResponse resp = userService.getUserByEmail(email);
        assertThat(resp).isNotNull();
    }

    @Test
    void testGetUserByEmailNotFound() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUserByEmail(email))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void testGetUserById() {
        UUID id = existing.getId();
        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userMapper.toUserResponse(existing)).thenReturn(new UserResponse());
        UserResponse resp = userService.getUserById(id);
        assertThat(resp).isNotNull();
    }

    @Test
    void testGetUserByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUserById(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void testGetUsersPageable() {
        UserEntity u1 = existing;
        UserEntity u2 = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("other@test.com")
                .firstName("O")
                .lastName("T")
                .patronymic("")
                .role(UserRole.operator.name())
                .enabled(false)
                .build();
        List<UserEntity> list = List.of(u1, u2);
        Page<UserEntity> page = new PageImpl<>(list, PageRequest.of(0, 2), 2);

        when(userRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(page);
        when(userMapper.toUserResponse(u1)).thenReturn(new UserResponse());
        when(userMapper.toUserResponse(u2)).thenReturn(new UserResponse());

        ListUsersResponse resp = userService.getUsers(0, 2, null, null, null, null, null);
        assertThat(resp.getUsers()).hasSize(2);
        assertThat(resp.getCurrentPage()).isEqualTo(0);
        assertThat(resp.getTotalItems()).isEqualTo(2);
        assertThat(resp.getTotalPages()).isEqualTo(1);
    }

    @Test
    void testGetUsersWithFilters() {
        when(userRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(existing), PageRequest.of(1, 1), 1));
        when(userMapper.toUserResponse(existing)).thenReturn(new UserResponse());

        ListUsersResponse resp = userService.getUsers(
                1, 1, "First", UserRole.user.name(), "active",
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1)
        );
        assertThat(resp.getUsers()).hasSize(1);
        assertThat(resp.getCurrentPage()).isEqualTo(1);
        assertThat(resp.getTotalItems()).isEqualTo(2L);
        assertThat(resp.getTotalPages()).isEqualTo(2);
    }
}