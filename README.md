# SIGES API 

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-latest-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-latest-red.svg)](https://redis.io/)

> 🌐 [Versión español](README.es.md)

SIGES (Sistema de Gestión) API is a robust, modular backend built with Spring Boot 4.0.2 and Java 21, designed to manage institutional resources, reservations, and notifications efficiently.

---

## Objective

The primary goal of **SIGES API** is to provide a scalable and secure backend infrastructure for managing:
- **Users and Authentication**: Secure JWT-based authentication and role-based access control.
- **Reservables**: Management of physical spaces and equipment.
- **Reservations**: A streamlined process for booking and managing resources.
- **Notifications**: Real-time push and email notifications for various system events.
- **Agenda & Reports**: Scheduling and data-driven insights through dashboard statistics and reports.

---

## Tech Stack

- **Core**: Java 21, Spring Boot 4.0.2
- **Persistence**: PostgreSQL (RDBMS), Spring Data JPA, Flyway (Migrations)
- **Caching & Messaging**: Redis
- **Security**: Spring Security, JWT (JSON Web Tokens)
- **Cloud Services**: AWS S3 (Storage), AWS CloudFront (CDN), Firebase Admin (Push Notifications)
- **Communication**: Resend (Email API)
- **Mapping & Utilities**: MapStruct, Lombok, Apache Commons, Google LibPhonenumber
- **Testing**: JUnit 5, Testcontainers, Mockito

---

## Architecture

The project follows a **Modular Monolith** architecture, where each business domain is encapsulated in its own package. This promotes separation of concerns and facilitates scalability.

---

## Project Structure

```text
src/main/java/dev/spiffocode/sigesapi/
├── agenda/          # Scheduling and availability management
├── auth/            # Authentication, JWT, and Security config
├── common/          # Shared utilities, base classes, and global exceptions
├── logbook/         # Activity tracking and logs
├── mailsender/      # Email service integration (Resend)
├── notifications/   # System-wide notification logic (Push/In-app)
├── reports/         # Dashboard statistics and reporting logic
├── reservables/     # Management of Spaces and Equipment
├── reservations/    # Core reservation workflows
└── users/           # User profiles, registration, and management
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 21 JDK**
- **Docker & Docker Compose**
- **Gradle** (included via wrapper)

### Setup

1.  **Clone the repository**:
    ```bash
    git clone <repository-url>
    cd siges-api
    ```

2.  **Configure environment variables**:
    Copy `.env.example` to `.env` and fill in the required values:
    ```bash
    cp .env.example .env
    ```

3.  **Spin up infrastructure**:
    Use Docker Compose to start PostgreSQL and Redis:
    ```bash
    docker compose up -d
    ```

4.  **Run the application**:
    ```bash
    ./gradlew bootRun
    ```

The API will be available at `http://localhost:8080`.

---

## 📖 API Documentation

The project includes interactive API documentation via **SpringDoc OpenAPI (Swagger)**.
Once the application is running, you can access it at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

---

## 🧪 Testing

The project uses **JUnit 5** and **Testcontainers** for both unit and integration tests.

- **Run all tests**:
  ```bash
  ./gradlew test
  ```
- **Run tests with coverage**:
  ```bash
  ./gradlew test jacocoTestReport
  ```

---

## 📄 License

This project is proprietary and for internal use within UTEZ.
