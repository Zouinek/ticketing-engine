# 🎟️ High-Performance Event Ticketing Engine

![Java](https://img.shields.io/badge/Java-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Purpose](https://img.shields.io/badge/Purpose-Educational-purple)
![Security](https://img.shields.io/badge/Security-JWT-red)

> **A scalable, concurrency-safe REST API designed to handle high-demand ticket sales (e.g., concert sell-outs) without overbooking.**

---

## 🎓 Educational Purpose
**This project is created for educational and portfolio purposes.**
It is designed to demonstrate advanced backend engineering concepts, specifically:
* Solving **Race Conditions** in high-concurrency environments.
* Implementing **Role-Based Access Control (RBAC)** security from scratch.
* Structuring a **Production-Grade** Spring Boot architecture.

*While the logic is robust, this is a demonstration project and is not intended for commercial use in its current state.*

---

## 📖 Project Overview
This engine simulates a ticketing platform (like Ticketmaster or Eventim). It addresses the critical engineering challenge of **"The Taylor Swift Problem"**—where thousands of users try to buy the last ticket simultaneously.

### 🌟 Key Features
* **🔐 Military-Grade Security:**
    * Stateless Authentication using **JWT (JSON Web Tokens)**.
    * Distinction between `ADMIN` and `USER` roles.
    * Password encryption using **BCrypt**.
* **⚡ High-Performance Architecture:**
    * **Optimistic Locking (`@Version`):** Prevents double-booking using database-level concurrency control.
    * **Database Seeding:** Automatically pre-fills the database with test events on startup.
* **🛠️ Developer Experience:**
    * **Swagger UI / OpenAPI:** Automatic, interactive API documentation.
    * **Health Checks:** Dedicated `/api/v1/system/status` endpoint for load balancers.

---

##  Tech Stack

| Component | Technology          | Reason for Choice |
| :--- |:--------------------| :--- |
| **Language** | Java 25             | Strong typing and massive ecosystem. |
| **Framework** | Spring Boot 3       | Rapid development and "Convention over Configuration". |
| **Database** | PostgreSQL          | Robust ACID compliance for financial transactions. |
| **Security** | Spring Security 6   | Industry standard for protecting Java apps. |
| **Docs** | SpringDoc (OpenAPI) | Auto-generated API playground. |
| **Build Tool** | Maven               | Dependency management. |

---

##  Getting Started

### Prerequisites
* Java 17+ installed.
* Docker (for the database) OR a local PostgreSQL installation.
* Maven (optional, wrapper is included).

### 1. Start the Database
Use the provided `docker-compose.yml` to spin up PostgreSQL instantly.
```bash
docker-compose up -d
```

### 2.Configure Environment
Update src/main/resources/application.properties:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ticket_db
spring.datasource.username=admin 
spring.datasource.password=admin
jwt.secret=${JWT_SECRET}
```
don't forget to create ur own keySecret :)
### 3. Run The App 
```bash
./mvnw spring-boot:run
```

### 4. Access the API
* Docs: http://localhost:8080/swagger-ui/index.html
* Health: http://localhost:8080/api/v1/system/status

---

###  System Architektur
Security Flow 
1)  User sends credentials to /auth/authenticate.
2) Server verifies hash with BCrypt.
3) Server issues a JWT (signed with HS256).
4) User attaches header Authorization: Bearer <token> to future requests.
5) JwtAuthenticationFilter intercepts requests -> Validates Token -> Sets SecurityContext.

Database Design Notes
 * Table _user: Named with an underscore because user is a reserved keyword in PostgreSQL.
 * Table event: Contains a @Version column. This is the secret sauce for handling concurrency. 
 * If two users try to buy the last ticket at the exact same millisecond, the database version check will fail one of them automatically.
---
### Future Roadmap
* [ ] Dockerize the App: Create a Dockerfile for the Java application itself.
* [ ] Payment Gateway: Mock integration with Stripe/PayPal.
* [ ] Email Notifications: Send booking confirmation via SMTP.
* [ ] Testing: Add JUnit integration tests for the concurrency logic.

---

Author: [Aymen Zouinek]
