# High-Performance Event Ticketing Engine

![Java](https://img.shields.io/badge/Java-17-007396?style=for-the-badge)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.x-6DB33F?style=for-the-badge)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?style=for-the-badge)
![Purpose](https://img.shields.io/badge/Purpose-Educational-555?style=for-the-badge)
![Security](https://img.shields.io/badge/Security-JWT-EF4444?style=for-the-badge)

A Java 17 + Spring Boot learning project that’s being refactored into a **microservices-style, Maven multi-module** repository.

**Current services**
- **auth-service**: registration/login + JWT authentication/authorization
- **event-service**: event CRUD / ticketing domain (WIP)
- **notification-service**: placeholder for async notifications (WIP)

> Goal: evolve this into a clean microservices architecture: separate services, separate databases, clear boundaries, and (later) async messaging.

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
  compose.yaml               # local PostgreSQL containers
  auth-service/
  event-service/
  notification-service/
```

Each service has its own `pom.xml` and `src/main/...`.

---

## Local development

### 1) Start dependencies (PostgreSQL)
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

## Microservices conventions (intentional design)
- Don’t share entities between services (each service owns its data)
- Separate databases per service (already set up in `compose.yaml`)
- Prefer HTTP between services first; later add async messaging

---

## Roadmap
- [ ] Add API Gateway (Spring Cloud Gateway)
- [ ] Consider a centralized config approach (optional)
- [ ] Add async messaging (Kafka/RabbitMQ) for notifications
- [ ] Add Dockerfiles per service + full compose to run everything
- [ ] Add tests (unit + integration)

---

## Author
Aymen Zouinek
