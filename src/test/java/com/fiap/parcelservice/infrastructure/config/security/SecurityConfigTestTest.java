package com.fiap.parcelservice.infrastructure.config.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

class SecurityConfigTestTest {

    @Test
    @DisplayName("Deve criar JwtDecoder capaz de validar JWTs assinados com o segredo configurado")
    void shouldCreateJwtDecoderAndValidateToken() throws Exception {
        // Arrange
        SecurityConfigTest config = new SecurityConfigTest();

        byte[] secretBytes = new byte[32];
        new SecureRandom().nextBytes(secretBytes);
        String secretBase64 = Base64.getEncoder().encodeToString(secretBytes);

        Field field = SecurityConfigTest.class.getDeclaredField("jwtSecretBase64");
        field.setAccessible(true);
        field.set(config, secretBase64);

        // Act
        JwtDecoder decoder = config.jwtDecoder();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("user123")
                .issuer("unit-test")
                .expirationTime(new Date(System.currentTimeMillis() + 60_000))
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        MACSigner signer = new MACSigner(secretBytes);
        signedJWT.sign(signer);
        String token = signedJWT.serialize();

        Jwt jwt = decoder.decode(token);

        // Assert
        assertNotNull(decoder);
        assertNotNull(jwt);
        assertEquals("user123", jwt.getSubject());
    }
}