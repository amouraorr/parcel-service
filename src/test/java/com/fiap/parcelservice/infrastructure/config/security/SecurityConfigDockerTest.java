package com.fiap.parcelservice.infrastructure.config.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigDockerTest {

    @Test
    @DisplayName("jwtDecoder deve retornar NimbusJwtDecoder quando segredo Base64 válido for fornecido")
    void jwtDecoder_withValidBase64_shouldReturnDecoder() {
        // Arrange
        SecurityConfigDocker config = new SecurityConfigDocker();
        byte[] secretBytes = "01234567890123456789012345678901".getBytes();
        String secretBase64 = Base64.getEncoder().encodeToString(secretBytes);

        // Act
        JwtDecoder decoder = config.jwtDecoder(secretBase64);

        // Assert
        assertNotNull(decoder);
        assertThrows(JwtException.class, () -> decoder.decode("invalid.token.value"));
    }

    @Test
    @DisplayName("jwtDecoder deve lançar IllegalStateException quando Base64 inválido for fornecido")
    void jwtDecoder_withInvalidBase64_shouldThrowIllegalStateException() {
        // Arrange
        SecurityConfigDocker config = new SecurityConfigDocker();
        String invalidBase64 = "not-base64!!";

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> config.jwtDecoder(invalidBase64));
    }

    @Test
    @DisplayName("jwtDecoder sem segredo deve retornar decoder que lança JwtException indicando configuração ausente")
    void jwtDecoder_withoutSecret_shouldReturnThrowingDecoder() {
        // Arrange
        SecurityConfigDocker config = new SecurityConfigDocker();

        // Act
        JwtDecoder decoder = config.jwtDecoder("");

        // Assert
        JwtException ex = assertThrows(JwtException.class, () -> decoder.decode("any.token"));
        assertTrue(ex.getMessage().contains("JwtDecoder não configurado"));
    }

    @Test
    @DisplayName("passwordEncoder deve gerar hash que corresponde à senha fornecida")
    void passwordEncoder_shouldEncodeAndMatch() {
        // Arrange
        SecurityConfigDocker config = new SecurityConfigDocker();
        PasswordEncoder encoder = config.passwordEncoder();
        String raw = "mySecretPassword";

        // Act
        String encoded = encoder.encode(raw);

        // Assert
        assertNotNull(encoded);
        assertTrue(encoder.matches(raw, encoded));
    }

    @Test
    @DisplayName("jwtAuthenticationConverter (privado) deve converter claim 'roles' em authorities com prefixo ROLE_")
    void jwtAuthenticationConverter_private_shouldConvertRoles() throws Exception {
        // Arrange
        SecurityConfigDocker config = new SecurityConfigDocker();

        // Usando reflection para acessar o método privado jwtAuthenticationConverter (passo 5)
        Method method = SecurityConfigDocker.class.getDeclaredMethod("jwtAuthenticationConverter");
        method.setAccessible(true);
        JwtAuthenticationConverter converter = (JwtAuthenticationConverter) method.invoke(config);

        // Construir um Jwt com claim "roles"
        Map<String, Object> claims = Map.of("roles", List.of("ADMIN", "USER"));
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(3600), Map.of("alg", "HS256"), claims);

        // Act
        AbstractAuthenticationToken auth = (AbstractAuthenticationToken) converter.convert(jwt);

        // Assert
        assertNotNull(auth);
        List<String> authorities = auth.getAuthorities().stream().map(a -> a.getAuthority()).toList();
        assertTrue(authorities.contains("ROLE_ADMIN"));
        assertTrue(authorities.contains("ROLE_USER"));
    }
}
