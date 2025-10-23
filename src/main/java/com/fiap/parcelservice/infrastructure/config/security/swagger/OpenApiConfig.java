package com.fiap.parcelservice.infrastructure.config.security.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PÓS GRADUAÇÃO - FIAP 2025 - SERVIÇO DE ENCOMENDAS")
                        .version("1.0.0")
                        .description("Microsserviço responsável pelo registro, gestão e enfileiramento de encomendas recepcionadas na portaria, incluindo integração com Kafka para processamento assíncrono."));
    }
}