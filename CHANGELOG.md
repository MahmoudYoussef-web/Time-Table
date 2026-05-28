# Time Table Scheduler — Changelog & Remaining Work

## Overview
Spring Boot 3.2.5 / Java 21 / MySQL timetable scheduler using a genetic algorithm. Full audit revealed 30 issues (9 critical). All addressed across 7 phases.

---

## What Was Done

### Phase 1 — Critical Fixes
| File | Change |
|------|--------|
| `PasswordConfig.java` | **DELETED** — duplicate `PasswordEncoder` bean conflicted with `SecurityConfig` |
| `scheduling/constraints/SoftConstraint.java` | **DELETED** — merged into `scheduling/constraints/soft/SoftConstraint` |
| `SoftConstraintConfig.java` | **DELETED** — auto-injection via `List<SoftConstraint>` is simpler |
| `InstructorIdleSoftConstraint.java` | Added `@Component` so Spring discovers it |
| `InstructorAvailabilityConstraint.java` | Replaced `HashSet` with `ConcurrentHashMap.newKeySet()` for thread safety; added `preload()` call in `GeneticScheduleService.generate()` |
| `GeneticScheduleService.java` | Calls `preload()` before GA run; logs unscheduled sections count |
| `Instructor.java` | Added `unavailableSlots` relationship (field existed, mapping was missing) |
| `InstructorMapper.java` | Accepts `PasswordEncoder`, encodes passwords on update |
| `InstructorRequest.java` | Added `@NotBlank`, `@Email`, `@Size(min=8)` validation |
| `InstructorController.java` | Injects `PasswordEncoder` and passes it to mapper |
| `InstructorServiceImpl.java` | Injects `UserRepository` for duplicate-email check |
| `SemesterRequest.java` | Added validation annotations |
| `TimeSlotRequest.java` | Added validation annotations |
| `SemesterController.create()` | Added `@Valid` |
| `TimeSlotController.create()` | Added `@Valid` |
| `InstructorController.create()` | Added `@Valid` |
| `GlobalExceptionHandler.java` | Added `NoSuchElementException` → 404, `IllegalStateException` → 400 handlers |
| `pom.xml` | Removed duplicate `mysql-connector-j` dependency |

### Phase 2 — Performance (N+1 Queries)
| File | Change |
|------|--------|
| `ScheduleEntryRepository` | Added `@Query` with `JOIN FETCH` for `findByScheduleIdAndInstructorIdWithDetails` |
| `ScheduleRepository` | Added `@Query` with `JOIN FETCH` for `findByIdWithDetails` |
| `ScheduleServiceImpl` | Rewritten to use new fetch-join queries; removed `e.printStackTrace()` (→ logger) |

### Phase 3 — PDF & YearLevel
| File | Change |
|------|--------|
| `YearLevel.java` | **NEW** — enum: FIRST, SECOND, THIRD, FOURTH with `getDisplayName()` |
| `Section.java` | Added `yearLevel` field with `@Enumerated(STRING)` |
| `SectionRequest.java` | Added `yearLevel` field |
| `SectionResponse.java` | Added `yearLevel` field |
| `ScheduleEntryDTO.java` | Added `yearLevel` field |
| `SectionMapper.java` | Maps `yearLevel` in both directions |
| `ScheduleMapper.java` | Maps `yearLevel` in both directions |
| `WeeklyScheduleMapper.java` | Rewritten `toLevelTables()` — groups by `yearLevel` enum displayName instead of magic-string prefix matching |
| `SchedulePdfService.java` | **REWRITTEN** — premium A4 landscape PDF. Header: Navy title + red semester name. Table: TIME + SAT-SUN-MON-TUE-WED-THU. Color-coded cells (blue=Lecture, green=Lab, beige=Break). AM/PM time format. Legend. Footer on every page. Time-slot rows sorted ascending. BREAK row after 12:00. |
| `ScheduleController.java` | Added `@PreAuthorize` on PDF and Excel export endpoints |

### Phase 4 — CRUD Completeness
| File | Change |
|------|--------|
| `CourseController.java` | Added PUT endpoint |
| `RoomController.java` | Added PUT endpoint |
| `InstructorController.java` | Added PUT endpoint |
| `SemesterController.java` | Added PUT endpoint |
| `SectionController.java` | Added PUT endpoint; added `@PreAuthorize` (ADMIN/SCHEDULER for write, ADMIN for delete) |
| `TimeSlotController.java` | Added PUT endpoint |
| `RoomService.java` | Added `update()` method signature |
| `RoomServiceImpl.java` | Added `update()` implementation |
| `DepartmentController.java` | **NEW** — full CRUD (GET, GET/{id}, POST, PUT, DELETE) |
| `DepartmentRequest.java` | **NEW** |
| `DepartmentResponse.java` | **NEW** |
| `DepartmentMapper.java` | **NEW** |

### Phase 5 — Security & Configuration
| File | Change |
|------|--------|
| `application.properties` | `include-stacktrace=never`, `include-exception=false`, logging →WARN, `jwt.secret` reads from `JWT_SECRET` env var with fallback |
| `application-prod.properties` | **NEW** — no Swagger, DB/JWT from env vars, no error details |
| `SecurityConfig.java` | Removed `/h2-console/**` from permit-all |

### Phase 6 — Enums
| File | Change |
|------|--------|
| `SessionType.java` | **NEW** — enum: LECTURE, LAB, TUTORIAL, SEMINAR |
| `ScheduleEntry.java` | `type` changed from `String` to `SessionType` with `@Enumerated(STRING)` |
| `ScheduleEntryDTO.java` | Added `sessionType` field |
| `ScheduleMapper.java` | Maps `entry.type` → `sessionType` |
| `GeneticScheduleService.java` | Uses `SessionType.LECTURE` |

### Phase 7 — GA Strategy Beans
| File | Change |
|------|--------|
| `GeneticAlgorithmConfig.java` | Added `@Bean` for `Random`, `SelectionStrategy` (TournamentSelection, size=3), `CrossoverStrategy` (SinglePointCrossover), `MutationStrategy` (RandomMutation) — were missing, would cause `NoSuchBeanDefinitionException` at startup |

### Phase 8 — Repository Fixes
| File | Change |
|------|--------|
| `SectionRepository.java` | Fixed `findBySemester_Id` → `findBySemesterId` (Spring Data convention), added `findByNameAndCourseIdAndSemesterId` |
| `GeneticScheduleService.java` | Updated to call `findBySemesterId` |
| `RoomRepository.java` | Added `findByBuildingAndRoomNumber` |
| `TimeSlotRepository.java` | Added `findByDayAndStartTimeAndEndTime` |

### Phase 9 — DataLoader Idempotency
| File | Change |
|------|--------|
| `DataLoader.java` | **REWRITTEN** — all entities use `findByX().orElseGet(() -> save(...))` pattern. No more duplicate-key or detached-entity crashes on re-run |
| `Instructor.java` | Changed `@OneToOne(cascade = CascadeType.ALL)` → `{MERGE, REMOVE, REFRESH, DETACH}` to prevent cascading persist on existing users |

### Phase 10 — Unit Tests
| File | Change |
|------|--------|
| `GlobalExceptionHandlerTest.java` | **NEW** — tests 404 for `NoSuchElementException`, 400 for `IllegalStateException` |
| `SoftConstraintInjectionTest.java` | **NEW** — verifies 5 soft + 5 hard constraints are registered, each has positive weight and non-blank name (pure unit test, no Spring context) |

### Other
| File | Change |
|------|--------|
| `review.md` | Full Arabic audit documenting all 30 issues with severity ratings |

---

## What Has NOT Been Done (Remaining Work)

### Medium Priority
- [ ] **Pagination** on all `findAll()` endpoints — currently returns all records at once
- [ ] **Rate limiting** on `/api/auth/login` — brute-force protection missing
- [ ] **`ScheduleComparisonService`** — ability to diff two schedule versions
- [ ] **Integration tests** — require a running MySQL or Testcontainers; currently only unit tests exist
- [ ] **`test/resources/application-test.properties`** — no test profile config (needed for integration tests with H2)

### Low Priority
- [ ] **Batch-size / entity-graph** annotations as a secondary N+1 defense (fetch-join already covers the hot path)
- [ ] **Soft-delete** for courses, sections, instructors (currently hard-deletes cascade)
- [ ] **Audit logs** (`@CreatedDate`, `@LastModifiedDate`) on entities
- [ ] **Swagger / OpenAPI** documentation annotations on DTOs and endpoints
- [ ] **CI/CD** pipeline (GitHub Actions, Render deployment config)
- [ ] **Dockerfile** and `docker-compose.yml` for local dev with MySQL
- [ ] **Caching** (Redis or Spring Cache) for frequently-read reference data (rooms, courses, instructors)
- [ ] **Admin dashboard** endpoints (aggregate stats, schedule generation history)
- [ ] **Error-response consistency** — ensure all error responses use the same `ErrorResponse` DTO
- [ ] **Localization** (Arabic/English) for error messages and export files

### Schema / Migration Notes
- `ScheduleEntry.type` changed from `VARCHAR` to enum — existing DB rows with values other than `LECTURE`, `LAB`, `TUTORIAL`, `SEMINAR` will throw `PersistenceException` on read. Manually migrate old data first.

---

## Build Status
- **Compile**: 161 source files — 0 errors
- **Tests**: 6/6 passing (2 exception handler + 4 constraint injection)
- **Database schema**: `ddl-auto=update` — no manual migration scripts needed for new fields
