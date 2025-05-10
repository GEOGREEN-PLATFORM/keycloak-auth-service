package com.example.keycloak.auth.service.service;

import com.example.keycloak.auth.service.exception.CustomAccessDeniedException;
import com.example.keycloak.auth.service.exception.KeycloakException;
import com.example.keycloak.auth.service.mapper.UserMapper;
import com.example.keycloak.auth.service.model.dto.RegisterRequest;
import com.example.keycloak.auth.service.model.dto.UserResponse;
import com.example.keycloak.auth.service.model.entity.UserRole;
import com.example.keycloak.auth.service.repository.UserRepository;
import com.example.keycloak.auth.service.util.JwtParserUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.example.keycloak.auth.service.util.ExceptionStringUtil.USER_NOT_FOUND;


@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    @Value("${app.keycloak.user-realm.name}")
    private String realm;
    @Value("${app.keycloak.user-realm.client-id}")
    private String clientId;
    @Value("${app.keycloak.mail.verification-time}")
    private Long verificationTime;

    private final Keycloak keycloak;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final JwtParserUtil jwtParserUtil;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public UserResponse createUser(RegisterRequest request, UserRole userRole) {
        var userRepresentation = saveUserToKeycloak(request, userRole.name());
        return saveUserToDB(request, userRepresentation, userRole.name());
    }

    private UserRepresentation saveUserToKeycloak(RegisterRequest request, String role) {
        UserRepresentation userRepresentation = userMapper.toUserRepresentation(request);

        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setValue(request.getPassword());
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);

        userRepresentation.setCredentials(List.of(credentialRepresentation));

        UsersResource usersResource = getUsersResource();

        try (Response response = usersResource.create(userRepresentation)) {
            if (!Objects.equals(201, response.getStatus())) {
                throw new KeycloakException(response.getStatus());
            }
        }
        userRepresentation = getUserRepresentation(usersResource, request.getEmail());
        setUserRoles(userRepresentation, role);
        return userRepresentation;
    }

    private UserResponse saveUserToDB(RegisterRequest request, UserRepresentation userRepresentation, String role) {
        var userEntity = userMapper.toUserEntity(request, userRepresentation);
        userEntity.setRole(role);
        return userMapper.toUserResponse(userRepository.save(userEntity));
    }

    private UserRepresentation getUserRepresentation(UsersResource usersResource, String email) {
        List<UserRepresentation> userRepresentations = usersResource.searchByUsername(email, true);
        return userRepresentations.getFirst();
    }

    private void setUserRoles(UserRepresentation userRepresentation, String role) {
        var clientRepresentation = keycloak.realm(realm).clients().findByClientId(clientId).getFirst();
        var clientRoles = keycloak.realm(realm).clients()
                .get(clientRepresentation.getId())
                .roles().list();
        List<RoleRepresentation> rolesToAdd = clientRoles.stream()
                .filter(role1 -> role1.getName().equals(role))
                .collect(Collectors.toList());

        keycloak.realm(realm).users()
                .get(userRepresentation.getId())
                .roles()
                .clientLevel(clientRepresentation.getId())
                .add(rolesToAdd);
    }

    public void changeEnableStatus(String email, boolean isEnabled) {
        var userEntity = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));
        List<UserRepresentation> userRepresentations = getUsersResource()
                .searchByUsername(email, true);
        UserRepresentation userRepresentation1 = userRepresentations.get(0);
        userRepresentation1.setEnabled(isEnabled);
        getUsersResource().get(userRepresentation1.getId()).update(userRepresentation1);
        userEntity.setEnabled(isEnabled);
        userRepository.save(userEntity);
    }

    public void sendVerificationEmail(String token, String email) {
        if (!jwtParserUtil.extractEmailFromJwt(token).equals(email)) {
            throw new CustomAccessDeniedException("Недостаточно прав");
        }
        userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));
        UsersResource usersResource = getUsersResource();
        List<UserRepresentation> userRepresentations = usersResource.searchByUsername(email, true);
        UserRepresentation userRepresentation = userRepresentations.getFirst();
        usersResource.get(userRepresentation.getId()).sendVerifyEmail();

        scheduler.schedule(() -> updateVerification(email), verificationTime, TimeUnit.MINUTES);
    }

    private UserRepresentation getUserRepresentation(String email) {
        UsersResource usersResource = getUsersResource();
        List<UserRepresentation> userRepresentations = usersResource.searchByUsername(email, true);
        return userRepresentations.getFirst();
    }

    private void updateVerification(String email) {
        UserRepresentation userRepresentation = getUserRepresentation(email);
        var isEmailVerified = userRepresentation.isEmailVerified();
        if (isEmailVerified) {
            var userEntity = userRepository.findByEmail(email).orElseThrow();
            userEntity.setIsEmailVerified(true);
            userRepository.save(userEntity);
        }
    }

    public void forgotPassword(String email) {
        userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));
        UsersResource usersResource = getUsersResource();
        List<UserRepresentation> userRepresentations = usersResource.searchByUsername(email, true);
        UserRepresentation userRepresentation = userRepresentations.getFirst();
        UserResource userResource = usersResource.get(userRepresentation.getId());
        userResource.executeActionsEmail(List.of("UPDATE_PASSWORD"));
    }

    private UsersResource getUsersResource() {
        return keycloak.realm(realm).users();
    }
}