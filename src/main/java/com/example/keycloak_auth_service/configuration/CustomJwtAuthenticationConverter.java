package com.example.keycloak_auth_service.configuration;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomJwtAuthenticationConverter extends JwtAuthenticationConverter {

    public CustomJwtAuthenticationConverter() {
        this.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        Collection<GrantedAuthority> authorities = grantedAuthoritiesConverter.convert(jwt);
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");

        Map<String, Object> userClient = (Map<String, Object>) resourceAccess.get("user-client");
        Collection<String> roles = (Collection) userClient.get("roles");
        if (roles != null) {
            authorities.addAll(roles.stream()
                    .map(role -> "ROLE_" + role.toUpperCase())

                            .map(role -> {
                                return new SimpleGrantedAuthority(role);
                            })
                    .collect(Collectors.toList()));
        }

        return authorities;
    }
}
