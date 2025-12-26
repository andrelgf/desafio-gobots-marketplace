# GoBots Marketplace - Desafio Tecnico
[![Continuous Integration](https://github.com/andrelgf/desafio-gobots-marketplace/actions/workflows/continuous-integration.yml/badge.svg)](https://github.com/andrelgf/desafio-gobots-marketplace/actions/workflows/continuous-integration.yml)

## Visao geral do projeto
Este repositorio implementa dois microservicos em Kotlin/Spring Boot para um fluxo end-to-end de pedidos:
o `marketplace-service` gerencia orders e publica eventos via Outbox + RabbitMQ, e o `receiver-service`
consome os eventos, aplica idempotencia e armazena snapshots da order para lojas assinantes.

## Principais features
API
- Criacao, consulta e atualizacao de status de orders (`marketplace-service`)
- Listagem de orders (`marketplace-service`)
- Assinatura de lojas (`receiver-service`)
- Listagem de subscriptions, received events e order snapshots (`receiver-service`)

Dominio
- Maquina de estados de order: CREATED -> PAID -> SHIPPED -> COMPLETED (ou CANCELED a partir de qualquer estado valido)
- Itens de order com total calculado
- Idempotencia por `eventId` no receiver

Mensageria
- Outbox pattern (persistencia transacional de evento)
- Publicacao assincrona via scheduler
- Retry com TTL + DLX (2 niveis) e DLQ final
- Headers de evento (`x-event-id`, `x-event-type`)

Persistencia
- PostgreSQL com Flyway
- Schemas separados: `marketplace` e `receiver`

Testes
- Unit tests para services e publisher
- Integracao com Testcontainers (Postgres e RabbitMQ)
- Testes de listeners e endpoints REST

Infra/DevOps
- Dockerfiles multi-stage
- Docker Compose para stack completa
- CI com GitHub Actions (build/test + build de imagens)

## Stack utilizada
Linguagem e runtime
- Kotlin 1.9.25
- Java 21

Frameworks e libs
- Spring Boot 3.5.8
- Spring Web, Spring Validation, Spring Data JPA
- Spring AMQP (RabbitMQ)
- Springdoc OpenAPI (Swagger)
- Resilience4j (retry/circuit breaker)
- OpenFeign
- Flyway

Banco de dados
- PostgreSQL

Mensageria
- RabbitMQ

Testes
- JUnit 5
- MockK
- RestAssured
- Testcontainers (Postgres, RabbitMQ)
- Awaitility

Observabilidade / qualidade
- Spring Boot Actuator (health/info)

Infra / DevOps
- Docker + Docker Compose
- GitHub Actions CI

## Endpoints principais
| Servico | Metodo | Endpoint | Descricao |
| --- | --- | --- | --- |
| marketplace-service | POST | /api/v1/orders | Cria uma order |
| marketplace-service | GET | /api/v1/orders | Lista orders |
| marketplace-service | GET | /api/v1/orders/{id} | Busca order por id |
| marketplace-service | PATCH | /api/v1/orders/{id} | Atualiza status da order |
| receiver-service | POST | /api/v1/subscriptions | Cria subscriptions |
| receiver-service | GET | /api/v1/subscriptions | Lista subscriptions |
| receiver-service | GET | /api/v1/received-events | Lista eventos recebidos |
| receiver-service | GET | /api/v1/order-snapshots | Lista snapshots |

## Arquitetura
Arquitetura em camadas com separacao clara entre:
- API (controllers, DTOs, mappers)
- Application/service (casos de uso)
- Domain (entidades e regras)
- Persistence (repositories JPA)
- Messaging/infra (RabbitMQ, Feign)

No `receiver-service` ha elementos de ports/adapters (ex: `MarketplaceServicePort` + adapter Feign),
aproximando uma arquitetura hexagonal leve.

## Como rodar localmente (Docker)
Clone o repositorio e entre na pasta:
```bash
git clone https://github.com/andrelgf/desafio-gobots-marketplace.git
cd desafio-gobots-marketplace
```

Na raiz do projeto:
```bash
docker compose -f infra/docker-compose.yml up -d --build
```

Opcional (rodando de dentro da pasta `infra`):
```bash
cd infra
docker compose up -d --build
```

Servicos:
- marketplace-service: http://localhost:8080
- receiver-service: http://localhost:8081
- RabbitMQ UI: http://localhost:15672 (guest/guest)

## Roteiro E2E (fluxo completo)
1) Criar subscription no receiver:
```bash
curl -X POST http://localhost:8081/api/v1/subscriptions \
  -H "Content-Type: application/json" \
  -d '{"storeIds":["STORE_001"]}'
```

2) Criar order no marketplace:
```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"storeCode":"STORE_001","items":[{"productName":"Widget","quantity":2,"unitPrice":10.00}]}'
```
Anote o `id` retornado no response.

3) Verificar estado inicial:
```bash
curl http://localhost:8080/api/v1/orders/<id>
```
Status esperado: `CREATED`.

4) Atualizar status (maquina de estados):
```bash
curl -X PATCH http://localhost:8080/api/v1/orders/<id> \
  -H "Content-Type: application/json" \
  -d '{"status":"PAID"}'
```

5) Validar eventos recebidos no receiver:
```bash
curl http://localhost:8081/api/v1/received-events
```

6) Validar snapshots capturados:
```bash
curl http://localhost:8081/api/v1/order-snapshots
```

## Variaveis de ambiente necessarias
Marketplace:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_RABBITMQ_HOST`
- `SPRING_RABBITMQ_PORT`
- `SPRING_RABBITMQ_USERNAME`
- `SPRING_RABBITMQ_PASSWORD`

Receiver:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_RABBITMQ_HOST`
- `SPRING_RABBITMQ_PORT`
- `SPRING_RABBITMQ_USERNAME`
- `SPRING_RABBITMQ_PASSWORD`
- `MARKETPLACE_BASE_URL`

Defaults estao definidos nos `application.yaml`.

## Observacoes de design
- Outbox garante consistencia transacional entre escrita do pedido e publicacao do evento.
- Retry TTL + DLX evita requeue infinito e permite backoff controlado.
- Idempotencia no receiver via `eventId` (unique constraint + checks).
- Publisher envia JSON puro com headers de metadados para evitar acoplamento de classes entre servicos.
