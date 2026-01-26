# High-Performance Event Ticketing Engine

![Java](https://img.shields.io/badge/Java-17-007396?style=for-the-badge)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.x-6DB33F?style=for-the-badge)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?style=for-the-badge)
![Purpose](https://img.shields.io/badge/Purpose-Educational-555?style=for-the-badge)
![Security](https://img.shields.io/badge/Security-JWT-EF4444?style=for-the-badge)

A **microservices-style event ticketing platform** built with **Java 17 + Spring Boot 3**. The goal is to practice real-world backend design: **clear service boundaries**, **independent deployability**, and a **database per service**.

This repository is a **Maven multi-module** project where each module is a separate Spring Boot application.

---

## Architecture (at a glance)
- **Microservices**: each service is deployed and scaled independently.
- **Database-per-service**: each service owns its schema and persists its own data.
- **No shared JPA entities across services**: shared contracts should be APIs/events, not shared persistence models.

---

## Services

### Current
- **auth-service** – user registration/login, JWT authentication & authorization
- **event-service** – event catalog (CRUD) *(WIP)*
- **notification-service** – notifications foundation *(WIP)*

### Planned
- **api-gateway** – single entry point (routing, auth enforcement, rate limiting)
- **booking-service** – reservation workflow, seat/ticket locking, concurrency handling
- **payment-service** – Stripe integration (payment intents + webhooks)
- **search-service** – hybrid search (keyword + semantic/vector search)

---

## Tech stack
- Java 17
- Spring Boot 3.2.x
- Spring Security + JWT
- Spring Data JPA + Hibernate
- PostgreSQL 16 (Docker)
- Maven multi-module

---

## Repository structure
```
./
  pom.xml              # parent POM
  compose.yaml         # local infra (PostgreSQL containers)
  auth-service/
  event-service/
  notification-service/
```

---

## Getting started (local)

### 1) Start the infrastructure
```bash
docker compose up -d
```

### 2) Run a service
```bash
./mvnw -pl auth-service spring-boot:run
```

```bash
./mvnw -pl event-service spring-boot:run
```

### 3) Build everything
```bash
./mvnw clean verify
```

### ⚠️ Important: Database Migration Notice
If you previously ran `event-service` and encounter schema errors about `category` or `status` columns, you need to drop the events table:

**Windows/PowerShell:**
```powershell
docker exec -it event_db psql -U admin -d eventdb -c "DROP TABLE IF EXISTS events CASCADE;"
```

**Mac/Linux:**
```bash
docker exec -it event_db psql -U admin -d eventdb -c "DROP TABLE IF EXISTS events CASCADE;"
```

**Or reset all databases:**
```bash
docker-compose down -v
docker-compose up -d
```

This is needed because enum storage changed from `ORDINAL` (integer) to `STRING` (varchar). Hibernate will recreate the table with the correct schema.

---

## Databases
Defined in `compose.yaml` (database-per-service).

Typical local mapping:
- `auth-db`  → `localhost:5432`, database `authdb`
- `event-db` → `localhost:5433`, database `eventdb`

Default credentials:
- user: `admin`
- password: `password`

---

## API (auth-service)
- Swagger UI (if enabled): http://localhost:8080/swagger-ui/index.html
- Health/Status: `GET /api/v1/system/status`
- Auth:
  - `POST /api/v1/auth/register`
  - `POST /api/v1/auth/authenticate`

JWT header:
- `Authorization: Bearer <JWT>`

---

## Roadmap
- [ ] API Gateway (Spring Cloud Gateway)
- [ ] Booking Service (reservations + concurrency)
- [ ] Payment Service (Stripe payment intents + webhooks)
- [ ] Search Service (keyword + semantic search)
- [ ] Async messaging for notifications (Kafka/RabbitMQ)
- [ ] Observability (Micrometer + OpenTelemetry)
- [ ] Tests (unit + integration)

---

## OpenAI / LLM usage (planned)
Primary target is the **search-service** for **semantic search** (embeddings) and **hybrid retrieval**.

Potential extensions:
- **support/assistant-service** – Q&A / RAG over events, venues, policies
- **notification-service** – optional templated personalization and localization

LLMs are intentionally **not** planned for auth/booking/payment flows to keep security and consistency deterministic.

---

## Author
Aymen Zouinek
