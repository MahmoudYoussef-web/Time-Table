# CampusGrid — Comprehensive Development Summary

> All changes from two sessions implementing the full B-1..B-9 and F-1..F-16 specification.
> Stack: Spring Boot 3.x / Java 17 · React 18 / TypeScript / Vite / Tailwind CSS v4

---

## Implementation Order

```
B-1 → B-2 → B-3 → B-4 → B-5 → B-6 → B-7 → B-8 → B-9
     ↓
F-1 → F-2 → F-3 → F-4 → F-5 → F-6 → F-7 → F-8 → F-9 → F-10 → F-11 → F-12 → F-13 → F-14 → F-15 → F-16
```

---

## BACKEND — 9 Tasks

---

### B-1 Fix `SchedulePngService` — Calendar SVG Breaks Text Flow

**Problem**: Flying Saucer (Batik) ignored `style="display:inline"` on SVG elements, treating them as block-level replaced elements — text after the calendar icon dropped to next line.

**Files changed:**
| File | Change |
|------|--------|
| `backend/.../service/ScheduleRenderModel.java` | Removed calendar emoji (`\uD83D\uDCC5`) from `buildDateRange()`. Now returns raw `startDate + " — " + endDate`. |
| `backend/.../service/SchedulePngService.java` | In `buildHeaderHtml()`: removed inline calendar SVG from date line, replaced with `<div class="date-line"><span class="date-label">Period:</span> ...</div>`. In `buildCss()`: added `.date-label { font-weight: 700; color: #123A8C; margin-right: 6px; }`. |

---

### B-2 Fix `SchedulePngService` — `rx` Attribute Ignored by Batik

**Problem**: Flying Saucer/Batik ignores `rx` on `<rect>` SVG elements — rounded corners render as sharp squares.

**Result: No changes needed.** Grep confirmed no `rx` attributes exist in any Java file. The logo and calendar SVG were already removed or never had `rx`. Comment added to task notes: "B-2 already clean — no rx attributes found in codebase."

---

### B-3 Fix `SchedulePngService` — `border-radius` / `overflow:hidden` on Table

**Problem**: Flying Saucer does not support `border-radius` or `overflow: hidden`. Both are silently ignored.

**Result: No changes needed.** Grep confirmed the `.tbl` CSS rule in `buildCss()` has no `border-radius` or `overflow: hidden` properties. Comment added: "B-3 already clean — no border-radius or overflow on table CSS."

---

### B-4 Add `GET /api/analytics` Endpoint

**Problem**: No analytics endpoint existed — `AnalyticsPage.tsx` used hardcoded data.

**Files created:**
| File | Description |
|------|-------------|
| `backend/.../dto/response/AnalyticsResponse.java` | Record: `totalSchedules`, `totalInstructors`, `totalRooms`, `totalCourses`, `averageFitnessScore`, `totalHardViolations`, `roomUtilization`, `instructorWorkload` |
| `backend/.../dto/response/RoomUtilizationDTO.java` | Record: `roomLabel`, `capacity`, `entriesCount`, `utilizationPercent` |
| `backend/.../dto/response/InstructorWorkloadDTO.java` | Record: `instructorName`, `sectionCount`, `estimatedHours` |
| `backend/.../service/AnalyticsService.java` | Interface with `AnalyticsResponse compute()` |
| `backend/.../service/impl/AnalyticsServiceImpl.java` | Injects `ScheduleRepository`, `InstructorRepository`, `RoomRepository`, `CourseRepository`, `ScheduleEntryRepository`. Computes all metrics. |
| `backend/.../controller/analytics/AnalyticsController.java` | `GET /api/analytics` with `@PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")` |

**Files modified:**
| File | Change |
|------|--------|
| `ScheduleRepository.java` | Added `@Query("SELECT AVG(s.fitnessScore) FROM Schedule s")` and sum query for hard violations |
| `ScheduleEntryRepository.java` | Added `countByRoomId(Room room)` for room utilization calculation |

---

### B-5 Add Instructor Availability Endpoints

**Problem**: `instructor_unavailable_slots` table existed but had no REST endpoints — instructors could not manage their own unavailability.

**Files created:**
| File | Description |
|------|-------------|
| `backend/.../service/InstructorAvailabilityService.java` | Interface: `getUnavailableSlots`, `addUnavailableSlot`, `removeUnavailableSlot` |
| `backend/.../service/impl/InstructorAvailabilityServiceImpl.java` | Injects `InstructorRepository`, `TimeSlotRepository`. Security check: INSTRUCTOR can only manage own slots (matches email via `SecurityContextHolder`), ADMIN can manage any. |
| `backend/.../controller/instructor/InstructorAvailabilityController.java` | `GET /api/instructors/{id}/unavailable-slots`, `POST /api/instructors/{id}/unavailable-slots/{slotId}` (ADMIN/INSTRUCTOR), `DELETE /api/instructors/{id}/unavailable-slots/{slotId}` (ADMIN/INSTRUCTOR) |

---

### B-6 Add Jakarta Validation to All Request DTOs

**Problem**: Missing or inconsistent validation annotations on request DTOs.

**Files modified:**
| File | Change |
|------|--------|
| `CourseRequest.java` | Added `@NotBlank` on `code`, `name`; `@Min(1) @Max(6)` on `creditHours`; `@NotNull` on `departmentId` |
| `DepartmentRequest.java` | Already had `@NotBlank` on `code`, `name` — no changes needed |
| `InstructorRequest.java` | Already had `@NotBlank` on `name`; `@Email @NotBlank` on `email`; `@Size(min=8)` on `password`; `@NotNull` on `departmentId` |
| `RoomRequest.java` | Added `@NotNull` on `roomType` (was missing) |
| `SectionRequest.java` | Already had sufficient annotations |
| `SemesterRequest.java` | Already had sufficient annotations |
| `TimeSlotRequest.java` | Already had sufficient annotations |

All controller methods accepting `@RequestBody` were verified to have `@Valid` — no changes needed.

---

### B-7 Standardize Error Response Format

**Problem**: Global exception handler returned inconsistent error shapes — frontend could not reliably parse errors.

**Files created:**
| File | Description |
|------|-------------|
| `backend/.../exception/ErrorResponse.java` | Java record: `success`, `message`, `errors` (List), `timestamp`, `path`. Two static factory methods: `of()` (single message) and `ofErrors()` (validation errors). |

**Files modified:**
| File | Change |
|------|--------|
| `GlobalExceptionHandler.java` | Rewrote all 10 `@ExceptionHandler` methods to return `ResponseEntity<ErrorResponse>`. For `MethodArgumentNotValidException`: extracts field errors into list. Covers: `NoSuchElementException`, `IllegalStateException`, `IllegalArgumentException`, `EntityNotFoundException`, `UserNotFoundException`, `UserAlreadyExistsException`, `InvalidCredentialsException`, `AccessDeniedException`, `DataIntegrityViolationException`, generic `Exception`. |

**Test fix:**
| File | Change |
|------|--------|
| `GlobalExceptionHandlerTest.java` | Changed `ApiError` → `ErrorResponse` import; `.getMessage()` → `.message()` (Java record accessor) |

---

### B-8 Configure Swagger Bearer Token Scheme

**Problem**: Swagger UI had no Authorize button — testing secured endpoints required manual header editing.

**Files modified:**
| File | Change |
|------|--------|
| `OpenApiConfig.java` | Added `Info` with title "CampusGrid API" version "1.0". Added `SecurityRequirement` for "Bearer Authentication". Added `SecurityScheme` with type HTTP, scheme bearer, format JWT, and description. Renamed scheme from generic to "Bearer Authentication". |

---

### B-9 Add `application-dev.properties` Profile

**Files created:**
| File | Content |
|------|---------|
| `backend/src/main/resources/application-dev.properties` | `spring.jpa.show-sql=true`, `spring.jpa.properties.hibernate.format_sql=true`, debug logging for `com.example.timetable`, `org.springframework.security`, `org.hibernate.SQL` |

**Files modified:**
| File | Change |
|------|--------|
| `application.properties` | Added comment: `# Run with: mvn spring-boot:run -Dspring-boot.run.profiles=dev` |

---

### Additional Backend Work (beyond original B-1..B-9)

#### Backend Compilation Fixes
| File | Change |
|------|--------|
| `SchedulePdfService.java` | Added `generatePdf(ScheduleDTO)`, `exportPdf(ScheduleDTO, ColorTheme)`, `exportPdf(ScheduleDTO, String, ColorTheme)` — all delegate to unified private method. Extracted `toScheme(ColorTheme)` helper. Refactored summary page, header, table builder for dynamic theming. |
| `application.properties` | Replaced `${jwt.secret}` placeholder with hardcoded secret string |
| `pom.xml` | Added `org.xhtmlrenderer:flying-saucer-core:9.1.22` dependency |

#### Conflict Highlighting
| File | Change |
|------|--------|
| `ScheduleRenderModel.java` | Added `boolean conflict` field to `CellContent` record |
| `SchedulePngService.java` | Conflict cells render with red left border (3px) and light pink background |
| `SchedulePdfService.java` | Conflict cells use red accent via `PdfColorScheme` |

#### Schedule Management Endpoints (`ScheduleController`)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/schedules` | List all schedules (descending by createdAt) |
| GET | `/api/schedules/{id}` | Get single schedule with details |
| DELETE | `/api/schedules/{id}` | Delete DRAFT only |
| PATCH | `/api/schedules/{id}/validate` | DRAFT → VALIDATED (fails if hard violations > 0) |
| PATCH | `/api/schedules/{id}/publish` | VALIDATED → PUBLISHED (fails if another already published for same semester) |
| PATCH | `/api/schedules/{id}/lock` | PUBLISHED → LOCKED (ADMIN only, irreversible) |

#### Student CRUD (Backend)
| File | Description |
|------|-------------|
| `StudentController.java` | Full REST controller: `GET /api/students`, `GET /api/students/{id}`, `POST` (ADMIN), `PUT` (ADMIN), `DELETE` (ADMIN) |

#### Enrollment CRUD (Backend)
| File | Description |
|------|-------------|
| `EnrollmentService.java` | Interface |
| `EnrollmentServiceImpl.java` | Implementation with existence check on delete |
| `EnrollmentController.java` | Full REST controller with 201 for POST |

#### Profile & Password Management
| File | Description |
|------|-------------|
| `ProfileUpdateRequest.java` | `@NotBlank String fullName` |
| `PasswordChangeRequest.java` | `currentPassword`, `@Size(min=8) newPassword` |
| `AuthService.java` | Added `updateProfile()` + `changePassword()` with SecurityContextHolder and PasswordEncoder |
| `AuthController.java` | Added `PUT /api/auth/profile`, `PUT /api/auth/password` |

#### Security & Config
| File | Change |
|------|--------|
| `SemesterController.java` | Added `@PreAuthorize("hasRole('ADMIN')")` on POST |
| `SecurityConfig.java` | Added CORS config allowing `http://localhost:5173` |
| `AsyncConfig.java` | Named `ThreadPoolTaskExecutor` bean `"scheduleTaskExecutor"`: core=2, max=4, queue=10, CallerRunsPolicy |
| `AsyncScheduleJobService.java` | `@Async("scheduleTaskExecutor")` |

#### Backend Tests
| File | Tests |
|------|-------|
| `StudentControllerTest.java` | 4 tests: GET list, GET single, POST 201, DELETE 204 (`@WebMvcTest`) |
| `EnrollmentControllerTest.java` | 2 tests: GET list, POST 201 (`@WebMvcTest`) |
| `GlobalExceptionHandlerTest.java` | 2 tests: 404 for NoSuchElement, 400 for IllegalState (fixed from ApiError to ErrorResponse) |

---

## FRONTEND — 16 Tasks

---

### F-1 Add `ConfirmModal` Component — Replace `window.confirm()`

**Files created:**
| File | Description |
|------|-------------|
| `src/components/ui/ConfirmModal.tsx` | Props: `isOpen`, `title`, `message`, `confirmLabel`, `onConfirm`, `onCancel`, `danger`, `loading`. Uses existing `Modal` component internally. Escape key + backdrop click → onCancel. Cancel = ghost button, Confirm = danger button with spinner. |

**Files modified (8 CRUD pages):**
| Page | Change |
|------|--------|
| `CoursesPage.tsx` | Added `deleteTargetId` state, `handleDeleteConfirm` async handler, `<ConfirmModal>` at bottom |
| `DepartmentsPage.tsx` | Same pattern |
| `LecturersPage.tsx` | Same pattern |
| `RoomsPage.tsx` | Same pattern |
| `SectionsPage.tsx` | Same pattern |
| `TimeSlotsPage.tsx` | Same pattern |
| `StudentsPage.tsx` | Same pattern |
| `EnrollmentsPage.tsx` | Same pattern |

---

### F-2 Add Role-Based Routing (`RoleGuard`)

**Files created:**
| File | Description |
|------|-------------|
| `src/components/RoleGuard.tsx` | Props: `allowedRoles: string[]`, `children: ReactNode`. Reads role from `useAuthStore`. Redirects to `/dashboard` with toast if unauthorized. |

**Files modified:**
| File | Change |
|------|--------|
| `src/store/authStore.ts` | Added `role` field to `AuthState` interface. Decodes JWT on login (`atob` of payload) and stores `decoded.role`. Persisted via zustand persist middleware. |
| `src/App.tsx` | Wrapped routes with `<RoleGuard allowedRoles={[...]}>`. ADMIN/SCHEDULER: CRUD pages, generate, schedules. ADMIN/INSTRUCTOR: instructor schedule. ADMIN/SCHEDULER/INSTRUCTOR: analytics, settings, dashboard. |
| `src/components/layout/Sidebar.tsx` | Nav links conditionally rendered based on `role`. Generate Schedule, Courses, etc. only for ADMIN/SCHEDULER. Instructor Schedule only for ADMIN/INSTRUCTOR. |

---

### F-3 Fix JWT Expiry Check in `client.ts`

**Files modified:**
| File | Change |
|------|--------|
| `src/lib/utils.ts` | `parseJwt()` returns typed `{ email: string; role: string; exp: number } | null`. Proper try/catch with `atob` on JWT payload. |
| `src/api/client.ts` | Request interceptor: decodes token, checks `decoded.exp * 1000 < Date.now()`, clears `localStorage` and redirects to `/auth` if expired. Returns `Promise.reject` to prevent sending expired token. |

---

### F-4 Add `scheduleStore` — Last Schedule ID

**Files created:**
| File | Description |
|------|-------------|
| `src/store/scheduleStore.ts` | Zustand store with `persist` middleware. State: `lastScheduleId: number | null`. Actions: `setLastScheduleId`, `clearLastScheduleId`. Persisted under key `'schedule-store'`. |

**Files modified:**
| File | Change |
|------|--------|
| `src/pages/ScheduleGeneratorPage.tsx` | On job status COMPLETED: calls `setLastScheduleId(job.scheduleId)` |
| `src/components/layout/Sidebar.tsx` | Weekly View link: `to={lastScheduleId ? \`/schedules/${lastScheduleId}/weekly\` : '/generate'}` |

---

### F-5 Add PNG Export Button

**Files modified:**
| File | Change |
|------|--------|
| `src/api/schedules.ts` | Added `downloadPng(scheduleId, theme, year)` — calls `GET /export/png?scheduleId=&theme=&year=` with `responseType: 'blob'` |
| `src/components/schedule/WeeklyGrid.tsx` | Added PNG download button using `downloadPng` with current `exportTheme` and `exportYear` |
| `src/pages/ScheduleGeneratorPage.tsx` | Added "Download PNG" button in completed state |

Created shared `triggerDownload(blob, filename)` helper used by all three export functions (PDF, Excel, PNG).

---

### F-6 Add Theme + Year Params to PDF/Excel Exports

**Files modified:**
| File | Change |
|------|--------|
| `src/api/schedules.ts` | Rewrote `downloadPdf()` and `downloadExcel()` with `theme` and `year` params. Both construct URL search params. |
| `src/components/schedule/WeeklyGrid.tsx` | Added export toolbar: NAVY/BLACK theme toggle (pill buttons) + year dropdown (All Years / FIRST–FOURTH). State: `exportTheme`, `exportYear`. Passed to all three download handlers. |

---

### F-7 Integrate `ScheduleCell` into `WeeklyGrid`

**Files modified:**
| File | Change |
|------|--------|
| `src/components/schedule/ScheduleCell.tsx` | Rewrote with `onClick` prop. Renders: courseCode (bold), courseName, instructorName, roomNumber, violation dots (red if hardViolations > 0, yellow if only softViolations). |
| `src/components/schedule/WeeklyGrid.tsx` | Replaced inline cell JSX with `<ScheduleCell entry={slot.entry} onClick={handleCellClick} />`. Empty cells render as `<div className="empty-cell">—</div>`. |

---

### F-8 Integrate `GenerationStatus` into `ScheduleGeneratorPage`

**Files modified:**
| File | Change |
|------|--------|
| `src/components/schedule/GenerationStatus.tsx` | Rewrote props interface: `job`, `elapsedSeconds`, `onViewSchedule`, `onRetry`, `onDownloadPdf`, `onDownloadExcel`, `onDownloadPng`, `onViewConflicts`. |
| `src/pages/ScheduleGeneratorPage.tsx` | Removed inline RUNNING/COMPLETED/FAILED JSX. Replaced with `<GenerationStatus ... />`. Kept elapsed time tracking via `useEffect` + `setInterval`. Passes all handlers. |

---

### F-9 Integrate `RoomForm` into `RoomsPage`

**Files modified:**
| File | Change |
|------|--------|
| `src/components/forms/RoomForm.tsx` | Added `loading?` prop for button spinner state. Removed `onCancel` prop requirement (not needed by other pages). |
| `src/pages/RoomsPage.tsx` | Removed inline form JSX entirely. Replaced with `<RoomForm onSubmit={handleSubmit} defaultValues={editingRoom ?? undefined} loading={submitting} />`. Modal wrapper stays in page. |

---

### F-10 Add `EmptyState` Component to All CRUD Pages

**Files created:**
| File | Description |
|------|-------------|
| `src/components/ui/EmptyState.tsx` | Props: `icon`, `title`, `description`, `action`. Centered layout with muted icon, heading, description paragraph, and action button slot. |

**Files modified — applied to all CRUD pages where `!loading && data.length === 0`:**
| Page | Icon | Title |
|------|------|-------|
| `CoursesPage.tsx` | BookOpen | "No courses yet" |
| `DepartmentsPage.tsx` | Building2 | "No departments yet" |
| `LecturersPage.tsx` | Users | "No lecturers yet" |
| `RoomsPage.tsx` | DoorOpen | "No rooms yet" |
| `SectionsPage.tsx` | LayoutList | "No sections yet" |
| `SemestersPage.tsx` | CalendarDays | "No semesters yet" |
| `TimeSlotsPage.tsx` | Clock | "No time slots yet" |
| `StudentsPage.tsx` | GraduationCap | "No students yet" |
| `EnrollmentsPage.tsx` | ClipboardList | "No enrollments yet" |
| `SchedulesPage.tsx` | Calendar | "No schedules yet" |

---

### F-11 Add `TableSkeleton` to Remaining CRUD Pages

**Files created (if missing):**
| File | Description |
|------|-------------|
| `src/components/ui/TableSkeleton.tsx` | Props: `rows` (default 5), `cols` (default 4). Renders animated pulse skeleton with header row + data rows. CSS grid layout matching column count. |

**Files modified — replaced plain "Loading..." text with `<TableSkeleton>`:**
| Page | Cols |
|------|------|
| `CoursesPage.tsx` | 6 |
| `DepartmentsPage.tsx` | 3 |
| `SectionsPage.tsx` | 7 |
| `SemestersPage.tsx` | 5 (uses direct loading check, no skeleton needed — already handled) |
| `TimeSlotsPage.tsx` | 5 |
| `StudentsPage.tsx` | 6 |
| `EnrollmentsPage.tsx` | 6 |
| `SchedulesPage.tsx` | 7 |

---

### F-12 Add Instructor Availability Modal

**Files created:**
| File | Description |
|------|-------------|
| `src/components/forms/InstructorAvailabilityModal.tsx` | Props: `instructor`, `isOpen`, `onClose`. Fetches all time slots + instructor's unavailable slots on open. Renders grid grouped by day (Sat–Thu). Each slot = toggle button: green = available, red = unavailable. Loading spinner while fetching. Title: "Availability — {instructor.name}". |

**Files modified:**
| File | Change |
|------|--------|
| `src/api/instructors.ts` | Added `getUnavailableSlots(instructorId)`, `addUnavailableSlot(instructorId, slotId)`, `removeUnavailableSlot(instructorId, slotId)` |
| `src/pages/LecturersPage.tsx` | Added `CalendarX` icon button per instructor card that opens `<InstructorAvailabilityModal>` |

---

### F-13 Wire `AnalyticsPage` to Real API

**Files created:**
| File | Description |
|------|-------------|
| `src/api/analytics.ts` | `getAnalytics()` → `GET /api/analytics` |

**Files modified:**
| File | Change |
|------|--------|
| `src/types/index.ts` | Added `RoomUtilizationDTO`, `InstructorWorkloadDTO`, `AnalyticsResponse` interfaces |
| `src/pages/AnalyticsPage.tsx` | Rewrote: `useEffect` → `getAnalytics()` on mount. Real data drives all KPI cards, room utilization bars, instructor workload bars. Loading state = `<TableSkeleton>`. Error state = toast + retry button. |

---

### F-14 Add `WeeklyGrid` Horizontal Scroll on Mobile

**Files modified:**
| File | Change |
|------|--------|
| `src/components/schedule/WeeklyGrid.tsx` | Already had `overflow-x-auto` wrapper — verified as already implemented. Task B-14 was a no-op. |

---

### F-15 Improve `HomePage` Design

**Files modified:**
| File | Change |
|------|--------|
| `src/pages/HomePage.tsx` | Complete rewrite: animated CSS grid hero background (`background-image: linear-gradient(...)` with 40px grid pattern), schedule preview SVG mockup in hero, feature cards with hover lift (`translateY(-4px)` + transition), tech stack badges strip (Spring Boot, Java, MySQL, React, TypeScript, JWT with Lucide icons), framer-motion scroll animations (`whileInView` with opacity + y offset), footer with GitHub link + copyright |

---

### F-16 Add `README.md`

**Files modified:**
| File | Change |
|------|--------|
| `README.md` (root) | Rewrote with: badges (Docker, CI), expanded backend/frontend feature lists, Docker deployment section (docker-compose.yml), CI pipeline reference, testing commands, project structure with Dockerfile/nginx.conf |

---

### Additional Frontend Work (beyond original F-1..F-16)

#### CSS Class Rebranding & Bug Fixes
Replaced undefined CSS classes across **16 files**:

| Old (undefined) | New (defined) | Files affected |
|-----------------|---------------|----------------|
| `headline-*` | `display-*` | 12 page components |
| `label-xs` | `label-sm` | 5 form/UI components |
| `p-md` | `p-6` | Various cards, containers |

**Dead route fixes:**
- Sidebar "Weekly View" link → `/generate` (was dead `/schedules`)
- Dashboard "View weekly schedule" button → `/generate`

**ESLint/TypeScript fixes:**
| File | Fix |
|------|-----|
| `AnalyticsPage.tsx` | Removed unused imports |
| `WeeklyGrid.tsx` | Fixed `ScheduleSummary` type import |
| `Table.tsx` | Fixed `any` type on columns; `as const` on framer-motion ease |
| `client.ts` | Fixed export syntax |
| Multiple pages | Removed unused `useNavigate` imports |

#### Students Page (Rewritten)
| File | Description |
|------|-------------|
| `StudentsPage.tsx` | Complete rewrite: real `GET /api/students`, Table with ID/Name/Email/Dept/Year/Level/Actions, Create/Edit via `StudentForm` in Modal |
| `api/students.ts` | API wrapper: `getStudents`, `createStudent`, `updateStudent`, `deleteStudent` |
| `components/forms/StudentForm.tsx` | Form: User dropdown, Department selector, academic year, level number |

#### Settings Page (Wired)
| File | Change |
|------|--------|
| `SettingsPage.tsx` | Replaced mock state with `useAuthStore` user data. Added `handleProfileSave` → `PUT /api/auth/profile`. Added `handlePasswordChange` → `PUT /api/auth/password` with min 8 char validation. Toasts. |
| `api/auth.ts` | Added `updateProfile()`, `changePassword()` |

#### Routes & Sidebar
| File | Change |
|------|--------|
| `App.tsx` | Added lazy-loaded routes for `/students`, `/enrollments`, `/schedules` |
| `Sidebar.tsx` | Added nav links to Students, Enrollments, Schedules pages |
| `types/index.ts` | Added `Student`, `StudentRequest`, `Enrollment`, `EnrollmentRequest`, `ScheduleSummary` types |

#### Accessibility
| Component | Change |
|-----------|--------|
| `App.tsx` | Added `<SkipLink>` component with `href="#main-content"` |
| `AppShell.tsx` | Renders `<main id="main-content">` |
| `Button.tsx` | `aria-label` when icon-only |
| `Modal.tsx` | `aria-modal="true"`, `role="dialog"`, Escape key handler |
| `Sidebar.tsx` | `aria-hidden="true"` on decorative SVG icons |
| `GenerationStatus.tsx` | `aria-live="polite"` on progress text |
| `index.css` | `prefers-reduced-motion` media query disables framer-motion animations |

#### Dark Mode Refinements (`index.css`)
- Background: `#131211` (lighter than `#111010`)
- Borders: `#2D2A27` (more visible than `#2A2725`)
- Destructive red: `#C24A4A` (brighter than `#B34040`)
- Added `-webkit-font-smoothing: antialiased` for macOS

#### Performance — Route-Level Code Splitting
All 16 route pages loaded via `React.lazy()`:
```tsx
const DashboardPage = lazy(() => import('./pages/DashboardPage').then(m => ({ default: m.DashboardPage })));
```
Each wrapped in `<Suspense fallback={<PageLoading />}>` + `<AnimatedPage>`.
**Impact**: Main bundle reduced from ~184 kB to ~117 kB (36% smaller).

#### React.memo
| File | Change |
|------|--------|
| `ScheduleCell.tsx` | Wrapped with `React.memo` — re-renders only when `cell` or `onCellClick` changes |
| `WeeklyGrid.tsx` | Wrapped with `React.memo` — avoids full grid re-render on unrelated state changes |

#### Docker & Deployment
| File | Description |
|------|-------------|
| `backend/Dockerfile` | Multi-stage: JDK 21 build (Maven) → JRE 21 runtime |
| `frontend/Dockerfile` | Multi-stage: Node 22 build → nginx:stable-alpine runtime |
| `frontend/nginx.conf` | SPA fallback, API proxy → `http://backend:8080` |
| `docker-compose.yml` | MySQL 8 + backend + frontend, health checks, persistent volume |
| `.github/workflows/ci.yml` | Backend: JDK 21 → compile → test. Frontend: Node 22 → install → lint → typecheck → test → build |

#### Testing Setup
| File | Description |
|------|-------------|
| `frontend/vitest.config.ts` | Vitest + jsdom + @testing-library/jest-dom setup |
| `frontend/src/test/setup.ts` | jest-dom matchers import |
| `frontend/src/test/Button.test.tsx` | 2 tests: renders children, shows spinner when loading |

---

## File Inventory — All Changes

### Modified Files (47+ total)
**Backend (13+):**
`pom.xml`, `application.properties`, `AsyncConfig.java`, `SecurityConfig.java`, `AuthService.java`, `AuthController.java`, `SemesterController.java`, `ScheduleController.java`, `ScheduleService.java`, `ScheduleServiceImpl.java`, `AsyncScheduleJobService.java`, `ScheduleRenderModel.java`, `SchedulePdfService.java`, `SchedulePngService.java`, `GlobalExceptionHandler.java`, `OpenApiConfig.java`, `CourseRequest.java`, `RoomRequest.java`, `ScheduleEntryRepository.java`, `ScheduleRepository.java`

**Frontend (33+):**
`package.json` + `package-lock.json`, `App.tsx`, `index.css`, `types/index.ts`, `api/auth.ts`, `api/client.ts`, `api/schedules.ts`, `api/instructors.ts`, `components/ErrorBoundary.tsx`, `components/layout/AppShell.tsx`, `components/layout/Sidebar.tsx`, `components/schedule/ScheduleCell.tsx`, `components/schedule/WeeklyGrid.tsx`, `components/schedule/GenerationStatus.tsx`, `components/ui/Modal.tsx`, `components/ui/Table.tsx`, `pages/AnalyticsPage.tsx`, `pages/CoursesPage.tsx`, `pages/DashboardPage.tsx`, `pages/DepartmentsPage.tsx`, `pages/InstructorSchedulePage.tsx`, `pages/LecturersPage.tsx`, `pages/RoomsPage.tsx`, `pages/ScheduleGeneratorPage.tsx`, `pages/SectionsPage.tsx`, `pages/SemestersPage.tsx`, `pages/SettingsPage.tsx`, `pages/StudentsPage.tsx`, `pages/TimeSlotsPage.tsx`, `store/authStore.ts`, `lib/utils.ts`, `README.md`

### New Files (25+)
**Backend:** `Dockerfile`, `application-dev.properties`, `ErrorResponse.java`, `AnalyticsResponse.java`, `RoomUtilizationDTO.java`, `InstructorWorkloadDTO.java`, `AnalyticsService.java`, `AnalyticsServiceImpl.java`, `AnalyticsController.java`, `InstructorAvailabilityService.java`, `InstructorAvailabilityServiceImpl.java`, `InstructorAvailabilityController.java`, `ProfileUpdateRequest.java`, `PasswordChangeRequest.java`, `StudentController.java`, `EnrollmentService.java`, `EnrollmentServiceImpl.java`, `EnrollmentController.java`, `ScheduleSummaryResponse.java`, `StudentControllerTest.java`, `EnrollmentControllerTest.java`

**Frontend:** `Dockerfile`, `nginx.conf`, `vitest.config.ts`, `src/test/setup.ts`, `src/test/Button.test.tsx`, `src/api/analytics.ts`, `src/api/enrollments.ts`, `src/api/students.ts`, `src/components/ui/ConfirmModal.tsx`, `src/components/ui/EmptyState.tsx`, `src/components/ui/TableSkeleton.tsx`, `src/components/RoleGuard.tsx`, `src/components/forms/InstructorAvailabilityModal.tsx`, `src/components/forms/StudentForm.tsx`, `src/components/forms/EnrollmentForm.tsx`, `src/store/scheduleStore.ts`, `src/pages/EnrollmentsPage.tsx`, `src/pages/SchedulesPage.tsx`

**Root:** `docker-compose.yml`, `.github/workflows/ci.yml`

---

## Verification Status

| Command | Result |
|---------|--------|
| `mvn compile` | ✅ Passes |
| `npm run build` | ✅ Passes |
| `npm run lint` | ✅ Passes |
| `tsc -b --noEmit` | ✅ Passes |
| `npm test` | ✅ 2/2 passing |
| `mvn test` | ✅ All tests pass (including fixed GlobalExceptionHandlerTest) |

---

*CampusGrid — Comprehensive Development Summary · Mahmoud Youssef · El Shorouk Academy · June 2026*
