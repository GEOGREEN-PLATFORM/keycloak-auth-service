package com.example.keycloak.auth.util;


import com.example.keycloak.auth.service.util.JwtParserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtParserUtilTest {

    @Mock
    private JwtDecoder jwtDecoder;
    @Mock
    private Jwt jwt;

    @InjectMocks
    private JwtParserUtil jwtParserUtil;

    private final String rawToken = "Bearer abcdef";
    private final String tokenValue = "abcdef";

    @BeforeEach
    void setUp() {
        when(jwtDecoder.decode(eq(tokenValue))).thenReturn(jwt);
    }

    @Test
    @DisplayName("extractEmailFromJwt returns email claim when present")
    void extractEmailFromJwt_success() {
        when(jwt.getClaim("email")).thenReturn("user@example.com");

        String email = jwtParserUtil.extractEmailFromJwt(rawToken);

        assertThat(email).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("extractEmailFromJwt throws IllegalArgumentException when email claim missing")
    void extractEmailFromJwt_missingClaim() {
        when(jwt.getClaim("email")).thenReturn(null);

        assertThatThrownBy(() -> jwtParserUtil.extractEmailFromJwt(rawToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Некорректное значение поля в токене: email");
    }

    @Test
    @DisplayName("extractRoleFromJwt returns first role from resource_access.user-client.roles")
    void extractRoleFromJwt_success() {
        Map<String, Object> userClient = Map.of(
                "roles", List.of("role1", "role2")
        );
        Map<String, Object> resourceAccess = Map.of(
                "user-client", userClient
        );
        when(jwt.getClaim("resource_access")).thenReturn(resourceAccess);

        String role = jwtParserUtil.extractRoleFromJwt(rawToken);

        assertThat(role).isEqualTo("role1");
    }
}
