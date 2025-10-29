package com.fiap.parcelservice.infrastructure.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança para ambientes de execução (não docker e não test).
 * Alterado o @Profile para excluir explicitamente o profile "test", evitando a
 * criação concorrente de duas SecurityFilterChain que combinam 'anyRequest()'
 * durante os testes automatizados.
 */
@Configuration(value = "parcelServiceSecurityConfigProd", proxyBeanMethods = false)
@Profile("!docker & !test")
public class SecurityConfigProd {

    @Bean("securityFilterChainProd")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}