package com.fiap.parcelservice.infrastructure.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Configuração de segurança padrão — ativa quando o profile NÃO for 'docker'.
 * Requer autenticação para as requisições, mas libera /actuator/**.
 */
@Configuration
@Profile("!docker")
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/parcels/**").hasAnyRole("PORTEIRO", "MORADOR") // exemplo
                        .requestMatchers(HttpMethod.POST, "/api/parcels").hasRole("PORTEIRO")
                        .requestMatchers(HttpMethod.POST, "/api/parcels/*/pickup").hasRole("PORTEIRO")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));
        return http.build();
    }
}