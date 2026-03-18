# SIGES API

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-latest-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-latest-red.svg)](https://redis.io/)

> 🌐 [English version](README.md)

SIGES (Sistema de Gestión de Equipos y Espacios) API es un backend modular y robusto construido con Spring Boot 4.0.2 y Java 21, diseñado para gestionar recursos institucionales, reservaciones y notificaciones de forma eficiente.

---

## Objetivo

El objetivo principal de **SIGES API** es proveer una infraestructura backend escalable y segura para gestionar:
- **Usuarios y Autenticación**: Autenticación segura basada en JWT y control de acceso por roles.
- **Reservables**: Administración de espacios físicos y equipos.
- **Reservaciones**: Proceso simplificado para reservar y gestionar recursos.
- **Notificaciones**: Notificaciones push y por correo en tiempo real para distintos eventos del sistema.
- **Agenda y Reportes**: Programación de actividades e insights basados en datos mediante estadísticas del dashboard.

---

## Stack Tecnológico

- **Core**: Java 21, Spring Boot 4.0.2
- **Persistencia**: PostgreSQL (RDBMS), Spring Data JPA, Flyway (Migraciones)
- **Caché y Mensajería**: Redis
- **Seguridad**: Spring Security, JWT (JSON Web Tokens)
- **Servicios Cloud**: AWS S3 (Almacenamiento), AWS CloudFront (CDN), Firebase Admin (Push Notifications)
- **Comunicación**: Resend (API de correo)
- **Mapeo y Utilidades**: MapStruct, Lombok, Apache Commons, Google LibPhonenumber
- **Testing**: JUnit 5, Testcontainers, Mockito

---

## Arquitectura

El proyecto sigue una arquitectura de **Monolito Modular**, donde cada dominio de negocio está encapsulado en su propio paquete. Esto promueve la separación de responsabilidades y facilita la escalabilidad.

---

## Estructura del Proyecto
```text
src/main/java/dev/spiffocode/sigesapi/
├── agenda/          # Gestión de horarios y disponibilidad
├── auth/            # Autenticación, JWT y configuración de seguridad
├── common/          # Utilidades compartidas, clases base y excepciones globales
├── logbook/         # Seguimiento de actividad y logs
├── mailsender/      # Integración con servicio de correo (Resend)
├── notifications/   # Lógica de notificaciones del sistema (Push/In-app)
├── reports/         # Estadísticas del dashboard y lógica de reportes
├── reservables/     # Administración de Espacios y Equipos
├── reservations/    # Flujos principales de reservación
└── users/           # Perfiles de usuario, registro y gestión
```

---

## 🚀 Inicio Rápido

### Prerequisitos

- **Java 21 JDK**
- **Docker & Docker Compose**
- **Gradle** (incluido via wrapper)

### Configuración

1. **Clona el repositorio**:
```bash
   git clone <repository-url>
   cd siges-api
```

2. **Configura las variables de entorno**:
   Copia `.env.example` a `.env` y llena los valores requeridos:
```bash
   cp .env.example .env
```

3. **Levanta la infraestructura**:
   Usa Docker Compose para iniciar PostgreSQL y Redis:
```bash
   docker compose up -d
```

4. **Ejecuta la aplicación**:
```bash
   ./gradlew bootRun
```

La API estará disponible en `http://localhost:8080`.

---

## 📖 Documentación de la API

El proyecto incluye documentación interactiva de la API mediante **SpringDoc OpenAPI (Swagger)**.
Una vez que la aplicación esté corriendo, puedes acceder en:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

---

## 🧪 Testing

El proyecto usa **JUnit 5** y **Testcontainers** para pruebas unitarias y de integración.

- **Ejecutar todas las pruebas**:
```bash
  ./gradlew test
```
- **Ejecutar pruebas con cobertura**:
```bash
  ./gradlew test jacocoTestReport
```

---

## 📄 Licencia

Este proyecto es propietario y de uso interno en la UTEZ.