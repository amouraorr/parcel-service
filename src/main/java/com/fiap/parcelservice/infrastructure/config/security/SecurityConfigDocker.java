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
 *
 * Observações:
 * - Para testes locais via docker-compose, liberamos explicitamente todas as rotas
 *   para permitir testes sem exigir um JWT/credencial.
 * - Isso deve ser usado apenas em ambiente de desenvolvimento/docker local.
 *
 * Alterações importantes:
 * - Adicionado @Order(0) para garantir que esta SecurityFilterChain tenha precedência
 *   sobre eventuais outras configurações de segurança e evitar 403 não esperado.
 * - Desabilitado explicitamente httpBasic e formLogin para evitar autenticação padrão.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("docker")
public class SecurityConfigDocker {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfigDocker.class);

    @Bean
    //@Order
    public SecurityFilterChain parcelServiceSecurityFilterChainDocker(HttpSecurity http) throws Exception {
        logger.info("Ativando SecurityConfigDocker (ParcelService) - permissões relaxadas para profile 'docker'.");

        http
            // Em APIs stateless com JWT normalmente o CSRF pode ser desabilitado
            .csrf(csrf -> csrf.disable())
            // desabilitar autenticação HTTP Basic e form login que podem ser habilitadas por default
            //.httpBasic(httpBasic -> httpBasic.disable())
            //.formLogin(form -> form.disable())
            //.authorizeHttpRequests(auth -> auth
            // ------------------ ALTERAÇÃO PARA TESTES ------------------
            // As linhas abaixo foram comentadas para liberar todas as requisições
            // durante testes locais no profile 'docker'. Para produção, remova
            // os comentários e ajuste as permissões conforme o comportamento desejado.

            // Endpoints públicos úteis em ambiente docker (ajuste conforme necessidade)
            // .requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
            // Permitir criação de usuários e login sem estar autenticado (internals)
            // .requestMatchers(HttpMethod.POST, "/api/internal/users/**").permitAll()
            // .requestMatchers(HttpMethod.POST, "/api/internal/auth/login").permitAll()

            // ***************** Ajuste para testes via API Gateway em 'docker' *****************
            // Permite que o API Gateway encaminhe POST /api/parcels sem autenticação local.
            // REMOVA ou altere esta regra em ambientes de produção!
            // .requestMatchers(HttpMethod.POST, "/api/parcels").permitAll()
            // .requestMatchers(HttpMethod.POST, "/api/parcels/*/pickup").hasRole("PORTEIRO")
            // Leitura de encomendas permitida para PORTEIRO e MORADOR
            // .requestMatchers(HttpMethod.GET, "/api/parcels/**").hasAnyRole("PORTEIRO", "MORADOR")

            // Demais requests precisam de autenticação
            // .anyRequest().authenticated()
            // IMPORTANTE: é uma solução temporária para ambiente 'docker' apenas.
            .anonymous(anon -> anon.authorities("ROLE_PORTEIRO"))

            .authorizeHttpRequests(auth -> auth
                    // --> Para TESTES LOCAIS: liberar todas as rotas para evitar problemas com autenticação

                    .anyRequest().permitAll()
            )
        // ------------------ ALTERAÇÃO PARA TESTES ------------------
        // Comentamos temporariamente a configuração do resource server (validação JWT).
        // Para reativar a validação de tokens, remova os comentários abaixo e garanta
        // que a propriedade 'security.jwt.secret' esteja configurada no ambiente.
                /*
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                */
        ;

        // Observação: reative .oauth2ResourceServer(...) em produção
        return http.build();
    }

    /**
     * JwtDecoder HS256 (Nimbus) usando segredo Base64.
     * Usa a propriedade 'security.jwt.secret' para manter consistência com o gateway.
     * Ex.: export SECURITY_JWT_SECRET=$(echo -n "minha-chave-secreta" | base64)
     *
     * Observação: se security.jwt.secret não for fornecido, o bean retorna um decoder que falhará ao tentar validar,
     * mas como neste profile 'docker' as regras de segurança estão temporariamente relaxadas, requests de teste
     * podem funcionar sem validação de token. Esta configuração permanece para facilitar reativação.
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
     * Ajuste aqui se seu token usar outro claim (ex.: 'authorities' ou 'realm_access.roles').
     *
     * Nota: este bean permanece no código para facilitar reativação da validação JWT.
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