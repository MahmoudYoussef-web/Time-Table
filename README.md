

## 📌 Project Overview

This project is a **secure RESTful backend system** designed to automatically generate optimized university timetables.

The system combines:
- Genetic Algorithm–based optimization
- Strong constraint validation
- JWT-based authentication and authorization
- Clean layered architecture
- Fully documented REST APIs using Swagger

The generated schedules are **conflict-free**, **feasible**, and **optimized**, making the system suitable for academic and real-world scheduling environments.

---

## 🧠 Problem Description

University timetabling is a classical **NP-Hard optimization problem** that involves assigning:
- Courses
- Instructors
- Rooms
- Time slots

while satisfying multiple institutional constraints.

Manual scheduling is time-consuming, error-prone, difficult to optimize, and does not scale well.  
This system automates the process while ensuring correctness and quality.

---

## 🎯 Key Features

- Automated timetable generation using Genetic Algorithm
- Strict enforcement of hard constraints
- Optimization of soft constraints
- Stateless JWT-based security
- Role-based access control
- DTO-based API design
- Swagger UI with authorization support
- In-memory database for development
- Automatic seed data initialization

---

## 🏗️ System Architecture

The application follows a **layered architecture** with clear separation of concerns.

---

## 🧩 Package Responsibilities

| Package | Responsibility |
|------|--------------|
| `controller` | Exposes REST API endpoints |
| `service` | Business logic and orchestration |
| `ga` | Genetic Algorithm implementation |
| `model` | JPA entities (domain layer) |
| `dto` | Request and response data transfer objects |
| `repository` | Database access using Spring Data JPA |
| `auth` | Authentication and authorization logic |
| `config` | Security, Swagger, and initialization configuration |
| `mapper` | Entity to DTO mapping |

---

## 🔐 Security Architecture

The system uses **JWT (JSON Web Token)** for stateless authentication.

### Authentication Flow
1. User registers or logs in
2. Credentials are validated
3. A signed JWT token is generated
4. The token is returned to the client
5. Client sends the token in the `Authorization` header
6. Requests are authenticated via a JWT filter

### Authorization
- Role-based access (`ADMIN`, `SCHEDULER`, `INSTRUCTOR`)
- Secured endpoints require a valid JWT
- Public endpoints:
  - `/api/auth/**`
  - `/swagger-ui/**`
  - `/v3/api-docs/**`
  - `/h2-console/**`

---

## 🔑 Default Admin Initialization

A default admin user is automatically created at application startup to ensure system accessibility during development.

- Passwords are securely hashed
- Duplicate creation is prevented by email checks

---

## 🧾 DTO-Based API Design

The project separates:
- Persistence models (Entities)
- API contracts (DTOs)

### Benefits
- Prevents exposing internal database structure
- Protects sensitive fields
- Improves API stability
- Allows flexible response shaping

---

## 🧬 Genetic Algorithm Overview

The Genetic Algorithm is responsible for schedule optimization.

### Core Operations
- Initial population generation
- Tournament selection
- Crossover
- Mutation
- Elitism
- Early stopping condition

### Constraint Handling

#### Hard Constraints
- No instructor teaches multiple classes at the same time
- No room conflicts
- Room capacity must be sufficient
- No overlapping time slots

#### Soft Constraints
- Minimize instructor idle gaps
- Reduce back-to-back lectures
- Improve schedule distribution

---

## 📈 Fitness Evaluation

Fitness is calculated as:

```

Fitness = exp(-(HardViolations × HardWeight + SoftViolations × SoftWeight))

```

- Hard constraints dominate evaluation
- Soft constraints guide optimization
- Fitness values range between 0 and 1

---

## 🌐 API Documentation (Swagger)

All APIs are documented using Swagger / OpenAPI.

### Access Swagger UI
```

[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

```

Features:
- Interactive API testing
- JWT Authorize button
- Full request/response schemas

---

## 🧪 Database Configuration

The application uses **H2 In-Memory Database** for development.

### H2 Console
```

[http://localhost:8080/h2-console](http://localhost:8080/h2-console)

```
```

JDBC URL: jdbc:h2:mem:timetabledb
Username: sa
Password: (empty)

````

> Data is cleared when the application stops.

---

## ▶️ Running the Application

### Requirements
- Java 17+
- Maven

### Run Commands
```bash
mvn clean install
mvn spring-boot:run
````

Application runs on:

```
http://localhost:8080
```

---

## 📦 Sample API Response

```json
{
  "id": 1,
  "fitnessScore": 0.93,
  "entries": [
    {
      "courseCode": "CS101",
      "courseName": "Algorithms",
      "instructorName": "Dr. Ahmed",
      "roomNumber": "R1",
      "dayOfWeek": "MONDAY",
      "startTime": "09:00",
      "endTime": "11:00"
    }
  ]
}

