package com.fiap.parcelservice.infrastructure.config.security;

import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;


/**
 * Configuração ativa apenas para o profile "test" que fornece um JwtDecoder.
 * Lê a propriedade jwt.secret (Base64) definida em src/test/resources e cria um NimbusJwtDecoder HS256.
 */
@Configuration
@Profile("test")
public class SecurityConfigTest {

    @Value("${jwt.secret}")
    private String jwtSecretBase64;

    @Bean
    public JwtDecoder jwtDecoder() {
        // Decodifica o segredo Base64 em bytes
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecretBase64);

        // Cria SecretKey compatível com HmacSHA256
        SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");

        // Cria e retorna o NimbusJwtDecoder usando HS256
        return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}