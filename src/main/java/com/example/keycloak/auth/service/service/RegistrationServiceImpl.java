package com.example.keycloak.auth.service.service;

import com.example.keycloak.auth.service.exception.KeycloakException;
import com.example.keycloak.auth.service.model.dto.RegisterRequest;
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
public class RegistrationServiceImpl implements RegistrationService {

    @Value("${app.keycloak.realm}")
    private String realm;
    private final Keycloak keycloak;


    public void createUser(RegisterRequest newUserRecord) {

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        userRepresentation.setFirstName(newUserRecord.getFirstName());
        userRepresentation.setLastName(newUserRecord.getLastName());
        userRepresentation.setEmail(newUserRecord.getEmail());
        userRepresentation.setUsername(newUserRecord.getUsername());
        userRepresentation.setEmailVerified(false);


        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setValue(newUserRecord.getPassword());
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);

        userRepresentation.setCredentials(List.of(credentialRepresentation));

        UsersResource usersResource = getUsersResource();

        System.out.println(keycloak);
        Response response = usersResource.create(userRepresentation);

        log.info("Status Code " + response.getStatus());

        if (!Objects.equals(201, response.getStatus())) {

            throw new KeycloakException(response.getStatus());
        }

        log.info("New user has bee created");

        List<UserRepresentation> userRepresentations = usersResource.searchByUsername(newUserRecord.getUsername(), true);
        UserRepresentation userRepresentation1 = userRepresentations.get(0);
//        sendVerificationEmail(userRepresentation1.getId());

        var client = keycloak.realm(realm).clients().findByClientId("user-client").getFirst();
        var clientRole = keycloak.realm(realm).clients()
                .get(client.getId())
                .roles().list();
        List<RoleRepresentation> rolesToAdd = clientRole.stream()
                .filter(role -> role.getName().equals("user"))
                .collect(Collectors.toList());

        keycloak.realm(realm).users()
                .get(userRepresentation1.getId())
                .roles()
                .clientLevel(client.getId())
                .add(rolesToAdd);
    }

    @Override
    public void createOperator(RegisterRequest request) {

    }

    @Override
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
