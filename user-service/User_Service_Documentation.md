# E-Commerce Microservices: User Service Documentation

This document provides a comprehensive overview of the developed **User Service** component, including database configurations, credentials, technologies, package structures, and API endpoints.

---

## 1. Technologies & Versions

*   **Language**: Java 17
*   **Framework**: Spring Boot (v4.1.0-parent / Spring 6.x)
*   **Security Framework**: Spring Security 6.x (Stateless Authentication)
*   **Database Engine**: PostgreSQL
*   **ORM / Data Access**: Hibernate 7.x / Spring Data JPA
*   **Token Standard**: JSON Web Token (JWT) using `io.jsonwebtoken:jjwt-api:0.11.5`
*   **Build Tool**: Maven

---

## 2. Port & Database Configurations

The application configuration parameters are defined inside `src/main/resources/application.yaml`.

### Application Server
*   **Service Port**: `8081`

### PostgreSQL Connection Details
*   **Host**: `localhost`
*   **Port**: `5432`
*   **Database Name**: `user_db`
*   **Username**: `postgres`
*   **Password**: `123123`
*   **Dialect**: `org.hibernate.dialect.PostgreSQLDialect`
*   **DDL Auto Strategy**: `update` (auto-creates tables)

---

## 3. Package & Directory Structure

```text
user-service/
├── src/main/java/com/ecommerce/userservice/
│   ├── UserServiceApplication.java
│   │
│   ├── controller/
│   │   ├── AuthController.java            # Registration and Login APIs
│   │   └── UserController.java            # Profile viewing & updating APIs
│   │
│   ├── service/
│   │   ├── AuthService.java               # Auth registration & login business logic
│   │   └── UserService.java               # Profile querying & updating logic
│   │
│   ├── repository/
│   │   └── UserRepository.java            # Database access interface
│   │
│   ├── entity/
│   │   ├── User.java                      # JPA entity implementing UserDetails
│   │   └── Role.java                      # Role enum (CUSTOMER, ADMIN)
│   │
│   ├── dto/
│   │   ├── RegisterRequest.java           # Signup validation DTO
│   │   ├── LoginRequest.java              # Signin validation DTO
│   │   ├── AuthResponse.java              # Auth response payload containing JWT
│   │   └── UserResponse.java              # Safe user profile presentation DTO
│   │
│   ├── security/
│   │   ├── ApplicationConfig.java         # Global authentication and encoder beans
│   │   ├── JwtService.java                # Token generation & validation utility
│   │   ├── JwtAuthenticationFilter.java   # Request filter verifying Bearer JWTs
│   │   └── SecurityConfig.java            # Authorization security filter chains
│   │
│   └── exception/
│       ├── ResourceNotFoundException.java # Safe HTTP 404 Exception
│       └── GlobalExceptionHandler.java    # Handles runtime & validation exceptions
│
└── src/main/resources/
    └── application.yaml                   # Port, DB credentials, & JWT configurations
```

---

## 4. API Specification & Sample Payloads

### A. Register User
*   **Method**: `POST`
*   **Endpoint**: `/api/auth/register`
*   **Authorization**: Public
*   **Payload**:
    ```json
    {
      "name": "John Doe",
      "email": "johndoe@example.com",
      "password": "securepassword123"
    }
    ```

### B. Login User
*   **Method**: `POST`
*   **Endpoint**: `/api/auth/login`
*   **Authorization**: Public
*   **Payload**:
    ```json
    {
      "email": "johndoe@example.com",
      "password": "securepassword123"
    }
    ```

### C. Get Current User Profile
*   **Method**: `GET`
*   **Endpoint**: `/api/users/me`
*   **Authorization**: Authenticated (Requires Header: `Authorization: Bearer <JWT_TOKEN>`)

### D. Update Current User Profile
*   **Method**: `PUT`
*   **Endpoint**: `/api/users/me`
*   **Authorization**: Authenticated (Requires Header: `Authorization: Bearer <JWT_TOKEN>`)
*   **Payload**:
    ```json
    {
      "name": "John Doe Updated",
      "password": "newpassword123"
    }
    ```
