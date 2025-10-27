package com.fiap.parcelservice.infrastructure.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança para o profile 'docker'.
 * Libera apenas endpoints relacionados ao Swagger/OpenAPI e actuator.
 */
@Configuration("securityConfigDockerUnique")
@Profile("docker")
public class SecurityConfigDocker {

    /**
     * Libera apenas a documentação (Swagger/OpenAPI) e actuator no profile 'docker'.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain parcelServiceSecurityFilterChainDocker(HttpSecurity http) throws Exception {
        http
                .securityMatcher(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/swagger-ui/index.html",
                        "/swagger-resources/**",
                        "/actuator/**"
                )
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}