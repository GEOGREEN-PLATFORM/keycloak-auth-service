package com.example.keycloak_auth_service.service;

import com.example.keycloak_auth_service.exception.CustomAccessDeniedException;
import com.example.keycloak_auth_service.exception.KeycloakException;
import com.example.keycloak_auth_service.mapper.UserMapper;
import com.example.keycloak_auth_service.model.dto.RegisterRequest;
import com.example.keycloak_auth_service.model.entity.UserEntity;
import com.example.keycloak_auth_service.model.entity.UserRole;
import com.example.keycloak_auth_service.repository.UserRepository;
import com.example.keycloak_auth_service.service.impl.RegistrationServiceImpl;
import com.example.keycloak_auth_service.util.JwtParserUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegistrationServiceImplTest {

    @Mock
    private Keycloak keycloak;
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtParserUtil jwtParserUtil;

    @Mock
    private UsersResource usersResource;
    @Mock
    private UserResource userResource;
    @Mock
    private Response kcResponse;

    @Mock
    private ScheduledExecutorService scheduler;

    @InjectMocks
    private RegistrationServiceImpl registrationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(registrationService, "realm", "test-realm");
        ReflectionTestUtils.setField(registrationService, "clientId", "test-client");
        ReflectionTestUtils.setField(registrationService, "verificationTime", 15L);
        ReflectionTestUtils.setField(registrationService, "scheduler", scheduler);

        RealmResource realmResource = mock(RealmResource.class);
        ClientsResource clientsResource = mock(ClientsResource.class);
        lenient().when(keycloak.realm("test-realm")).thenReturn(realmResource);
        lenient().when(realmResource.users()).thenReturn(usersResource);
        lenient().when(realmResource.clients()).thenReturn(clientsResource);
        lenient().when(clientsResource.findByClientId("test-client"))
                .thenReturn(List.of(mock(org.keycloak.representations.idm.ClientRepresentation.class)));

    }

    @Test
    void testCreateUserThrowsKeycloakExceptionOnErrorStatus() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("user@example.com");
        req.setPassword("password123");

        when(userMapper.toUserRepresentation(req)).thenReturn(new UserRepresentation());
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(kcResponse);
        when(kcResponse.getStatus()).thenReturn(400);

        assertThatThrownBy(() -> registrationService.createUser(req, UserRole.user))
                .isInstanceOf(KeycloakException.class);
    }

    @Test
    void testChangeEnableStatusSuccessfully() {
        String email = "foo@bar.com";
        UserEntity entity = UserEntity.builder()
                .email(email)
                .enabled(false)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(entity));

        UserRepresentation kcUser = new UserRepresentation();
        kcUser.setId("1");
        when(usersResource.searchByUsername(email, true)).thenReturn(List.of(kcUser));
        lenient().when(usersResource.get("1")).thenReturn(userResource);

        registrationService.changeEnableStatus(email, true);

        assertThat(entity.getEnabled()).isTrue();
        verify(userRepository).save(entity);
        verify(userResource).update(kcUser);
    }

    @Test
    void testChangeEnableStatusUserNotFound() {
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationService.changeEnableStatus("x@x.com", true))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void testForgotPasswordSuccessfully() {
        String email = "test@abc.com";
        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(new UserEntity()));

        UserRepresentation kcUser = new UserRepresentation();
        kcUser.setId("uid");
        when(usersResource.searchByUsername(email, true)).thenReturn(List.of(kcUser));
        lenient().when(usersResource.get("uid")).thenReturn(userResource);

        registrationService.forgotPassword(email);

        verify(userResource).executeActionsEmail(List.of("UPDATE_PASSWORD"));
    }

    @Test
    void testForgotPasswordUserNotFound() {
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationService.forgotPassword("a@b.com"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void testSendVerificationEmailSchedulesUpdate() {
        String token = "token";
        String email = "mail@domain.com";

        when(jwtParserUtil.extractEmailFromJwt(token)).thenReturn(email);
        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(new UserEntity()));

        UserRepresentation kcUser = new UserRepresentation();
        kcUser.setId("u1");
        when(usersResource.searchByUsername(email, true))
                .thenReturn(List.of(kcUser));
        lenient().when(usersResource.get("u1")).thenReturn(userResource);

        registrationService.sendVerificationEmail(token, email);

        verify(userResource).sendVerifyEmail();
        verify(scheduler).schedule(any(Runnable.class), eq(15L), eq(TimeUnit.MINUTES));
    }

    @Test
    void testSendVerificationEmailAccessDenied() {
        when(jwtParserUtil.extractEmailFromJwt("t")).thenReturn("other@x.com");

        assertThatThrownBy(() -> registrationService.sendVerificationEmail("t", "x@x.com"))
                .isInstanceOf(CustomAccessDeniedException.class);
    }
}