# Secure RESTful API for Resource Booking System

A secure, scalable RESTful Resource Booking System API built with **Spring Boot 3**, **Java 17+**, **Spring Security 6**, **JWT Authentication**, **Role-Based Access Control (RBAC)**, and **Spring Data JPA**.

---

## Key Features

- **JWT Authentication & Stateless Security**: `POST /auth/login` and `POST /auth/register` endpoints returning signed JWT Bearer tokens. Password security enforced with BCrypt password encoding.
- **Role-Based Access Control (RBAC)**:
  - `ROLE_ADMIN`: Full CRUD permissions on resources (`/resources/**`) and reservations (`/reservations/**`).
  - `ROLE_USER`: Read-only access to resources (`GET /resources/**`), permission to create reservations, and view/manage *only their own reservations*.
- **JWT-Based Identity Resolution**: User identity is strictly resolved from the authenticated SecurityContext (JWT token), preventing identity spoofing in reservation creation requests.
- **Reservation Management**:
  - Reservation statuses: `PENDING`, `CONFIRMED`, `CANCELLED`.
  - Precise decimal pricing using Java `BigDecimal`.
  - Multi-criteria filtering by `status`, `minPrice`, and `maxPrice`.
  - Pagination using `page` and `size` parameters.
  - Dynamic sorting using `sortBy` and `sortDir` parameters.
- **Database Support**: Built-in zero-configuration **H2 Database** for immediate local running and testing, with seamless support for **MySQL** and **PostgreSQL**.
- **Interactive OpenAPI/Swagger Documentation**: Interactive API documentation generated dynamically via SpringDoc Swagger UI.

---

## Seed Accounts & Testing Credentials

The application automatically seeds initial admin and user accounts on startup if not already present:

| Role | Email | Password | Access Rights |
| :--- | :--- | :--- | :--- |
| **ADMIN** | `admin@booking.com` | `Admin@123` | Full CRUD on resources & all reservations |
| **USER** | `user@booking.com` | `User@123` | Read-only resources, Create & view own reservations |

---

## API Endpoints Summary

### Authentication Endpoints (`/auth`)
- `POST /auth/login` - Authenticate user and receive JWT token.
- `POST /auth/register` - Register a new user account (defaults to `ROLE_USER`).

### Resource Endpoints (`/resources`)
- `GET /resources` - Get all resources (Paginated & Sortable). *Available to USER and ADMIN*.
- `GET /resources/{id}` - Get resource details by ID. *Available to USER and ADMIN*.
- `POST /resources` - Create a new resource. *Requires ADMIN role*.
- `PUT /resources/{id}` - Update a resource. *Requires ADMIN role*.
- `DELETE /resources/{id}` - Delete a resource. *Requires ADMIN role*.

### Reservation Endpoints (`/reservations`)
- `GET /reservations` - Filter & paginate reservations. *ADMIN views all; USER views only their own*.
  - Query Parameters: `status` (PENDING, CONFIRMED, CANCELLED), `minPrice`, `maxPrice`, `page` (default: 0), `size` (default: 10), `sortBy` (default: id), `sortDir` (asc/desc).
- `GET /reservations/{id}` - Get reservation by ID (Ownership verified).
- `POST /reservations` - Create a reservation (User ID extracted from JWT).
- `PUT /reservations/{id}/status` - Update reservation status (`PENDING`, `CONFIRMED`, `CANCELLED`).
- `DELETE /reservations/{id}` - Cancel reservation (USER) or delete reservation (ADMIN).

---

## Prerequisites & Requirements

- **Java JDK 17** or higher (Java 21/24 supported)
- Maven 3.8+ (Provided via included `./mvnw` Maven Wrapper)

---

## Quick Start & Running the Application

### 1. Run using Maven Wrapper (Default H2 In-Memory Database)
```bash
./mvnw spring-boot:run
```
*(On Windows Command Prompt / PowerShell: `.\mvnw.cmd spring-boot:run`)*

The application will start on port `8080`.

### 2. Access Swagger UI API Documentation
Open your browser and navigate to:
```
http://localhost:8080/swagger-ui.html
```

### 3. Access H2 Console
```
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:bookingdb
User Name: sa
Password: (leave blank)
```

---

## Running Unit and Integration Tests

To run the automated security, RBAC, reservation filtering, and validation tests:

```bash
./mvnw clean test
```

---

## Database Configuration & Environment Variables

### H2 Database (Default)
Default configuration in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:h2:mem:bookingdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
```

### MySQL Configuration
To connect to MySQL, update `application.properties` or set environment variables:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bookingdb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:rootpassword}
spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

### PostgreSQL Configuration
To connect to PostgreSQL, update `application.properties` or set environment variables:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/bookingdb
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
spring.datasource.driverClassName=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

---

## Example cURL Usage

### 1. Login to obtain JWT Token
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "user@booking.com", "password": "User@123"}'
```

### 2. Create a Reservation (USER)
```bash
curl -X POST http://localhost:8080/reservations \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "resourceId": 1,
    "startTime": "2026-10-01T09:00:00",
    "endTime": "2026-10-01T12:00:00",
    "price": 150.00,
    "status": "PENDING"
  }'
```

### 3. Filter & Paginate Reservations
```bash
curl -X GET "http://localhost:8080/reservations?status=CONFIRMED&minPrice=50.00&maxPrice=300.00&page=0&size=5&sortBy=price&sortDir=asc" \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>"
```
