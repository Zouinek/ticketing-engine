# Ticketing Engine (Microservices playground)

A small **microservices-style ticketing platform** built to practice real-world backend engineering: service boundaries, data ownership, concurrency, testing, and inter-service communication.

- Language/runtime: **Java 17**
- Framework: **Spring Boot 3.2.x**
- Build: **Maven multi-module**
- Databases: **PostgreSQL (Docker Compose)**
- Auth: **Spring Security + JWT**
- Service-to-service: **gRPC** (contracts in `ticketing-common`)

> Status: I’m pausing development during the semester and plan to continue in the summer.

---

## Modules / services
This repository is a **single Maven multi-module** project. Each service is a separate Spring Boot app.

- `ticketing-common`  
  Shared **contracts** (DTOs/enums) + **gRPC protobuf** definitions & generated stubs.

- `auth-service`  
  Registration/login + JWT issuing. Provides user identity and roles.

- `event-service`  
  Event catalog (CRUD) + exposes gRPC APIs used by booking (ex: reserve tickets).

- `booking-service`  
  Booking workflow + expiration window for pending reservations. Uses **optimistic locking** and calls `event-service` over **gRPC** for validation/reservation.

- `notification-service`  
  Currently **empty** (placeholder module). Planned for async notifications in the summer roadmap.

---

## Local infrastructure
`compose.yaml` starts **one Postgres container per service**:

- auth DB: `localhost:5432` → `authdb`
- booking DB: `localhost:5433` → `bookingdb`
- event DB: `localhost:5434` → `eventdb`

Default credentials (local dev only):
- user: `admin`
- password: `password`

---

## Run locally (Windows PowerShell)

### 1) Start databases
```powershell
cd C:\..\ticketing-engine
docker compose up -d
```

### 2) Provide environment variables (secrets)
This repo intentionally reads secrets from env vars.

Set these in your terminal session (or in your OS env settings):
- `JWT_SECRET` (required for auth/event/booking)
- `OPENAI_API_KEY` (only used if you experiment with Spring AI in auth-service)

Example (PowerShell session only):
```powershell
$env:JWT_SECRET = "change-me"
# optional
$env:OPENAI_API_KEY = "your-key"
```

### 3) Run services
Run each service from the parent folder:

```powershell
.\mvnw -pl auth-service spring-boot:run
```

```powershell
.\mvnw -pl event-service spring-boot:run
```

```powershell
.\mvnw -pl booking-service spring-boot:run
```

> `event-service` also exposes a gRPC server. See `event-service/src/main/resources/application.properties` (default port `9090`).

### 4) Run tests
```powershell
.\mvnw clean test
```

---

## API quick links (dev)
When services are running:
- event swagger (if enabled): `http://localhost:8082/swagger-ui/index.html`
- booking swagger (if enabled): `http://localhost:8083/swagger-ui/index.html`

JWT header:
- `Authorization: Bearer <token>`

---

## Notes / gotchas

### Hibernate enum storage changes
If you previously ran `event-service` and changed enum mapping (ORDINAL ⇄ STRING), Postgres schema can get stuck.

For a clean reset (drops data):
```powershell
docker compose down -v
docker compose up -d
```

### Don’t commit secrets
- JWT values are loaded from `JWT_SECRET` env var.
- Keep secrets out of `application.properties`.

---

## What I plan to build next (summer roadmap)
Practical roadmap in the order I’d implement it:

### Booking & inventory correctness
- Implement a **full reservation lifecycle**:
  - reserve tickets (already)
  - release tickets on cancel/expiration (add a gRPC RPC + server impl)
- Add **idempotency** and clearer state transitions (PENDING → CONFIRMED → CANCELLED/EXPIRED)
- Improve concurrency tests (multi-thread + optimistic locking scenarios)

### Seat model / pricing
- Decide between:
  - simple “general admission” inventory, or
  - real seat map (sections/rows/seats)
- Add price rules (per seat category, promo codes, fees)

### Payment service (new)
- `payment-service` with Stripe (or similar)
- Webhooks + payment status reconciliation
- Outbox pattern / reliable events (optional but realistic)

### Async messaging + notifications
- Introduce a message broker (**RabbitMQ or Kafka**)
- Emit domain events (BookingCreated/Confirmed/Expired)
- Implement `notification-service` (currently empty) to consume events (email/SMS later)

### Caching & performance
- Add **Redis** properly:
  - caching read-heavy endpoints
  - rate limiting / request throttling (gateway)
- Load testing and profiling

### Deployment & ops
- Add CI (GitHub Actions): build + test + formatting/linting
- Add observability:
  - structured logs
  - Micrometer metrics
  - OpenTelemetry tracing
- Dockerfiles per service + Compose for full stack
- Kubernetes later (after the basics), with health checks and readiness probes

### API gateway
- Add an API gateway (Spring Cloud Gateway) for:
  - routing
  - auth enforcement
  - basic rate limiting

---

## Repo structure
```
./
  compose.yaml
  pom.xml
  ticketing-common/
  auth-service/
  event-service/
  booking-service/
  notification-service/
```

---

## Author
Aymen Zouinek
