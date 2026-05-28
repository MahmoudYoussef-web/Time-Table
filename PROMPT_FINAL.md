# Time Table Scheduler — Comprehensive Improvement Prompt

## Instructions
Apply these phases **in order**. After each phase, compile (`mvn compile`) and run (`mvn spring-boot:run`). Do NOT skip phases.

---

## Phase 1: Fix Critical Bugs (Prevent Startup)

### 1.1 Delete Duplicate PasswordConfig
Delete file `PasswordConfig.java` entirely. It defines a duplicate `PasswordEncoder` bean that conflicts with `SecurityConfig`.

### 1.2 Fix SoftConstraint Interface Conflict
There are TWO `SoftConstraint` interfaces:
- `scheduling/constraints/SoftConstraint.java` (ROOT — has `getName()`)
- `scheduling/constraints/soft/SoftConstraint.java` (SOFT — has `name()`, `weight()`, `violations()`)

Delete `scheduling/constraints/SoftConstraint.java`.  
Make ALL soft constraints implement `scheduling/constraints/soft/SoftConstraint`:
- `InstructorBackToBackConstraint`
- `InstructorGapPreferenceConstraint`
- `SameCourseSameDayConstraint`

Each needs these methods:
```java
@Override public String name() { return "CONSTRAINT_NAME"; }
@Override public double weight() { return 1.0; }
@Override public int violations(Chromosome chromosome) { ... }
```

Add `@Component` to `InstructorIdleSoftConstraint` and `StudentIdleSoftConstraint`.

Delete `SoftConstraintConfig.java` — Spring auto-injects via `List<SoftConstraint>` in `FitnessCalculator`.

### 1.3 Fix InstructorMapper Password Encoding
`InstructorMapper` calls `user.setPassword("123456")` hardcoded. Fix:
1. Add `passwordEncoder` parameter to `toEntity()` and `updateEntity()` methods
2. Add `@NotBlank @Size(min=8)` `password` field to `InstructorRequest`
3. Encode: `user.setPassword(passwordEncoder.encode(request.password()))`
4. Inject `PasswordEncoder` into `InstructorController` and pass to mapper

### 1.4 Fix DataLoader (Duplicate Key + Detached Entity)
`DataLoader` crashes on second run (duplicate department key + detached entity cascade). Fix completely:

```java
// Use find-or-create for every entity

Department cs = departmentRepository.findByCode("CS").orElseGet(() -> {
    Department d = new Department();
    d.setCode("CS");
    d.setName("Computer Science");
    return departmentRepository.save(d);
});

User u1 = userRepository.findByEmail("ahmed@uni.edu").orElseGet(() -> {
    User u = new User();
    u.setFullName("Dr. Ahmed");
    u.setEmail("ahmed@uni.edu");
    u.setPassword(passwordEncoder.encode("123456"));
    u.setRole(UserRole.INSTRUCTOR);
    u.setEnabled(true);
    return userRepository.save(u);
});

// Same pattern for ALL entities: Instructor, Course, Room, TimeSlot, Semester, Section
```

Also change `Instructor.java` cascade on `user` field from `CascadeType.ALL` to:
```java
@OneToOne(cascade = {CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH, CascadeType.DETACH})
```
(This prevents `detached entity passed to persist` error when instructor already exists.)

Add missing repository finder methods:
- `RoomRepository`: `findByBuildingAndRoomNumber(String building, String roomNumber)`
- `TimeSlotRepository`: `findByDayAndStartTimeAndEndTime(DayOfWeek day, LocalTime startTime, LocalTime endTime)`
- `SectionRepository`: `findByNameAndCourseIdAndSemesterId(String name, Long courseId, Long semesterId)`

### 1.5 Fix GlobalExceptionHandler
Add these handlers:
```java
@ExceptionHandler(NoSuchElementException.class)
public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(Map.of("error", e.getMessage()));
}

@ExceptionHandler(IllegalStateException.class)
public ResponseEntity<Map<String, String>> handleBadState(IllegalStateException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("error", e.getMessage()));
}
```

### 1.6 Fix InstructorAvailabilityConstraint Thread Safety
Change the `Set<Long>` from `HashSet` to `ConcurrentHashMap.newKeySet()`:
```java
private final Set<Long> unavailableSlotIds = ConcurrentHashMap.newKeySet();
```

In `GeneticScheduleService.generate()`, call:
```java
instructorAvailabilityConstraint.preload(instructors);
```
*before* `geneticAlgorithm.evolve(sections, rooms, slots)`.

### 1.7 Remove Duplicate MySQL Dependency
In `pom.xml`, keep only ONE `mysql-connector-j` dependency (the one WITHOUT `<scope>runtime</scope>`).

---

## Phase 2: Security Hardening

### 2.1 application.properties
Change:
```properties
server.error.include-stacktrace=never
server.error.include-exception=false
logging.level.com.example=WARN
logging.level.org.springframework=WARN
logging.level.org.hibernate=WARN
jwt.secret=${JWT_SECRET:MySuperSecretKeyForJWTTokenGeneration2024Minimum32Chars!!}
```

### 2.2 Remove H2 Console
In `SecurityConfig.java`, delete `"/h2-console/**"` from `permitAll()`.

### 2.3 Create application-prod.properties
Create `src/main/resources/application-prod.properties`:
```properties
server.error.include-stacktrace=never
server.error.include-exception=false
springdoc.api-docs.enabled=false
springdoc.swagger-ui.enabled=false
jwt.secret=${JWT_SECRET}
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

### 2.4 Add PUT Endpoints
Add missing `@PutMapping("/{id}")` endpoints to:
- `CourseController`
- `RoomController`
- `InstructorController`
- `SemesterController`
- `SectionController`
- `TimeSlotController`

Each should call a service method that finds the existing entity, updates fields from request, and saves.

### 2.5 Create DepartmentController (Full CRUD)
Create:
- `DepartmentController` (GET all, GET by id, POST, PUT, DELETE)
- `DepartmentRequest` DTO (with `@NotBlank` on `code` and `name`)
- `DepartmentResponse` DTO
- `DepartmentMapper`

---

## Phase 3: Performance Fixes

### 3.1 Fix N+1 Query in ScheduleMapper
In `ScheduleRepository`, add:
```java
@Query("SELECT s FROM Schedule s LEFT JOIN FETCH s.entries e " +
       "LEFT JOIN FETCH e.section sec " +
       "LEFT JOIN FETCH sec.course c " +
       "LEFT JOIN FETCH sec.instructor i " +
       "LEFT JOIN FETCH i.user " +
       "LEFT JOIN FETCH e.room r " +
       "LEFT JOIN FETCH e.timeSlot t " +
       "WHERE s.id = :scheduleId")
Optional<Schedule> findByIdWithDetails(@Param("scheduleId") Long scheduleId);
```

In `ScheduleEntryRepository`, add:
```java
@Query("SELECT e FROM ScheduleEntry e " +
       "LEFT JOIN FETCH e.section sec " +
       "LEFT JOIN FETCH sec.course c " +
       "LEFT JOIN FETCH sec.instructor i " +
       "LEFT JOIN FETCH i.user " +
       "LEFT JOIN FETCH e.room r " +
       "LEFT JOIN FETCH e.timeSlot t " +
       "WHERE e.schedule.id = :scheduleId AND e.instructor.id = :instructorId")
List<ScheduleEntry> findByScheduleIdAndInstructorIdWithDetails(
    @Param("scheduleId") Long scheduleId,
    @Param("instructorId") Long instructorId);
```

### 3.2 Fix ScheduleServiceImpl
Replace `e.printStackTrace()` with `log.error("message", e)`.
Add meaningful messages to `orElseThrow()` calls.

### 3.3 Duplicate Email Check
In `InstructorServiceImpl.save()`, add before saving:
```java
if (userRepository.existsByEmail(instructor.getUser().getEmail())) {
    throw new IllegalArgumentException("Email already in use");
}
```

---

## Phase 4: Enums + YearLevel

### 4.1 Create SessionType Enum
```java
public enum SessionType {
    LECTURE, LAB, TUTORIAL, SEMINAR
}
```

### 4.2 Change ScheduleEntry.type
Change `private String type` to `private SessionType type` with `@Enumerated(EnumType.STRING)`.

### 4.3 Create YearLevel Enum
```java
public enum YearLevel {
    FIRST, SECOND, THIRD, FOURTH
}
```

### 4.4 Add yearLevel to Section
Add field to `Section` entity:
```java
@Enumerated(EnumType.STRING)
@Column(nullable = false, length = 20)
private YearLevel yearLevel;
```
Add to `SectionRequest`, `SectionResponse`, `SectionMapper`.

---

## Phase 5: Premium PDF Redesign (Core Feature)

### 5.1 Full SchedulePdfService Rewrite
Write a new `SchedulePdfService.java` that produces an A4 Landscape PDF. **One page per year level** (First Year → Fourth Year).

**Required structure for each page:**

**Header section:**
```
DEPARTMENT WEEKLY SCHEDULE          ← 20pt Bold, Navy (#1B4F72), centered
First Year — Semester: Spring 2026   ← 14pt Bold Red (#C0392B), centered
[Feb 1, 2026] — [Jun 1, 2026]       ← 10pt Gray
───────────────────────────────────── ← thin horizontal rule
```

**Table layout (7 columns):**
```
| TIME  | SAT | SUN | MON | TUE | WED | THU |
|-------|-----|-----|-----|-----|-----|-----|
| 8:00  |     |     |     |     |     |     |
| 9:00  |     |     |     |     |     |     |
| ...   |     |     |     |     |     |     |
| BREAK | BREAK | BREAK | BREAK | BREAK | BREAK | BREAK |
| 1:00  |     |     |     |     |     |     |
```

**Time format:** `8:00 AM`, `9:00 AM`, ... `6:00 PM` (use `DateTimeFormatter.ofPattern("h:mm a")`)

**Color scheme:**
```java
Color HEADER_BG  = new Color(26, 58, 108);  // Navy
Color HEADER_FG  = Color.WHITE;
Color LECTURE_BG = new Color(210, 228, 250); // Light blue
Color LECTURE_FG = new Color(26, 58, 108);   // Navy text
Color LAB_BG     = new Color(208, 240, 192);  // Light green
Color LAB_FG     = new Color(30, 100, 30);    // Dark green
Color BREAK_BG   = new Color(253, 235, 208);  // Beige
Color BREAK_FG   = new Color(180, 80, 20);    // Brown
Color ZEBRA_BG   = new Color(248, 249, 250);  // Light gray
```

**Cell content (Lecture):**
```
CS301 - Data Structures     ← Bold 9pt, Navy
Dr. Ahmed Hassan            ← Normal 8pt
Hall: B-12                  ← Italic 8pt
```

**Cell content (Lab):**
```
CS301 - Data Structures     ← Bold 9pt, Dark Green
Dr. Ahmed Hassan            ← Normal 8pt
Lab: B-12                   ← Italic 8pt
```

**BREAK row:** After 12:00 PM slot, insert a row spanning all columns with "BREAK" in beige background.

**Legend at bottom:**
```
■ Lecture (Main Hall)    ■ Section / Lab    ■ Break
```
Each box in its respective color. Use a small `PdfPTable` at 65% width, left-aligned.

**Footer on every page (PdfPageEventHelper):**
```
Generated by TimetableScheduler — 28 May 2026    ← 8pt gray italic, centered
```

### 5.2 Update ScheduleEntryDTO
Add `sectionType` field to record:
```java
String sectionType   // "LECTURE" or "LAB"
```

### 5.3 Update WeeklyScheduleMapper.toLevelTables()
Group entries by `yearLevel` (FIRST, SECOND, THIRD, FOURTH) → outputs:
```
"First Year"  → WeeklyScheduleDTO
"Second Year" → WeeklyScheduleDTO
"Third Year"  → WeeklyScheduleDTO
"Fourth Year" → WeeklyScheduleDTO
```

### 5.4 Controller Endpoint
`GET /api/schedules/{id}/pdf` returns PDF download with header:
```
Content-Disposition: attachment; filename="schedule-{id}.pdf"
Content-Type: application/pdf
```

---

## Phase 6: GA Algorithm Improvements

### 6.1 Enable Mutation Strategies
`RandomMutation` only mutates time (70%) or room (30%). Add:
```java
// 30% chance: swap BOTH room AND time
if (random.nextDouble() < 0.3) {
    gene.setTimeSlot(slots.get(random.nextInt(slots.size())));
    gene.setRoom(rooms.get(random.nextInt(rooms.size())));
}
```

### 6.2 Fix TournamentSelection Dependency
`SinglePointCrossover` and `RandomMutation` both take `Random` in constructor. In `GeneticAlgorithmConfig`:
```java
@Bean
public Random random(GAProperties props) {
    return new Random(props.getRandomSeed());
}

@Bean
public SelectionStrategy selectionStrategy(Random random) {
    return new TournamentSelection(random, 3);
}

@Bean
public CrossoverStrategy crossoverStrategy(Random random) {
    return new SinglePointCrossover(random);
}

@Bean
public MutationStrategy mutationStrategy(Random random) {
    return new RandomMutation(random);
}
```

---

## Phase 7: CRUD Completeness + Validation

### 7.1 Add @Valid to All Controllers
Ensure `@Valid` is on request bodies of:
- `SemesterController.create()`
- `TimeSlotController.create()`
- All PUT endpoints

### 7.2 Add Validation Annotations
**SemesterRequest:**
- `@NotBlank` on `name`
- `@NotNull` on `startDate`, `endDate`

**TimeSlotRequest:**
- `@NotNull` on `day`, `startTime`, `endTime`

**InstructorRequest:**
- `@NotBlank @Email` on `email`
- `@NotBlank @Size(min=8)` on `password`
- `@NotBlank` on `fullName` (or add this field)
- `@NotNull` on `departmentId`

**SectionRequest:**
- Add `@NotNull` for `yearLevel`

### 7.3 Fix SectionRepository
Change `findBySemester_Id` to `findBySemesterId` (Spring Data JPA convention).

---

## Phase 8: Unit Tests

### 8.1 SoftConstraintInjectionTest
Pure unit test (no `@SpringBootTest`):
- Manually instantiate all 5 soft constraints + 5 hard constraints
- Assert `softConstraints.size() == 5`
- Assert `hardConstraints.size() == 5`
- Assert each soft constraint has `weight() > 0` and non-blank `name()`

### 8.2 GlobalExceptionHandlerTest
Unit test using MockMvc or direct handler calls:
- `NoSuchElementException` → 404
- `IllegalStateException` → 400

---

## Phase 9: Before/After Data Migration

### 9.1 SessionType Migration
The DB column `schedule_entries.type` contains old String values. To migrate:
```sql
UPDATE schedule_entries SET type = 'LECTURE' WHERE type NOT IN ('LECTURE','LAB','TUTORIAL','SEMINAR');
```

### 9.2 yearLevel Default
New `year_level` column in `sections` table. Set default:
```sql
UPDATE sections SET year_level = 'FIRST' WHERE year_level IS NULL;
```

---

## Summary Checklist

| Phase | Items | Status |
|-------|-------|--------|
| 1 | 7 critical bug fixes | ❌ |
| 2 | Security hardening (5 items) | ❌ |
| 3 | Performance N+1 + logging (3 items) | ❌ |
| 4 | Enums + yearLevel (4 items) | ❌ |
| 5 | Premium PDF redesign (full service) | ❌ |
| 6 | GA algorithm improvements (2 items) | ❌ |
| 7 | CRUD completeness (3 items) | ❌ |
| 8 | Unit tests (2 test classes) | ❌ |
| 9 | Data migration (2 SQL scripts) | ❌ |

Verify after all phases:
- `mvn compile` — 0 errors
- `mvn test` — all tests pass
- `mvn spring-boot:run` — app starts on port 8080, seed data loads, Swagger at `/swagger-ui.html`
- `GET /api/schedules/{id}/pdf` returns a professional multi-page PDF
