# Frontend Review — TimeTable Scheduler

## Architecture

| Layer | Technology |
|-------|-----------|
| Framework | React 18 + TypeScript 5.6 |
| Build | Vite 5.4 |
| Styling | Tailwind CSS 4.3 |
| Routing | React Router DOM 6.30 |
| State | Zustand 5 (auth only) |
| Forms | React Hook Form + Zod |
| HTTP | Axios with JWT interceptor |
| Icons | Lucide React |
| Notifications | React Hot Toast |

---

## ✅ What's Working

### CRUD Operations (full: list/create/update/delete)
- Courses `/courses`
- Instructors (as `/lecturers`)
- Rooms `/rooms`
- Sections `/sections`
- Semesters `/semesters` (no delete)
- TimeSlots `/timeslots`
- Departments `/departments`

### Schedule Generation
- Semester selector → POST generate → poll job → completion UI
- Conflict display sidebar
- Download PDF / Excel buttons work

### Schedule Visualization
- Weekly grid (SAT–THU) with time slots
- Year level filter (ALL / FIRST / SECOND / THIRD / FOURTH)
- Department filter
- Session type color coding
- Conflict indicators (red/yellow dots)

### Auth
- Login / Register with JWT
- Token in localStorage, auto-attach via Axios interceptor
- Auto-redirect on 401 / expired token

### UI
- Dark/light mode (persisted)
- Responsive sidebar layout
- Toast notifications
- Error boundary
- Landing page

---

## 🔴 Critical Bugs & Missing Features

### 1. PNG Export Not Wired
**File:** `src/api/schedules.ts`
- Backend now has `GET /api/schedules/{id}/year/{year}/png?theme=NAVY`
- Frontend has **no PNG export button** anywhere
- **Fix:** Add `downloadPng()` function in `schedules.ts` + button in `WeeklyGrid` + `ScheduleGeneratorPage`

### 2. PDF/Excel Don't Use New Year+Theme Endpoints
**File:** `src/api/schedules.ts`
- Current frontend calls `GET /api/schedules/{id}/pdf` (no params)
- Backend now supports `?theme=NAVY|BLACK` and `/year/{year}/pdf`
- **Fix:** Update `downloadPdf()`/`downloadExcel()` to accept `year` and `theme` params; add year picker + theme toggle in UI

### 3. Sidebar "Weekly View" Link Broken
**File:** `src/components/layout/Sidebar.tsx`
- Links to `/schedules` → route doesn't exist → redirects to `/dashboard`
- **Fix:** Remove or link to `/schedules/:id/weekly` with last generated schedule ID

### 4. CSS Classes Missing
**Files:** `DashboardPage.tsx`, `WeeklySchedulePage.tsx`, `InstructorSchedulePage.tsx`
- Uses `headline-lg`, `headline-md` — **not defined** in `index.css`
- Causes invisible text or broken layout
- **Fix:** Add these classes to `index.css`

### 5. `ScheduleCell.tsx` Not Used
- Component exists but `WeeklyGrid.tsx` renders cells inline
- **Fix:** Integrate `ScheduleCell` into `WeeklyGrid` or remove

### 6. `GenerationStatus.tsx` Not Used
- Component exists but `ScheduleGeneratorPage.tsx` has its own inline UI
- **Fix:** Either integrate or remove dead code

### 7. Dead Code
| File | Status |
|------|--------|
| `LoginPage.tsx` | Replaced by `AuthPage.tsx` — delete |
| `InstructorsPage.tsx` | Duplicate of `LecturersPage.tsx` — delete |
| `Topbar.tsx` | Not used anywhere — delete |
| `ScheduleCell.tsx` | Exists but unused — integrate or delete |
| `GenerationStatus.tsx` | Exists but unused — integrate or delete |

### 8. `RoomsPage` Doesn't Use `RoomForm.tsx`
- Has its own inline form markup instead of reusing the `RoomForm` component
- **Fix:** Refactor to use `RoomForm.tsx`

---

## 🟡 Missing Features

### 9. Students Page is 100% Fake
**File:** `src/pages/StudentsPage.tsx`
- 10 hardcoded students, no API calls, no create/update/delete
- No `api/students.ts` file
- **Fix:** Either implement real CRUD or remove the page

### 10. Analytics Page is 100% Static
**File:** `src/pages/AnalyticsPage.tsx`
- All KPIs and charts are hardcoded
- **Fix:** Wire to real backend analytics endpoint or replace with useful page

### 11. Settings Page Has No Backend
**File:** `src/pages/SettingsPage.tsx`
- Profile form, notification toggles, theme selector — all UI-only
- **Fix:** Wire profile update to `PUT /api/users/{id}`, save to backend

### 12. Enrollments Page Missing
- No route, no page, no API module
- Backend has enrollment endpoints (`POST /api/enrollments`, etc.)
- **Fix:** Create `EnrollmentsPage` with CRUD

### 13. No Schedule List / History
- No way to browse all generated schedules
- Only accessible by ID or instructor's personal view
- **Fix:** Add `/schedules` page listing all generated schedules with links

### 14. No Role-Based Route Guarding
- `ProtectedRoute` only checks token expiry
- Doesn't check `role` from JWT (ADMIN / INSTRUCTOR / STUDENT)
- **Fix:** Add role check in `ProtectedRoute`, restrict pages per role

---

## 🟢 UI/UX Improvements

### 15. No Pagination on Any Table
- All CRUD tables load all records at once
- Will break with large datasets
- **Fix:** Add pagination or virtual scrolling

### 16. No Inline Schedule Editing
- Cannot adjust generated schedules manually
- **Fix:** Add drag-and-drop or cell click → reassign

### 17. `window.confirm()` Used for Delete
- Browser native confirm dialog
- **Fix:** Replace with custom `Modal` confirmation

### 18. No Lazy Loading
- All routes imported eagerly in `App.tsx`
- **Fix:** Add `React.lazy` + `Suspense` for route-level code splitting

### 19. No TanStack Query / SWR
- All data fetching is manual `useEffect` + `useState`
- No caching, no deduplication, no background refetch
- **Fix:** Adopt TanStack Query for server state

### 20. Font Classes Missing in `index.css`
- `headline-lg`, `headline-md` referenced but not defined
- **Fix:** Add proper typography classes

---

## 🔧 Quick Wins (Implement Now)

1. **Update `schedules.ts`** — add `downloadPng()` and year/theme params to existing export functions
2. **Add `headline-lg`/`headline-md` to `index.css`**
3. **Fix sidebar `/schedules` link** — remove or make functional
4. **Delete dead code** — `LoginPage.tsx`, `InstructorsPage.tsx`, `Topbar.tsx`
5. **Integrate `ScheduleCell` into `WeeklyGrid`**
6. **Wire export buttons** in `WeeklyGrid` and `ScheduleGeneratorPage` with year + theme options

---

## 📊 Summary Stats

| Metric | Count |
|--------|-------|
| Active source files | 39 |
| Pages | 16 (13 real, 3 mock) |
| API modules | 10 |
| Form components | 7 |
| Unused components | 4–5 |
| Routes | 17 |
