package com.example.keycloak.auth.service.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtParserUtil {
    private final JwtDecoder jwtDecoder;
    private static final String EMAIL = "email";

    public String extractEmailFromJwt(String tokenString) {
        Jwt jwt = jwtDecoder.decode(tokenString.substring(7));
        String target = jwt.getClaim(EMAIL);
        if (target == null) {
            throw new IllegalArgumentException("Некорректное значение поля в токене: " + EMAIL);
        }
        return target;
    }

    public String extractRoleFromJwt(String tokenString) {
        Jwt jwt = jwtDecoder.decode(tokenString.substring(7));
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        Map<String, Object> userClient = (Map<String, Object>) resourceAccess.get("user-client");
        Collection<String> roles = (Collection) userClient.get("roles");
        return roles.stream().limit(1).toList().getFirst();
    }
}