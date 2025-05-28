package com.example.keycloak_auth_service.configuration;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfiguration {
    @Value("${app.keycloak.admin.username}")
    private String username;
    @Value("${app.keycloak.admin.password}")
    private String password;
    @Value("${app.keycloak.admin.realm}")
    private String realm;
    @Value("${app.keycloak.server-url}")
    private String serverUrl;
    @Value("${app.keycloak.admin.client-id}")
    private String clientId;

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .username(username)
                .password(password)
                .clientId(clientId)
                .grantType("password")
                .realm(realm)
                .serverUrl(serverUrl)
                .build();
    }
}