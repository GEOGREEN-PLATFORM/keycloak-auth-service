package com.example.keycloak.auth.service.service;

import com.example.keycloak.auth.service.exception.KeycloakException;
import com.example.keycloak.auth.service.mapper.UserMapper;
import com.example.keycloak.auth.service.model.dto.RegisterRequest;
import com.example.keycloak.auth.service.model.dto.UserResponse;
import com.example.keycloak.auth.service.model.entity.UserRole;
import com.example.keycloak.auth.service.repository.UserRepository;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationServiceImpl {

    @Value("${app.keycloak.user-realm.name}")
    private String realm;

    @Value("${app.keycloak.user-realm.client-id}")
    private String clientId;
    private final Keycloak keycloak;
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public UserResponse createUser(RegisterRequest request, UserRole userRole) {
        var userRepresentation = saveUserToKeycloak(request, userRole.name());
        return saveUserToDB(request, userRepresentation, userRole.name());
    }


    public void createOperator(RegisterRequest request) {
        saveUserToKeycloak(request, "operator");
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

    private void enableUser() {
//        List<UserRepresentation> userRepresentations = usersResource.searchByUsername(newUserRecord.getUsername(), true);
//        UserRepresentation userRepresentation1 = userRepresentations.get(0);
//        userRepresentation1.setEnabled(false);
    }

    public void sendVerificationEmail(String userId) {
        UsersResource usersResource = getUsersResource();
        List<UserRepresentation> userRepresentations = usersResource.searchByUsername(userId, true);
        UserRepresentation userRepresentation1 = userRepresentations.get(0);
        var i = usersResource.get(userRepresentation1.getId());
        i.sendVerifyEmail();
    }


    private RolesResource getRolesResource() {

        return keycloak.realm(realm).roles();
    }

    public void deleteUser(String userId) {
        UsersResource usersResource = getUsersResource();
        usersResource.delete(userId);
    }


    public void forgotPassword(String username) {
        UsersResource usersResource = getUsersResource();
        List<UserRepresentation> userRepresentations = usersResource.searchByUsername(username, true);
        UserRepresentation userRepresentation1 = userRepresentations.get(0);
        UserResource userResource = usersResource.get(userRepresentation1.getId());
        userResource.executeActionsEmail(List.of("UPDATE_PASSWORD"));

    }


    public UserResource getUser(String userId) {
        UsersResource usersResource = getUsersResource();
        return usersResource.get(userId);
    }


    private UsersResource getUsersResource() {
        return keycloak.realm(realm).users();
    }
}
