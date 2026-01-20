# High-Performance Event Ticketing Engine

![Java](https://img.shields.io/badge/Java-17-007396?style=for-the-badge)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.x-6DB33F?style=for-the-badge)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?style=for-the-badge)
![Purpose](https://img.shields.io/badge/Purpose-Educational-555?style=for-the-badge)
![Security](https://img.shields.io/badge/Security-JWT-EF4444?style=for-the-badge)

A **microservices-style event ticketing platform** built with **Java 17 + Spring Boot**.

This repo is organized as a **Maven multi-module** project where each module is a separate Spring Boot application (service). The goal is to practice real microservice boundaries: **independent deployable services, separate databases, and clear service ownership**.

> Vision: a scalable ticketing system with auth, event catalog, booking/reservations, payments (Stripe), search, and notifications.

---

## Services

### Implemented / active
- **auth-service**: registration/login + JWT authentication/authorization
- **event-service**: event CRUD / event catalog domain (WIP)
- **notification-service**: placeholder for async notifications (WIP)

### Planned (roadmap)
- **api-gateway**: single entry point (routing, auth enforcement, rate limiting)
- **booking-service**: seat reservation / ticket purchase workflow, concurrency handling
- **payment-service (Stripe)**: payment intents, webhooks, payment status
- **search-service**: full-text search for events (e.g., Elasticsearch/OpenSearch) 

> Each service should own its data. Avoid sharing JPA entities across services.

---

## Tech stack
- Java 17
- Spring Boot 3.2.x
- Spring Security + JWT
- Spring Data JPA + Hibernate
- PostgreSQL 16 (Docker)
- Maven (multi-module)

---

## Repository structure
This is a **Maven parent** project with multiple Spring Boot apps.

```
./
  pom.xml                    # parent POM
  compose.yaml               # local infra (PostgreSQL containers)
  auth-service/
  event-service/
  notification-service/
  # (planned)
  # api-gateway/
  # booking-service/
  # payment-service/
  # search-service/
```

Each service has its own `pom.xml` and `src/main/...`.

---

## Local development

### 1) Start infrastructure (PostgreSQL)
From the repo root:

```bash
docker compose up -d
```

> If you change DB credentials or ports, update both `compose.yaml` and the service `application.properties`.

### 2) Run a single service
From the repo root:

```bash
./mvnw -pl auth-service spring-boot:run
```

Run another service:

```bash
./mvnw -pl event-service spring-boot:run
```

---

## Databases (Docker)
Defined in `compose.yaml`.

Typical setup:
- `auth-db`  → Postgres on `localhost:5432`, database `authdb`
- `event-db` → Postgres on `localhost:5433`, database `eventdb`

Default credentials (from `compose.yaml`):
- user: `admin`
- password: `password`

---

## Build
Build everything:

```bash
./mvnw clean verify
```

Build one module only:

```bash
./mvnw -pl auth-service clean verify
```

---

## Auth Service

### API
- Swagger UI (if enabled):
  - http://localhost:8080/swagger-ui/index.html
- Status:
  - `GET /api/v1/system/status`
- Auth:
  - `POST /api/v1/auth/register`
  - `POST /api/v1/auth/authenticate`

### JWT usage
After register/login, send the token with:
- Header: `Authorization: Bearer <JWT>`

If you get **403**, it usually means:
- you’re calling a protected endpoint without a token
- the token is expired / signed with a different secret
- the endpoint isn’t whitelisted in `SecurityConfig`

---

## “Why don’t I see my tables in IntelliJ?”
Hibernate creates tables inside the **database + schema** you’re connected to.

In IntelliJ Database tool window:
1. Verify the DataSource points to the correct host/port:
   - `5432` for `auth-db`, `5433` for `event-db`
2. Verify the database name:
   - `authdb` or `eventdb`
3. Check schema:
   - `public` (default)
4. Click **Refresh** / **Synchronize**

Also note: if you use `spring.jpa.hibernate.ddl-auto=create` or `create-drop`, tables can be dropped/recreated each restart.

---

## Roadmap
- [ ] Add **API Gateway** (Spring Cloud Gateway)
- [ ] Add **Booking service** (reservations + concurrency)
- [ ] Add **Payment service** (Stripe payment intents + webhooks)
- [ ] Add **Search service** (Elasticsearch/OpenSearch)
- [ ] Add async messaging for notifications (Kafka/RabbitMQ)
- [ ] Add Dockerfiles per service + full compose to run everything
- [ ] Add tests (unit + integration)
- [ ] Observability: logs/metrics/tracing (Micrometer + OpenTelemetry)

---

## LinkedIn-ready description
**High-Performance Event Ticketing Engine** is a microservices-style ticketing platform I’m building in **Java 17 / Spring Boot 3** to practice real-world backend architecture.

What I’ve built so far:
- a dedicated **Auth Service** with **JWT-based authentication & authorization**
- a multi-module Maven structure that supports independent services and clean boundaries
- local infra with **PostgreSQL (Docker)** per service

What’s coming next:
- **API Gateway** for routing and centralized security
- **Booking Service** for reservations and ticket purchase workflows (with concurrency handling)
- **Payment Service** integrating **Stripe** (payment intents + webhooks)
- **Search Service** for fast event discovery
- async notifications via messaging (Kafka/RabbitMQ) and better observability/testing

---

## Author
Aymen Zouinek
