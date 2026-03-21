# University Timetable Scheduler Backend (Smart Scheduling System)

This project is a backend system for generating optimized university timetables. It handles scheduling constraints, conflict detection, and automatic timetable generation.

The goal of this project was to build a complex scheduling system that simulates real-world university constraints such as instructor availability, room capacity, and time slot conflicts.

---

## Complete Tech Stack

* Java 21+
* Spring Boot
* Spring Data JPA (Hibernate)
* Spring Security
* JWT Authentication
* MySQL Database
* Maven
* Lombok
* MapStruct
* Swagger / OpenAPI

---

## Architecture

The project follows a clean layered architecture:

Controller → Service → Repository → Entity

Includes a scheduling engine responsible for generating optimized timetables based on constraints.

---

## Core Features

### Authentication

* Register / Login
* JWT-based authentication
* Role-based access (Admin / Instructor)

---

### Academic Management

* Courses management
* Departments management
* Instructors management
* Rooms management
* Sections management
* Semesters management

---

### Time Slots

* Create and manage time slots
* Define working days and hours

---

### Smart Scheduling Engine 

* Generate schedule automatically
* Assign:

  * Instructor
  * Room
  * Time slot
* Respect constraints:

  * Instructor availability
  * Room capacity
  * Section conflicts

---

### Conflict Detection

* Detect scheduling conflicts
* Return violations:

  * Hard constraints
  * Soft constraints

---

### Schedule Export

* Export schedule as:

  * PDF
  * Excel

---

### Async Job Processing

* Schedule generation runs as a background job
* Track job status (RUNNING / COMPLETED)

---

### Weekly Schedule View

* View structured weekly timetable
* Grouped by days and time slots

---

## Database Design

<p align="center">
  <img src="https://github.com/user-attachments/assets/dd3c265a-80e5-475e-b4cc-854a987beb13" width="900"/>
</p>

This diagram represents the relational database design including courses, instructors, rooms, schedules, constraints, and timetable entries.

---

## REST API Overview

All endpoints are prefixed with:

```id="m1gk8c"
/api
```

---

### Scheduling

* POST /schedules/generate/{semesterId}
* GET /schedules/jobs/{jobId}
* GET /schedules/{id}/conflicts
* GET /schedules/{id}/pdf
* GET /schedules/{id}/excel

---

### Weekly View

* GET /weekly-schedules/{id}
* GET /instructor/schedule/my

---

### Core Resources

* /courses
* /instructors
* /rooms
* /sections
* /semesters
* /timeslots

---

### Auth

* POST /auth/register
* POST /auth/login

---

## Key Highlights

* Complex scheduling algorithm
* Constraint-based system design
* Conflict detection engine
* Async processing with job tracking
* Real-world academic system simulation
* Clean architecture and modular design

---

## How to Run

1. Clone the repository

2. Configure application.properties

```properties id="s3gk9l"
spring.datasource.url=jdbc:mysql://localhost:3306/timetable_db
spring.datasource.username=your_user
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update

auth.jwt.secret=YOUR_SECRET
auth.jwt.expiration=3600000
```

3. Run the project

```bash id="x8p2kq"
mvn spring-boot:run
```

4. Open Swagger

```id="l4m9zn"
http://localhost:8080/swagger-ui/index.html
```

---

## Reflection

This project helped me:

* Design complex scheduling systems
* Work with constraint-based logic
* Build scalable backend architecture
* Handle asynchronous processing
* Model real-world academic systems

---

## Author

Mahmoud
Backend Developer (Spring Boot)

