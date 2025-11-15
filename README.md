# Serviço de Pacotes/Encomendas - Backend

## Introdução

Este microsserviço faz parte de um sistema modular de gerenciamento de encomendas, focado no registro, controle de estado e integração assíncrona (Kafka) das encomendas recebidas na portaria. 
Ele é responsável pelo cadastro, consulta, listagem e baixa (pickup) de encomendas, mantendo histórico de status e integrando com outros serviços do ecossistema.

## Objetivo do Projeto

O objetivo principal deste microsserviço é fornecer uma API REST robusta para gerenciar o fluxo de encomendas na portaria: receber, listar, consultar e marcar como retirada. 
Além disso, publica eventos em Kafka para processamento assíncrono (notificações, filas de trabalho, auditoria).

## Requisitos do Sistema

Para executar este microsserviço, você precisará dos seguintes requisitos:

- Sistema Operacional: Windows, macOS ou Linux
- Memória RAM: Pelo menos 4 GB recomendados
- Espaço em Disco: Pelo menos 500 MB de espaço livre
- Software:
    - Docker e Docker Compose
    - Java JDK 11 ou superior
    - Maven 3.6 ou superior
    - PostgreSQL
    - Git

## Estrutura do Projeto

A estrutura do projeto está organizada de forma a separar domínio, persistência, casos de uso e interface web:

```
parcel-service/
│
├── src/
│ └── main/
│   ├── java/
│   │ └── com.fiap.parcelservice
│   │   ├── adapter/web/ : Controladores REST 
│   │   ├── application/dto/ : DTOs 
│   │   ├── application/mapper/ : MapStruct mappers 
│   │   ├── application/service/ : Interfaces de serviço 
│   │   ├── application/service/impl/ : Implementações de serviço 
│   │   ├── domain/model/ : Entidades de domínio 
│   │   ├── domain/repository/ : Interfaces de repositório 
│   │   ├── infrastructure/persistence/ : Implementação JPA 
│   │   ├── infrastructure/messaging/ : Producers Kafkal
│   │   ├── infrastructure/config/ : Configurações (security, swagger) 
│   │   └── ParcelServiceApplication.java : Classe principal da aplicação 
│   └── resources/
│       └── application.properties : Configurações da aplicação
├── pom.xml
├── Dockerfile
├── docker-compose.yml
└── README.md
```

## Segurança

A segurança do microsserviço é configurada com Spring Security e suporte a JWT. Existem diferentes configurações por profile:

- configuração padrão (profile != docker/test) que define regras de autorização para endpoints.
- configuração usada quando rodando com profile "docker"; fornece um [JwtDecoder] configurável via propriedade/env `security.jwt.secret`.
- configuração para ambientes de execução sem profiles docker/test.
- configuração usada nos testes para fornecer um `JwtDecoder` de teste.
- Há também a interface que descreve abstração de validação JWT.

Observações importantes:
- Para rodar em produção, forneça um segredo JWT robusto (preferencialmente em Base64 com pelo menos 32 bytes) através da variável `SECURITY_JWT_SECRET` ou `security.jwt.secret`.
- Durante desenvolvimento local/containers, garanta que os profiles e secrets estejam configurados corretamente.

## Visão Geral do Projeto

O microsserviço é desenvolvido com Spring Boot e segue uma arquitetura limpa simplificada, separando responsabilidades entre domínio, serviço, gateway/persistência e controller.

Principais componentes:
- API REST exposta pelo [ParcelController].
- Lógica de negócio na interface [ParcelService] e implementação [ParcelServiceImpl].
- Persistência com JPA via [ParcelJpaRepository] e adaptação via [ParcelRepositoryImpl].
- Publicação de eventos Kafka por .
- Documentação OpenAPI via [OpenApiConfig].

## Arquitetura

A arquitetura segue princípios de separação de camadas:
- Domain: [Parcel], [ParcelStatus]
- UseCase/Service: [ParcelService] e [ParcelServiceImpl]
- Gateway/Repository: [ParcelRepository] e adapter JPA 
- Controller: [ParcelController]
- Mapper: [ParcelMapper]

## Princípios de Design e Padrões de Projeto

- Single Responsibility Principle (SRP) — cada classe tem responsabilidade única (controller, serviço, repositório).
- Gateway Pattern — abstração para persistência via interface.
- Mapper Pattern — MapStruct para conversão entre DTO e entidade.

## Tecnologias Utilizadas

- Spring Boot
- Spring Security (OAuth2 Resource Server / JWT)
- Spring Data JPA (Hibernate)
- PostgreSQL
- Kafka (Spring for Apache Kafka)
- MapStruct
- Lombok (se utilizado em modelos futuros)
- Springdoc OpenAPI (Swagger)
- Docker e Docker Compose

## Pré-requisitos

- Docker e Docker Compose
- Java JDK 11+
- Maven 3.6+
- Banco PostgreSQL (local ou via container)

### Executando com Docker Compose

1. Certifique-se que Docker e Docker Compose estão instalados e em execução.
2. No terminal, entre no diretório onde está o `docker-compose.yml`.
3. Execute:
   ```bash
   docker compose up
   ```
4. A aplicação estará disponível em: http://localhost:8082 
5. Swagger UI: http://localhost:8082/swagger-ui/index.html#/
6. PostgreSQL padrão: host `postgres` na porta 5432 (ver `docker-compose.yml`).
7. A ferramenta Adminer estará disponível para visualização do banco de dados no endereço `http://localhost:8088`.

## Conexão ao Banco via Adminer

1. Acesse `http://localhost:8088` (se Adminer estiver configurado).
2. Em Sistema, escolha PostgreSQL.
3. Em Servidor, utilize o nome do serviço do Docker Compose (ex.: `postgres`).
4. Usuário: `postgres`.
5. Senha: `postgres`.
6. Banco de dados: `postgres`.

## Configurações e Variáveis de Ambiente Importantes

- SECURITY_JWT_SECRET / security.jwt.secret — segredo usado para validar JWT (no profile "docker" o `SecurityConfigDocker` o usa).
- KAFKA_BOOTSTRAP_SERVERS — bootstrap servers do Kafka.
- KAFKA_TOPICS_PARCELS_IN — tópico para eventos de encomendas recebidas (padrão `parcels.received`).
- KAFKA_TOPICS_NOTIFICATIONS_OUT — tópico para notificações (padrão `notifications.sent`).
- Datasource (spring.datasource.*) — URL, usuário, senha do PostgreSQL.

## Endpoints Principais

- POST /api/parcels — Registrar nova encomenda (requer role PORTEIRO) 
- GET /api/parcels — Listar todas as encomendas
- GET /api/parcels/{id} — Consultar encomenda por ID
- POST /api/parcels/{id}/pickup — Marcar encomenda como retirada (requer role PORTEIRO)

## Contribuição

Contribuições são bem-vindas. Fluxo recomendado:
1. Fork do repositório
2. Criar branch: git checkout -b feature/nome-da-feature
3. Commits pequenos e claros
4. Pull Request explicando mudanças

## Licença

Este projeto pode ser privado ou não possuir licença explícita. Verifique com os responsáveis antes de redistribuir.

## Referências e Recursos

- Spring Boot: https://spring.io/projects/spring-boot
- Spring Security: https://spring.io/projects/spring-security
- Spring Data JPA: https://spring.io/projects/spring-data-jpa
- MapStruct: https://mapstruct.org
- Springdoc OpenAPI: https://springdoc.org
- Kafka (Spring for Apache Kafka): https://spring.apache.org/projects/spring-kafka
- PostgreSQL: https://www.postgresql.org

## Conclusão

Este microsserviço de Pacotes/Encomendas aplica princípios de arquitetura limpa, separando responsabilidades e garantindo integração assíncrona com Kafka e persistência com PostgreSQL, facilitando manutenção e evolução do sistema.

