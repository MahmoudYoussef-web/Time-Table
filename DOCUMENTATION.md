# Timetable Scheduler — Project Documentation

> **Version:** 0.0.1-SNAPSHOT  
> **Java:** 21 | **Spring Boot:** 3.2.5 | **Database:** MySQL 8  
> **Algorithm:** Genetic Algorithm | **PDF:** OpenPDF | **Excel:** Apache POI

---

## 📑 Table of Contents

1. [Project Overview](#1-project-overview)
2. [System Architecture](#2-system-architecture)
3. [API Endpoints](#3-api-endpoints)
4. [Genetic Algorithm](#4-genetic-algorithm)
5. [Constraint System](#5-constraint-system)
6. [Database Schema](#6-database-schema)
7. [PDF Export Design](#7-pdf-export-design)
8. [Excel Export Design](#8-excel-export-design)
9. [Security & Authentication](#9-security--authentication)
10. [Known Issues & Fixes](#10-known-issues--fixes)
11. [Configuration Reference](#11-configuration-reference)
12. [Development Guide](#12-development-guide)

---

## 1. Project Overview

### 1.1 What It Does
Timetable Scheduler automates university timetable generation using a **Genetic Algorithm (GA)**. It takes sections, instructors, rooms, and time slots as input and produces conflict-free schedules optimized for quality.

### 1.2 Key Capabilities
- **CRUD Management** — Full REST API for managing courses, instructors, rooms, sections, semesters, time slots, departments
- **GA Schedule Generation** — Asynchronous GA with configurable population, generations, crossover, mutation
- **Hard Constraints** — 5 hard constraints ensuring zero conflicts (room, instructor, capacity, time, availability)
- **Soft Constraints** — 5 soft constraints optimizing schedule quality (idle gaps, back-to-back, day distribution)
- **PDF Export** — Professional A4 landscape PDF with color-coded tables per year level
- **Excel Export** — Multi-sheet workbook with freeze panes and colors
- **JWT Authentication** — Stateless auth with ADMIN, SCHEDULER, INSTRUCTOR roles
- **Swagger UI** — Interactive API docs at `/swagger-ui/index.html`

### 1.3 Problem Domain
University timetabling is NP-Hard. Traditional manual scheduling:
- Takes days/weeks of administrative work
- Is error-prone (double-booked rooms, instructor conflicts)
- Cannot optimize for quality (idle gaps, back-to-back lectures)
- Hard to adapt to changes (room availability, instructor preferences)

### 1.4 Solution Approach
The Genetic Algorithm treats each schedule as a "chromosome" (array of genes), where each gene maps a section → (room × timeSlot × instructor). Through evolution (selection, crossover, mutation), the population converges toward feasible, high-quality schedules.

---

## 2. System Architecture

### 2.1 Layered Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Client / Swagger UI                    │
└──────────────────────┬──────────────────────────────────┘
                       │ HTTP
┌──────────────────────▼──────────────────────────────────┐
│                 REST Controllers                          │
│  [Academic] [Schedule] [Auth] [Admin]                    │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│                 Service Layer                             │
│  ScheduleService  GeneticScheduleService  ConflictService │
│  InstructorService  SectionService  SemesterService ...   │
└──────┬───────────────────────┬─────────────────────────┬─┘
       │                       │                         │
┌──────▼──────────┐  ┌────────▼───────┐  ┌──────────────▼┐
│  GA Engine      │  │  Export        │  │  Repository   │
│  GeneticAlgo    │  │  PdfService    │  │  JPA/MySQL    │
│  FitnessCalc    │  │  ExcelService  │  │               │
└─────────────────┘  └────────────────┘  └───────────────┘
```

### 2.2 Key Design Decisions

| Decision | Rationale |
|---|---|
| **Static mapper classes** | Simpler than mapping frameworks, no reflection overhead |
| **Record DTOs** | Immutable request/response objects, built-in equals/hashCode |
| **Commented-out SoftConstraintEvaluator** | Replaced by FitnessCalculator for integrated GA scoring |
| **Separate `soft.SoftConstraint` interface** | Avoids interface pollution — parent `constraints.SoftConstraint` was deleted in v2 cleanup |
| **`@Transactional` on services** | Ensures lazy loading works for all entity relationships |
| **Enum for SessionType/YearLevel** | Type safety instead of raw strings |
| **Async job generation** | GA can take time; returns jobId for polling |

### 2.3 Package Structure

```
com.example.timetable/
├── TimetableSchedulerApplication.java
├── auth/
│   ├── config/
│   │   ├── SecurityConfig.java          # JWT + role security
│   │   └── JwtFilter.java
│   ├── dto/
│   │   ├── AuthResponse.java
│   │   ├── LoginRequest.java
│   │   └── RegisterRequest.java
│   ├── filter/
│   │   └── JwtFilter.java
│   └── service/
│       └── AuthService.java
├── config/
│   ├── AsyncConfig.java
│   ├── DataInitializer.java
│   ├── DataLoader.java
│   └── OpenApiConfig.java
├── controller/
│   ├── academic/                        # CRUD: Course, Instructor, Room, Section, Semester, TimeSlot, Dept
│   ├── admin/
│   │   └── AdminController.java
│   ├── auth/
│   │   └── AuthController.java
│   └── schedule/
│       ├── InstructorScheduleController.java
│       ├── ScheduleController.java
│       └── WeeklyScheduleController.java
├── dto/
│   ├── request/                         # 9 request DTOs with Jakarta Validation
│   └── response/                        # Response DTOs + ScheduleDTO, SlotDTO, etc.
├── entity/
│   ├── enums/
│   │   ├── JobStatus.java
│   │   ├── ScheduleStatus.java
│   │   ├── SemesterStatus.java
│   │   ├── SessionType.java             # LECTURE, LAB, TUTORIAL, SEMINAR
│   │   ├── UserRole.java
│   │   └── YearLevel.java               # FIRST, SECOND, THIRD, FOURTH
│   ├── Course.java
│   ├── Department.java
│   ├── Enrollment.java
│   ├── Exam.java
│   ├── Instructor.java
│   ├── InstructorAvailability.java
│   ├── Room.java
│   ├── Schedule.java
│   ├── ScheduleEntry.java
│   ├── ScheduleGenerationJob.java
│   ├── ScheduleHistory.java
│   ├── Section.java
│   ├── Semester.java
│   ├── Student.java
│   ├── TimeSlot.java
│   └── User.java
├── exception/
│   ├── ApiError.java
│   ├── GlobalExceptionHandler.java      # 10 exception handlers
│   ├── InvalidCredentialsException.java
│   ├── UserAlreadyExistsException.java
│   └── UserNotFoundException.java
├── mapper/                              # 9 static mapper classes
├── repository/                          # 13 JPA repositories
├── scheduling/
│   ├── algorithm/
│   │   ├── config/                      # FitnessProperties, GA config
│   │   ├── crossover/
│   │   │   └── SinglePointCrossover.java
│   │   ├── mutation/
│   │   │   └── RandomMutation.java
│   │   ├── selection/
│   │   │   └── TournamentSelection.java
│   │   ├── Chromosome.java
│   │   ├── FitnessCalculator.java
│   │   ├── Gene.java
│   │   └── GeneticAlgorithm.java
│   └── constraints/
│       ├── hard/                        # 5 hard constraints
│       ├── soft/                        # 5 soft constraints (interface + implementations)
│       ├── Constraint.java
│       ├── ConstraintType.java
│       ├── ConstraintViolation.java
│       ├── HardConstraint.java
│       └── HardConstraintValidator.java
├── service/
│   ├── impl/                            # Service implementations
│   ├── ConflictEvaluationService.java
│   ├── GeneticScheduleService.java      # GA orchestrator
│   ├── ScheduleExcelService.java
│   ├── SchedulePdfService.java
│   └── (interfaces)
```

---

## 3. API Endpoints

### 3.1 Authentication (`/api/auth`)

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Create new user account |
| POST | `/api/auth/login` | Public | Login → returns JWT token |
| GET | `/api/auth/me` | Authenticated | Get current user profile |

**POST /api/auth/register:**
```json
{
  "fullName": "John Doe",
  "email": "john@uni.com",
  "password": "password123",
  "role": "INSTRUCTOR"
}
```

**POST /api/auth/login:**
```json
{
  "email": "admin@uni.com",
  "password": "admin123"
}
```
**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "fullName": "Admin User",
  "email": "admin@uni.com",
  "role": "ADMIN"
}
```

### 3.2 Academic Data (`/api/...`)

| Entity | Path | Methods | Auth |
|---|---|---|---|
| Courses | `/api/courses` | GET, POST | GET: ADMIN/SCHEDULER/INSTRUCTOR, POST: ADMIN |
| | `/api/courses/{id}` | GET, PUT, DELETE | PUT/DELETE: ADMIN |
| Rooms | `/api/rooms` | GET, POST | GET: all authenticated, POST: ADMIN |
| | `/api/rooms/{id}` | PUT, DELETE | ADMIN |
| Instructors | `/api/instructors` | GET, POST | GET: all authenticated, POST: ADMIN |
| | `/api/instructors/{id}` | PUT, DELETE | ADMIN |
| Sections | `/api/sections` | GET, POST | POST: ADMIN/SCHEDULER |
| | `/api/sections/{id}` | PUT, DELETE | PUT: ADMIN/SCHEDULER, DELETE: ADMIN |
| Semesters | `/api/semesters` | GET, POST | POST: ADMIN |
| | `/api/semesters/{id}` | PUT | ADMIN |
| Time Slots | `/api/timeslots` | GET, POST | POST: ADMIN |
| | `/api/timeslots/{id}` | PUT, DELETE | ADMIN |
| Departments | `/api/departments` | GET, POST | POST: ADMIN |
| | `/api/departments/{id}` | PUT, DELETE | ADMIN |

### 3.3 Schedule Generation (`/api/schedules`)

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/schedules/generate/{semesterId}` | ADMIN | Start async GA generation |
| GET | `/api/schedules/jobs/{jobId}` | Authenticated | Poll generation status |
| GET | `/api/schedules/{id}` | Authenticated | Get schedule by ID |
| GET | `/api/schedules/{id}/pdf` | Authenticated | Export PDF |
| GET | `/api/schedules/{id}/excel` | Authenticated | Export Excel |
| GET | `/api/schedules/{id}/conflicts` | Authenticated | List constraint violations |
| GET | `/api/schedules/current` | Authenticated | Get latest published schedule |
| PUT | `/api/schedules/{id}/validate` | ADMIN | Validate draft |
| PUT | `/api/schedules/{id}/publish` | ADMIN | Publish validated |
| PUT | `/api/schedules/{id}/lock` | ADMIN | Lock published |
| PUT | `/api/schedules/entries/{id}/lock` | ADMIN | Lock specific entry |

### 3.4 Weekly & Instructor Schedules

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/weekly-schedules/{id}` | Authenticated | Full weekly schedule |
| GET | `/api/instructor/schedule/my` | INSTRUCTOR | Current instructor's schedule |

### 3.5 Admin

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/admin/ping` | ADMIN | Health check |

---

## 4. Genetic Algorithm

### 4.1 Algorithm Flow

```
1. Load sections, rooms, time slots for semester
2. Preload instructor availability cache
3. Initialize population (random chromosomes)
4. For each generation:
   a. Evaluate fitness for all chromosomes
   b. Check early-stop condition
   c. Select parents (tournament)
   d. Apply crossover
   e. Apply mutation
   f. Apply elitism
5. Return best chromosome
6. Convert chromosome → Schedule entries (de-duplicate by instructor/room/section per slot)
```

### 4.2 Chromosome Representation

Each chromosome is a `List<Gene>` where each gene = `(Section, Room, TimeSlot)`.  
The GA operates on these genes through crossover and mutation.

### 4.3 GA Parameters (configurable in `application.properties`)

| Property | Default | Description |
|---|---|---|
| `ga.population-size` | 20 | Number of chromosomes per generation |
| `ga.max-generations` | 20 | Maximum iterations |
| `ga.crossover-rate` | 0.8 | Probability of crossover |
| `ga.mutation-rate` | 0.1 | Probability of mutation |
| `ga.elitism-count` | 2 | Top N chromosomes keep unchanged |
| `ga.early-stop-threshold` | 0.90 | If fitness >= threshold, stop early |
| `ga.max-execution-millis` | 2000 | Time limit per generation run |

### 4.4 Duplicate Gene Filtering

After evolution, if multiple sections map to the same (room × slot) or (instructor × slot), only one wins. The rest are dropped. A warning log shows dropped sections.

---

## 5. Constraint System

### 5.1 Interface Hierarchy

```
Constraint (interface)
├── getName() : String
├── getType() : ConstraintType
├── violations(Chromosome) : int
└── explain(Chromosome) : List<ConstraintViolation>
    │
    ├── HardConstraint (marker, getType = HARD)
    │
    └── SoftConstraint (in constraints.soft package)
        ├── name() : String
        ├── weight() : double
        └── violations(Chromosome) : int
```

**Note:** The original `constraints.SoftConstraint` (parent package) was removed in v2 because it conflicted with `constraints.soft.SoftConstraint`. The `soft.SoftConstraint` interface now serves as the sole contract for soft constraints.

### 5.2 Hard Constraints (Zero-Tolerance)

| Class | Purpose | Method |
|---|---|---|
| `RoomConflictConstraint` | No two sections in same room × slot | `explain()` |
| `InstructorConflictConstraint` | No instructor assigned twice in same slot | `explain()` |
| `TimeOverlapConstraint` | No overlapping times for same section, room, or instructor | `violations()` |
| `RoomCapacityConstraint` | Room capacity ≥ section enrollment | `violations()` |
| `InstructorAvailabilityConstraint` | Instructor available during slot (thread-safe cache) | `violations()` |

Hard constraints are injected via `List<HardConstraint>` into `FitnessCalculator`.

### 5.3 Soft Constraints (Optimization Targets)

| Class | Weight | Purpose |
|---|---|---|
| `InstructorBackToBackConstraint` | 2.0 | Count consecutive lectures (back-to-back) |
| `InstructorGapPreferenceConstraint` | 1.5 | Count gaps < 30 minutes between lectures |
| `SameCourseSameDayConstraint` | 1.0 | Count same-course repetitions on same day |
| `InstructorIdleSoftConstraint` | 1.0 | Count excessive idle time (>2h/day) |
| `StudentIdleSoftConstraint` | 1.0 | Count student idle time (>2h/day) |

Soft constraints are injected via `List<SoftConstraint>` into `FitnessCalculator`.

### 5.4 Fitness Formula

```
fitness = exp(-(hardViolations × hardWeight + Σ(softViolation_i × weight_i) × softWeight))
```

- Range: (0, 1], higher is better
- Hard violations dominate (prevent infeasible schedules)
- Soft violations guide optimization (prefer higher quality)

---

## 6. Database Schema

### 6.1 Entity Relationship

```
Semester ◄── Schedule ◄── ScheduleEntry ──► Room
    ▲                          │              ▲
    │                          │              │
    └── Section ◄──────────────┘              │
         │                                    │
         ├── Course ──► Department            │
         │                                    │
         ├── Instructor ──► User              │
         │                                    │
         └── Enrollment ──► Student           │
                                               │
    TimeSlot ◄─────────────────────────────────┘
```

### 6.2 Key Constraints

- `schedule_entries` has unique constraints on:
  - `(room_id, time_slot_id)` — no double-booking rooms
  - `(instructor_id, time_slot_id)` — no double-booking instructors
  - `(section_id, time_slot_id)` — no double-booking sections
- `instructor_availability` references `(instructor_id, time_slot_id)`
- All time comparisons use `DayOfWeek` + `LocalTime`

### 6.3 Indexes

- `schedules`: `idx_schedule_semester` (semester_id), `idx_schedule_status` (status)
- `schedule_entries`: implicit unique constraint indexes

---

## 7. PDF Export Design

### 7.1 Overview

The PDF is generated by `SchedulePdfService.java` using **OpenPDF** (LGPL, no license fees).  
Format: A4 Landscape (rotated), 30pt margins.

### 7.2 Color Palette

| Element | Background | Foreground | Usage |
|---|---|---|---|
| Header | `#1A3560` (kohli) | White | Day columns, title |
| Lecture Cell | `#DBEAFE` (light blue) | `#1D4ED8` (dark blue) | Regular lectures |
| Section/Lab Cell | `#DCFCE7` (light green) | `#15803D` (dark green) | Lab/Section/Tutorial |
| Break Row | `#FEF3C7` (light yellow) | `#92400E` (brown) | Break between sessions |
| Time Column (even) | `#F9FAFB` | `#374151` | Zebra striped |
| Time Column (odd) | White | `#374151` | Zebra striped |

### 7.3 Page Structure

Each year level gets its own page:

```
┌────────────────────────────────────────────────────────────┐
│              UNIVERSITY STUDY SCHEDULE                      │
│              ─────────────────────────                      │
│              DEPARTMENT WEEKLY SCHEDULE                     │
│     First Year — Semester: Fall 2026                        │
│     For The Week: 01 Sep 2026 — 30 Jan 2027                 │
├────────┬────────┬────────┬────────┬────────┬────────┬───────┤
│  TIME  │SATURDAY│ SUNDAY │ MONDAY │ TUESDAY│WEDNESDAY│THURSDAY│
├────────┼────────┼────────┼────────┼────────┼────────┼───────┤
│ 8:00 AM│CS101   │        │        │ MATH201│        │       │
│        │Programming│     │        │ Calculus│        │       │
│        │Dr. Ahmed│        │        │T.A Sara│        │       │
│        │Hall: A101│       │        │Lab: LAB1│       │       │
│        │(Lecture)│        │        │(Section)│       │       │
├────────┼────────┼────────┼────────┼────────┼────────┼───────┤
│10:00 AM│        │  ...   │        │        │        │       │
├────────┼────────┼────────┼────────┼────────┼────────┼───────┤
│ BREAK  │ BREAK  │ BREAK  │ BREAK  │ BREAK  │ BREAK  │ BREAK │
├────────┼────────┼────────┼────────┼────────┼────────┼───────┤
│12:00 PM│        │        │        │        │        │       │
├────────┼────────┼────────┼────────┼────────┼────────┼───────┤
│ 2:00 PM│        │        │        │        │        │       │
├────────┴────────┴────────┴────────┴────────┴────────┴───────┤
│ Notes: • Lectures are held in Main Hall.                    │
│        • Sections (Sakan) are led by Teaching Assistants.   │
│ LEGEND: [■ Lecture] [■ Section (Sakan)] [■ Break]          │
└─────────────────────────────────────────────────────────────┘
```

### 7.4 Summary Page

Before the table pages, a summary page displays:
- DEPARTMENT WEEKLY SCHEDULE — OVERVIEW
- Semester name and date range
- Generation timestamp
- Year levels included, total sections, total entries
- Hard violations, soft violations, fitness score

### 7.5 Time Format

- Times displayed in 12-hour format: `8:00 AM`, `2:00 PM`
- Ordered ascending within each day
- Break row inserted between AM (before 12:00) and PM (after 14:00) sessions
- Break only shown if afternoon sessions exist

### 7.6 Row Types

**Lecture Row:**
- Background: `#DBEAFE` (light blue)
- Content: `CS101 - Programming\nDr. Ahmed\nHall: A101\n(Lecture)`
- Note: No "Dr." prefix is added — the instructor name is used as-is

**Section/Lab Row:**
- Background: `#DCFCE7` (light green)
- Content: `CS101 - Programming\nT.A Omar\nLab: LAB1\n(Section)`
- Border: `#86EFAC` (green)

**Empty Cell:**
- Background: alternating `#F9FAFB` / White
- No content

### 7.7 Footer

```
Generated by TimetableScheduler — 28 May 2026    Page 3 of 5
```

- 8pt italic, gray color
- Centered at bottom of page

---

## 8. Excel Export Design

`ScheduleExcelService.java` generates `.xlsx` files using Apache POI.

### 8.1 Features

| Feature | Implementation |
|---|---|
| Multi-sheet | One sheet per year level (First–Fourth) |
| Freeze pane | First row + first column frozen |
| Color coding | Header: dark blue, Lecture: pale blue, Section: green, Break: yellow |
| Borders | Thin borders on all cells |
| Time format | AM/PM (h:mm a) |
| Auto-size | Columns auto-sized after data fill |
| Break row | Same logic as PDF (between AM/PM) |

### 8.2 Sheet Layout

| TIME | SATURDAY | SUNDAY | MONDAY | TUESDAY | WEDNESDAY | THURSDAY |
|---|---|---|---|---|---|---|
| 8:00 AM | CS101 - Programming\nDr. Ahmed \| A101 | | | | | |
| 10:00 AM | | MATH201 - ... | | | | |
| BREAK | BREAK | BREAK | BREAK | BREAK | BREAK | BREAK |
| 12:00 PM | | | | | | |

---

## 9. Security & Authentication

### 9.1 JWT Authentication

- **Library:** JJWT 0.11.5
- **Algorithm:** HS256
- **Secret:** Configurable via `JWT_SECRET` env var (fallback: hardcoded dev secret)
- **Expiration:** 900,000ms (15 minutes)

### 9.2 Roles & Permissions

| Role | Permissions |
|---|---|
| `ADMIN` | Full access: CRUD all entities, generate/validate/publish/lock schedules |
| `SCHEDULER` | Create/Edit sections, view all, cannot manage users |
| `INSTRUCTOR` | View own schedule, read-only access to courses/rooms |

### 9.3 Security Configuration

- Stateless sessions (no HTTP session)
- CSRF disabled
- Public endpoints: `/api/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`
- Error responses: `401 Unauthorized` with JSON body

### 9.4 Production Considerations

- `server.error.include-stacktrace=never`
- `server.error.include-exception=false`
- `server.error.include-message=never`
- Logging at WARN level

---

## 10. Known Issues & Fixes

### 10.1 Issues Fixed (v2)

| # | Issue | Fix |
|---|---|---|
| 1 | `PasswordConfig.java` + `SecurityConfig.java` both define `PasswordEncoder` bean | Deleted `PasswordConfig.java` — `SecurityConfig` has the single `@Bean` |
| 2 | `constraints.SoftConstraint` vs `constraints.soft.SoftConstraint` naming collision | Removed `constraints.SoftConstraint` (marker interface); `soft.SoftConstraint` is the sole soft constraint contract |
| 3 | `SoftConstraintConfig.java` created manual bean list (redundant) | Deleted — Spring auto-injects all `soft.SoftConstraint` beans into `FitnessCalculator` |
| 4 | `InstructorAvailabilityConstraint.preload()` never called | Called in `GeneticScheduleService.generate()` before GA evolution |
| 5 | `Section.type` always set to `LECTURE` | Added `determineSessionType()` method checking section name for "LAB"/"TUT" |
| 6 | No session type detection in GA | `determineSessionType()` returns LECTURE/LAB/TUTORIAL based on section name |
| 7 | PDF had "Dr." prefix hardcoded | Removed prefix — uses instructor name as-is |
| 8 | PDF session type not shown in cells | Added session label in parentheses: (Lecture), (Lab), (Section) |
| 9 | PDF time slots used short day names (SAT, SUN...) | Changed to full names (SATURDAY, SUNDAY...) |
| 10 | PDF break position based on data (not fixed) | Break now inserted before first slot ≥12:00, only if afternoon slots exist |
| 11 | PDF header missing date range | Added semester start/end date from ScheduleDTO |
| 12 | No summary page in PDF | Added overview page with stats |
| 13 | Excel had single sheet, no formatting | Multi-sheet, freeze pane, colors, borders, auto-size |

### 10.2 Verification Checklist

- [x] `mvn compile` passes
- [x] `mvn test` passes
- [x] No `BeanDefinitionOverrideException` on startup
- [x] POST `/api/auth/login` returns JWT
- [x] POST `/api/schedules/generate/{semesterId}` returns jobId
- [x] PDF exports with design (A4 landscape, color-coded, legend, footer)
- [x] No "Dr." prefix in PDF cells
- [x] Session type shown in parentheses in PDF
- [x] Break row between AM/PM sessions
- [x] Summary page at start of PDF
- [x] Excel with multiple sheets, freeze pane, colors

### 10.3 Potential Improvements

| Area | Suggestion |
|---|---|
| GA Performance | Increase `ga.population-size` and `ga.max-generations` for better results |
| Instructor Availability | Add `/api/instructors/{id}/availability` CRUD endpoint |
| Conflict Resolution | Auto-resolve minor conflicts before generation |
| Caching | Add Redis for schedule caching |
| CI/CD | Add GitHub Actions for build + test |
| Docker | Add `Dockerfile` + `docker-compose.yml` |
| Email Notify | Notify instructors when schedule is published |
| Batch Import | CSV/Excel import for sections, students, enrollments |
| Real-time Progress | WebSocket for generation job progress |

---

## 11. Configuration Reference

### 11.1 application.properties

```properties
# Server
server.port=8081

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/timetable_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=dev_user
spring.datasource.password=Dev@2026#
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false

# JWT
jwt.secret=${JWT_SECRET:R4nd0mVeryLongSecretKeyForHS256Algorithm1234567890}
jwt.expiration=900000

# GA
ga.tournament-size=3
ga.population-size=20
ga.max-generations=20
ga.crossover-rate=0.8
ga.mutation-rate=0.1
ga.elitism-count=2
ga.early-stop-threshold=0.90
ga.max-execution-millis=2000

# Logging
logging.level.root=WARN
logging.level.org.springframework=WARN
logging.level.org.hibernate=WARN
```

### 11.2 GA Tuning Guide

| Scenario | Population | Generations | Crossover | Mutation |
|---|---|---|---|---|
| Quick test | 20 | 20 | 0.8 | 0.1 |
| Large load (500+ sections) | 500 | 1000 | 0.85 | 0.15 |
| Quality focus | 200 | 500 | 0.9 | 0.05 |
| Speed focus | 50 | 100 | 0.8 | 0.1 |

### 11.3 Default Seed Data

| Entity | Values |
|---|---|
| Admin User | admin@uni.com / admin123 |
| Departments | Computer Science, Engineering Mathematics |
| Courses | CS101 (Programming), MATH201 (Calculus), etc. |
| Rooms | A101 (50), A102 (40), B201 (30), etc. |
| Instructors | Dr. Ahmed, Dr. Sara, etc. |
| Time Slots | SAT–THU × 8:00–10:00, 10:00–12:00, 12:00–14:00, 14:00–16:00 |

---

## 12. Development Guide

### 12.1 Prerequisites

- JDK 21+
- MySQL 8+
- Maven 3.8+ (or use bundled from IntelliJ)

### 12.2 Quick Start

```bash
# Clone
git clone https://github.com/MahmoudYoussef-web/Time-Table.git
cd Time-Table

# Create database
mysql -u root -p -e "CREATE DATABASE timetable_db;"

# Build & run
mvn clean install
mvn spring-boot:run

# Open Swagger
# http://localhost:8081/swagger-ui/index.html
```

### 12.3 Build Commands

```bash
mvn compile          # Compile only
mvn test             # Run tests
mvn clean install    # Full build (skip tests: -DskipTests)
mvn spring-boot:run  # Run application
```

### 12.4 Adding a New Soft Constraint

1. Create class in `constraints.soft/` implementing `SoftConstraint`
2. Add `@Component`
3. Implement `name()`, `weight()`, `violations(Chromosome)`
4. Spring auto-injects it into `FitnessCalculator`

### 12.5 Adding a New Hard Constraint

1. Create class in `constraints/hard/` implementing `HardConstraint`
2. Add `@Component`
3. Implement `getName()`, `violations(Chromosome)`, optionally `explain()`
4. Spring auto-injects it into `FitnessCalculator`

### 12.6 Testing the PDF

```bash
# Generate schedule
curl -X POST http://localhost:8081/api/schedules/generate/1 \
  -H "Authorization: Bearer <token>"

# Get schedule ID from job status, then:
curl -X GET http://localhost:8081/api/schedules/{id}/pdf \
  -H "Authorization: Bearer <token>" \
  --output schedule.pdf
```

### 12.7 Code Style

- Static mapper methods (no DI for mappers)
- Constructor injection via `@RequiredArgsConstructor`
- Records for DTOs (immutable by default)
- Interface-first service design
- Jakarta Validation on request DTOs
- @PreAuthorize for role-based access

---

## 📝 Changelog

### v2 (Current)
- Removed conflicting `constraints.SoftConstraint` interface
- Removed `PasswordConfig.java` (duplicate bean)
- Removed `SoftConstraintConfig.java` (auto-injection instead)
- Added `determineSessionType()` in genetic service
- Redesigned PDF with professional color scheme, summary page, legend, page numbers
- Improved Excel with multi-sheet, freeze pane, color coding
- Added session type display in PDF/Legend
- Fixed "Dr." prefix issue in instructor names
- Added semester name/dates to ScheduleDTO

### v1
- Initial GA implementation
- CRUD for all academic entities
- Basic PDF and Excel export
- JWT authentication

---

<div align="center">
  <sub>Timetable Scheduler — Graduation Project 2026</sub>
  <br/>
  <sub>Mahmoud Youssef · El Shorouk Academy</sub>
</div>
