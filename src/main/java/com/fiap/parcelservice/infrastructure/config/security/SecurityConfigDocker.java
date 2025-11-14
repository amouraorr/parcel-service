package com.fiap.parcelservice.infrastructure.config.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Configuração específica para o profile 'docker'.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("docker")
public class SecurityConfigDocker {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfigDocker.class);

    @Bean
    public SecurityFilterChain parcelServiceSecurityFilterChainDocker(HttpSecurity http) throws Exception {
        logger.info("Ativando SecurityConfigDocker (ParcelService) - permissões relaxadas para profile 'docker'.");

        http

            .csrf(csrf -> csrf.disable())
            .anonymous(anon -> anon.authorities("ROLE_PORTEIRO"))

            .authorizeHttpRequests(auth -> auth

                    .anyRequest().permitAll()
            );

        return http.build();
    }

    /**
     * JwtDecoder HS256 (Nimbus) usando segredo Base64.
     */
    @Bean
    public JwtDecoder jwtDecoder(@Value("${security.jwt.secret:}") String jwtSecretBase64) {
        if (jwtSecretBase64 != null && !jwtSecretBase64.isBlank()) {
            try {
                byte[] keyBytes = Base64.getDecoder().decode(jwtSecretBase64);
                SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
                logger.info("JwtDecoder configurado com segredo ({} bytes)", keyBytes.length);
                return NimbusJwtDecoder.withSecretKey(secretKey)
                        .macAlgorithm(MacAlgorithm.HS256)
                        .build();
            } catch (IllegalArgumentException ex) {
                logger.error("Propriedade 'security.jwt.secret' não é um Base64 válido: {}", ex.getMessage());
                throw new IllegalStateException("Propriedade 'security.jwt.secret' inválida (esperado Base64)", ex);
            }
        }

        logger.warn("security.jwt.secret não definido; JwtDecoder retornará erro ao decodificar tokens. " +
                "Endpoints explicitamente permitidos funcionarão sem validação (apenas para DEV/Docker).");
        return token -> {
            throw new JwtException("JwtDecoder não configurado para o profile 'docker'. Defina 'security.jwt.secret' para habilitar validação de tokens.");
        };
    }

    /**
     * Converte claim 'roles' (lista de Strings) em GrantedAuthority com prefixo ROLE_.
     */
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter conv = new JwtAuthenticationConverter();
        conv.setJwtGrantedAuthoritiesConverter((Jwt jwt) -> {
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles == null) {
                return List.of();
            }
            return roles.stream()
                    .map(r -> "ROLE_" + r)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        });
        return conv;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}