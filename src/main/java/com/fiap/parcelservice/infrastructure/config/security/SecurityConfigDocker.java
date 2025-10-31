package com.fiap.parcelservice.infrastructure.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuração de segurança para o profile 'docker'.
  */
@Configuration
@EnableWebSecurity
@Profile("docker")
public class SecurityConfigDocker {

    /**
     * Bean principal de SecurityFilterChain para o profile 'docker'.
     * Mantido com permitAll para facilitar o startup em ambiente Docker.
     */
    @Bean
    public SecurityFilterChain parcelServiceSecurityFilterChainDocker(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /**
     * PasswordEncoder padrão (BCrypt) — util para quando for necessário
     * autenticação com senha.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}