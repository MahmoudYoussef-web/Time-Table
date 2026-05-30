# Frontend Build Plan — Timetable Scheduler

## 📋 Overview

| Item | Detail |
|------|--------|
| Stack | React 18 + TypeScript + Tailwind CSS v4 + React Router v6 + Axios |
| Backend | Spring Boot 3.2 @ `http://localhost:8081` |
| Auth | JWT Bearer token → `localStorage` |
| State | Zustand (lightweight, no boilerplate) |
| Build | Vite 5 |

---

## Phase 0 — Project Scaffold

### Step 0.1: Initialize Vite + React + TS

```bash
npm create vite@latest timetable-frontend -- --template react-ts
cd timetable-frontend
npm install
```

### Step 0.2: Install Dependencies

```bash
npm install react-router-dom axios zustand
npm install -D @types/react @types/react-dom
```

### Step 0.3: Install Tailwind CSS v4

Tailwind v4 uses Vite plugin (NOT PostCSS config file like v3):

```bash
npm install tailwindcss @tailwindcss/vite
```

**`vite.config.ts`:**
```ts
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    proxy: { '/api': 'http://localhost:8081' }
  }
})
```

**`src/index.css`:**
```css
@import "tailwindcss";
```

### Step 0.4: Create Folder Structure

```
src/
├── api/            # Axios client + per-resource API modules
├── components/
│   ├── layout/     # AppShell, Sidebar, Topbar
│   ├── ui/         # Button, Card, Input, Select, Modal, Table, Badge, Spinner
│   ├── schedule/   # WeeklyGrid, ScheduleCell, ConflictBadge, GenerationStatus
│   └── forms/      # CourseForm, RoomForm, InstructorForm, SectionForm, SemesterForm, TimeslotForm
├── pages/          # One file per route
├── hooks/          # useAuth, useScheduleJob
├── store/          # authStore (Zustand)
└── types/          # All TS interfaces
```

---

## Phase 1 — Design System Foundation

### Step 1.1: Google Fonts

Add to `index.html` `<head>`:

```html
<link href="https://fonts.googleapis.com/css2?family=DM+Serif+Display&family=Quicksand:wght@300;400;500;600&display=swap" rel="stylesheet">
```

### Step 1.2: CSS Custom Properties

**`src/index.css`** — add AFTER `@import "tailwindcss"`:

```css
@import "tailwindcss";

@theme {
  --font-display: "DM Serif Display", serif;
  --font-body: "Quicksand", sans-serif;
}

:root {
  --background: #dad6ce;
  --foreground: #000000;
  --card: #dad6ce;
  --card-foreground: #000000;
  --muted: #cfcbc4;
  --muted-foreground: #575652;
  --border: #e5e7eb;
  --sidebar: #d1cdc6;
  --primary: #000000;
  --primary-foreground: #dad6ce;
  --secondary: #b7b4ad;
  --secondary-foreground: #ffffff;
  --cell-lecture: #DBEAFE;
  --cell-lab: #DCFCE7;
  --cell-tutorial: #FEF3C7;
  --cell-seminar: #F3E8FF;
  --cell-break: #F9FAFB;
  --destructive: #dc2626;
  --success: #16a34a;
  --warning: #d97706;
  --info: #0099ff;
  --radius-sm: 4px;
  --radius-md: 8px;
  --radius-lg: 12px;
  --radius-full: 9999px;
}

.dark {
  --background: #171715;
  --foreground: #f5f5f4;
  --card: #272725;
  --card-foreground: #f5f5f4;
  --muted: #2d2d2b;
  --muted-foreground: #9c9c9b;
  --border: #434342;
  --sidebar: #222220;
  --primary: #4d4d4d;
  --primary-foreground: #dad6ce;
}

body {
  font-family: var(--font-body);
  background: var(--background);
  color: var(--foreground);
}
```

### Step 1.3: Typography Utility Classes

Add to `index.css`:

```css
.headline-display { font-family: var(--font-display); font-size: 48px; font-weight: 700; }
.headline-lg     { font-family: var(--font-display); font-size: 35px; font-weight: 700; }
.headline-md     { font-family: var(--font-display); font-size: 26px; font-weight: 400; }
.body-lg         { font-family: var(--font-body); font-size: 16px; font-weight: 400; }
.body-md         { font-family: var(--font-body); font-size: 14px; font-weight: 300; }
.label-md        { font-family: var(--font-body); font-size: 14px; font-weight: 400; }
.label-sm        { font-family: var(--font-body); font-size: 12px; font-weight: 300; }
```

---

## Phase 2 — API Layer

### Step 2.1: Axios Client (`src/api/client.ts`)

Implementation:
- `baseURL: 'http://localhost:8081/api'` (Vite proxy will rewrite `/api` → backend)
- Request interceptor: reads `localStorage.getItem('token')` → attaches `Authorization: Bearer ${token}`
- Response interceptor: on 401 → clear token, redirect `/login`

### Step 2.2: API Service Modules (one per resource)

Create 10 files following the **exact same CRUD pattern**:

| File | Endpoints |
|------|-----------|
| `auth.ts` | POST `/auth/login`, POST `/auth/register` |
| `courses.ts` | GET `/courses`, GET `/courses/:id`, POST `/courses`, PUT `/courses/:id`, DELETE `/courses/:id` |
| `departments.ts` | same CRUD on `/departments` |
| `instructors.ts` | same CRUD on `/instructors` |
| `rooms.ts` | same CRUD on `/rooms` (with `roomType` field) |
| `sections.ts` | same CRUD on `/sections` (with `sessionType`, cascading dropdowns for course/instructor/semester) |
| `semesters.ts` | same CRUD on `/semesters` (with `status` field) |
| `timeslots.ts` | same CRUD on `/time-slots` |
| `schedules.ts` | POST `/schedules/generate/:semesterId`, GET `/schedules/jobs/:jobId`, GET `/schedules/:id/conflicts`, GET `/schedules/:id/pdf` (blob), GET `/schedules/:id/excel` (blob) |
| `weeklySchedule.ts` | GET `/weekly-schedules/:id`, GET `/instructor/schedule/my` |

Each API module returns the **parsed data** (`response.data`), not the Axios Response envelope.

### Step 2.3: PDF/Excel Download Pattern

The `downloadPdf` and `downloadExcel` functions:

```ts
export const downloadPdf = async (scheduleId: number, filename = 'schedule.pdf') => {
  const response = await client.get(`/schedules/${scheduleId}/pdf`, {
    responseType: 'blob'
  });
  const url = window.URL.createObjectURL(new Blob([response.data]));
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', filename);
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
};
```

---

## Phase 3 — Types (`src/types/index.ts`)

All interfaces from the prompt, **plus** adjustments for the actual backend DTOs:

### Key Adjustments vs Prompt:

1. **ScheduleDTO** — the backend returns a full DTO with `id`, `fitnessScore`, `hardViolations`, `softViolations`, `status`, `createdAt`, `entries: ScheduleEntryDTO[]`, `unscheduledSectionIds`, `semesterName`, `startDate`, `endDate`

2. **ScheduleEntryDTO** — matches this exact record:
```ts
{
  sectionId, sectionName, courseCode, courseName,
  instructorName, roomNumber, departmentName,
  dayOfWeek, startTime, endTime,
  yearLevel, sessionType,
  hardViolations, softViolations
}
```

3. **Async job response** — `POST /schedules/generate/:semesterId` returns a raw String (UUID), not JSON. The Axios response interceptor needs to handle this.

4. **Job status** — `GET /schedules/jobs/:jobId` returns `{ id, status: 'RUNNING'|'DONE'|'FAILED', scheduleId }`

### SessionType Enum
Must include `SECTION` (added to backend):
```ts
export type SessionType = 'LECTURE' | 'LAB' | 'TUTORIAL' | 'SEMINAR' | 'SECTION';
```

### RoomType Enum
```ts
export type RoomType = 'LECTURE_HALL' | 'LAB' | 'SEMINAR_ROOM';
```

### SemesterStatus
Backend uses `DRAFT` | `PUBLISHED` | `CLOSED` (NOT `ACTIVE` / `ARCHIVED`):
```ts
export type SemesterStatus = 'DRAFT' | 'PUBLISHED' | 'CLOSED';
```

---

## Phase 4 — State Management (Zustand)

### `src/store/authStore.ts`

```ts
interface AuthState {
  token: string | null;
  user: { id: number; email: string } | null;
  isAuthenticated: boolean;
  login: (token: string, user: { id: number; email: string }) => void;
  logout: () => void;
  initialize: () => void;  // read from localStorage on mount
}
```

- `login()` → saves to localStorage + updates state
- `logout()` → clears localStorage + redirects to `/login`
- `initialize()` → called once on app mount, reads token from localStorage

No persistence middleware needed — manual localStorage control is simpler and more explicit.

---

## Phase 5 — Routing (`src/App.tsx`)

### Route Table

| Path | Component | Auth | Notes |
|------|-----------|------|-------|
| `/login` | LoginPage | No | Redirect to `/dashboard` if already logged in |
| `/dashboard` | DashboardPage | Yes | Stats + quick actions |
| `/courses` | CoursesPage | Yes | CRUD table |
| `/departments` | DepartmentsPage | Yes | CRUD table |
| `/instructors` | InstructorsPage | Yes | CRUD table |
| `/rooms` | RoomsPage | Yes | CRUD table |
| `/sections` | SectionsPage | Yes | CRUD with dropdowns |
| `/semesters` | SemestersPage | Yes | CRUD table |
| `/timeslots` | TimeSlotsPage | Yes | CRUD table |
| `/schedules/generate` | ScheduleGeneratorPage | Yes | Async job polling |
| `/schedules/:id/weekly` | WeeklySchedulePage | Yes | Grid view |
| `/schedule/my` | InstructorSchedulePage | Yes | Instructor grid |

### ProtectedRoute Component

```tsx
function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const token = localStorage.getItem('token');
  if (!token) return <Navigate to="/login" replace />;
  return <>{children}</>;
}
```

Wrap in `App.tsx`:
```tsx
<BrowserRouter>
  <Routes>
    <Route path="/login" element={<LoginPage />} />
    <Route element={<ProtectedRoute><AppShell /></ProtectedRoute>}>
      <Route path="/dashboard" element={<DashboardPage />} />
      <Route path="/courses" element={<CoursesPage />} />
      {/* ... all other protected routes ... */}
    </Route>
  </Routes>
</BrowserRouter>
```

**Key decision:** `AppShell` (with Sidebar + Topbar) wraps only the protected routes. LoginPage is standalone.

---

## Phase 6 — Layout Components

### Step 6.1: AppShell.tsx

The shell layout:

```tsx
<div className="flex h-screen">
  <Sidebar />
  <div className="flex-1 flex flex-col">
    <Topbar />
    <main className="flex-1 overflow-y-auto p-md">
      <Outlet />
    </main>
  </div>
</div>
```

- Uses React Router `<Outlet />` for nested routes
- `h-screen` for full viewport height
- No scroll on shell, only `main` scrolls

### Step 6.2: Sidebar.tsx

- Width: `w-[220px]` on desktop
- Background: `var(--sidebar)`
- Navigation items grouped:
  - **Main:** Dashboard
  - **Academic:** Courses, Rooms, Instructors, Sections, Departments, Semesters, TimeSlots
  - **Schedules:** Generate, Weekly View, My Schedule
- Active link: `bg-black text-[#dad6ce]`
- Hover: `bg-[--muted]`
- Each item = `<NavLink>` from react-router-dom
- **Logout** at bottom (not a nav link — a button that calls `logout()`)

### Step 6.3: Topbar.tsx

- Height: `h-14` (56px)
- Background: `var(--background)`
- Bottom border: `1px solid var(--border)`
- Left side: hamburger icon (mobile) + page title from route
- Right side: user name/email + dark mode toggle (`🌙`)

### Step 6.4: Mobile Responsive Sidebar

- Default: hidden on mobile
- Toggle via hamburger icon in Topbar
- Show/hide with CSS class + transition
- Overlay backdrop when open on mobile

---

## Phase 7 — UI Components (detailed specs)

### Step 7.1: Button.tsx

```tsx
type Variant = 'primary' | 'secondary' | 'link' | 'danger';
type Size = 'sm' | 'md' | 'lg';

// Primary: bg-[--primary] text-[--primary-foreground] rounded-[--radius-sm]
// Secondary: border border-black text-black bg-transparent
// Link: underline text-black bg-transparent
// Danger: bg-[--destructive] text-white

// sm: px-3 py-1 text-sm
// md: px-4 py-2
// lg: px-6 py-3 text-lg

// disabled: opacity-50 cursor-not-allowed
// loading: show Spinner inside button
```

Props: `variant` (default `'primary'`), `size` (default `'md'`), `loading` (boolean), `disabled`, `onClick`, `children`, `className`, `type`

### Step 7.2: Card.tsx

Simple wrapper:
```tsx
<div className="bg-[--card] border border-[--border] rounded-[--radius-md] p-md">
  {children}
</div>
```

Props: `className`, `children`

### Step 7.3: Input.tsx

```tsx
type InputProps = React.InputHTMLAttributes<HTMLInputElement> & {
  label?: string;
  error?: string;
};
```

- Label: `label-md` class above the input
- Input: `border border-[--border] rounded-[--radius-sm] px-3 py-2 bg-[--background] w-full` focus: `ring-1 ring-black`
- Error: red text below, `body-md` size

Also create `Textarea.tsx` for longer text (if needed).

### Step 7.4: Select.tsx

Same pattern as Input but for `<select>`:
- Label, options, error display
- Styled consistently with Input

### Step 7.5: Modal.tsx

```tsx
// Portal to document.body via createPortal
// Overlay: fixed inset-0 bg-black/20 backdrop-blur-sm
// Content: centered, bg-[--card], rounded-[--radius-md], max-w-lg, w-full, p-md
// Close: on Escape key + backdrop click
// Animation: scale in (CSS transition)
```

Props: `isOpen`, `onClose`, `title` (string), `children`, `size` (`'sm'` | `'md'` | `'lg'`)

Title uses `headline-md` font.

### Step 7.6: Table.tsx

```tsx
interface Column<T> {
  key: keyof T | string;
  header: string;
  render?: (item: T) => React.ReactNode;
  sortable?: boolean;
}

interface TableProps<T> {
  columns: Column<T>[];
  data: T[];
  onEdit?: (item: T) => void;
  onDelete?: (item: T) => void;
  loading?: boolean;
  emptyMessage?: string;
}
```

Implementation:
- `<table>` with sticky header (`<thead>`)
- Zebra: even rows `bg-[--background]`, odd `bg-[--muted]`
- Hover: `hover:brightness-95`
- Actions column: ✏️ edit + 🗑️ delete buttons
- Empty state: centered message with icon

### Step 7.7: Badge.tsx

```tsx
type BadgeVariant = 'lecture' | 'lab' | 'tutorial' | 'seminar' | 'first' | 'second' | 'third' | 'fourth' | 'draft' | 'active' | 'closed';

// Session type badges:
// lecture: bg-[#DBEAFE] text-[#1e40af]
// lab: bg-[#DCFCE7] text-[#166534]
// tutorial: bg-[#FEF3C7] text-[#92400e]
// seminar: bg-[#F3E8FF] text-[#6b21a8]

// Year level badges:
// first/second/third/fourth: bg-[--muted] text-[--muted-foreground]

// Semester status badges:
// DRAFT: bg-gray-200 text-gray-700
// PUBLISHED: bg-green-100 text-green-800
// CLOSED: bg-stone-200 text-stone-700
```

Rounded `--radius-sm`, padding `px-2 py-0.5`, size `label-sm`.

### Step 7.8: Spinner.tsx

```tsx
// CSS circular spinner
// Sizes: sm (16px), md (24px), lg (36px)
// Color: currentColor (inherits from parent text)
// Animation: spin (Tailwind animate-spin)
```

---

## Phase 8 — Pages (implementation details)

### Step 8.1: LoginPage.tsx

```
Layout:
┌──────────────────────────────────┐
│                                  │
│   Timetable Scheduler            │ ← headline-display, centered
│                                  │
│   ┌────────────────────────┐     │
│   │ Email                  │     │
│   │ [____________________] │     │
│   │                        │     │
│   │ Password               │     │
│   │ [____________________] │     │
│   │                        │     │
│   │ [Sign In]              │     │
│   │                        │     │
│   │ Error message here     │     │ ← red text, shown only on error
│   └────────────────────────┘     │
│                                  │
└──────────────────────────────────┘
```

Full-page centered card on beige background. No sidebar.

Behavior:
1. User types email + password
2. Click "Sign In" → call `login()` from `auth.ts`
3. On success: `localStorage.setItem('token', token)` → `navigate('/dashboard')`
4. On error: show error message below button

### Step 8.2: DashboardPage.tsx

```
Dashboard                              ← headline-lg
───────────────────────────────────────

┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐
│  24    │ │  12    │ │  8     │ │  156   │
│ Courses│ │ Rooms  │ │ Instr. │ │Sections│
└────────┘ └────────┘ └────────┘ └────────┘

[Generate Schedule]  [View Weekly Schedule]

Recent Semesters
─────────────────
Semester Name          Status    Actions
Spring 2026          ● Active   View Schedule
Fall 2025            ● Closed   —
```

Stat cards: call GET endpoints (courses, rooms, instructors, sections) and show count.

Quick actions: buttons linking to `/schedules/generate` and `/schedules/:id/weekly` (latest schedule).

Recent semesters: `GET /semesters` → show last 5 with status badges. "View Schedule" button only for PUBLISHED semesters.

### Step 8.3: CoursesPage.tsx (and all CRUD pages)

**Structure:**
```
Courses                            ← headline-lg
───────────────────────────────────
[+ Add Course]        [🔍 Search]

┌────┬────────┬────────┬──────┬─────────┐
│ ID │ Code   │ Name   │ Hrs  │ Actions │
├────┼────────┼────────┼──────┼─────────┤
│ 1  │ CS101  │ Intro  │ 3    │ ✏  🗑  │
└────┴────────┴────────┴──────┴─────────┘
```

**Shared CRUD pattern for all pages:**

1. **Fetch data** on mount via `useEffect` → API call → setState
2. **Table** renders columns dynamically (Table component)
3. **Add** → open Modal/Form → POST → refresh list
4. **Edit** → populate Modal/Form with existing data → PUT → refresh
5. **Delete** → confirm dialog: "Are you sure? This cannot be undone." → DELETE → refresh

**CourseForm fields:**
- code (text input)
- name (text input)
- creditHours (number input, min 1)
- departmentId (select from GET /departments)

**RoomForm fields:**
- building (text)
- roomNumber (text)
- capacity (number, min 1)
- roomType (select: LECTURE_HALL / LAB / SEMINAR_ROOM)

**InstructorForm fields:**
- email (text)
- password (text, only on create)
- fullName (text)
- departmentId (select from GET /departments)

**SectionForm fields:**
- name (text)
- capacity (number)
- yearLevel (select: FIRST/SECOND/THIRD/FOURTH)
- sessionType (select: LECTURE/LAB/TUTORIAL/SEMINAR/SECTION)
- courseId (select from GET /courses)
- instructorId (select from GET /instructors)
- semesterId (select from GET /semesters)

**SemesterForm fields:**
- name (text)
- startDate (date input)
- endDate (date input)
- status (select: DRAFT/PUBLISHED/CLOSED)

**TimeSlotForm fields:**
- day (select: MONDAY/TUESDAY/WEDNESDAY/THURSDAY/FRIDAY/SATURDAY/SUNDAY)
- startTime (time input)
- endTime (time input)

### Step 8.4: ScheduleGeneratorPage.tsx

**Three states:**

State 1 — Idle:
```
Select Semester: [dropdown ▼]

[Generate Schedule]
```

State 2 — Running (after click):
```
⚙️ Generating schedule...
Job ID: abc-123-...
Status: ● RUNNING

[indeterminate progress bar]

⏱ Elapsed: 12 seconds
```

State 3 — Done:
```
✅ Schedule generated successfully!
Schedule ID: 7

[View Weekly Schedule]  [Download PDF]  [Download Excel]
[View Conflicts (3 violations)]

⚠️ Constraint Violations
● Room 201 double-booked Monday 10:00
```

State 4 — Failed:
```
❌ Schedule generation failed
Server error: No sections found for this semester

[Try Again]
```

**Implementation details:**

1. Semester dropdown populated from `GET /semesters` (only PUBLISHED or DRAFT)
2. "Generate Schedule" button → `POST /schedules/generate/{semesterId}` → receives UUID string
3. Poll with `useScheduleJob` hook every 2 seconds
4. On DONE: store `scheduleId` → show action buttons
5. PDF download: `downloadPdf(scheduleId)` blob approach
6. Excel download: same as PDF
7. Conflicts: `GET /schedules/{id}/conflicts` → show list

### Step 8.5: WeeklySchedulePage.tsx

**The timetable grid — most complex component.**

Data source: `GET /api/weekly-schedules/{id}` returns `WeeklyScheduleDTO`

**Grid layout:**
```
                SAT       SUN       MON       TUE       WED       THU
08:00-08:30 │  [cell]   [cell]    [cell]    [cell]    [cell]    [cell]
08:30-09:00 │  [cell]    —         [cell]     —        [cell]     —
09:00-09:30 │   —       [cell]     —        [cell]     —        [cell]
...
```

**Implementation approach:**

```tsx
// 1. Flatten the WeeklyScheduleDTO into a lookup map
//    key = `${day}-${startTime}`
//    value = ScheduleEntryDTO

// 2. Generate time slots from the data (collect all unique start-end combinations)
//    Sort by start time

// 3. Render grid:
//    - Header row: empty corner + day names
//    - Data rows: time label + 6 cells (SAT-THU)
//    - Each cell: look up entry from map → render ScheduleCell if exists

// 4. ScheduleCell renders:
//    - Background color from sessionType
//    - courseCode — courseName (bold)
//    - instructorName
//    - roomNumber (sessionType)
//    - Top-right: red dot if hardViolations > 0, amber dot if softViolations > 0
```

**Filters above grid:**
- Year level tabs: All | First | Second | Third | Fourth
  - "All" shows the full grid
  - Each level tab filters entries by `yearLevel` field
- Department dropdown: filter by `departmentName`
- The grid re-renders when filters change (no new API call)

**Export buttons:**
- "Download PDF" → blob download
- "Download Excel" → blob download

**Conflicts panel:**
- Slide-in from right (fixed position, `right-0`, `top-0`, `w-[400px]`, `h-full`)
- Overlay behind it
- List of `ConstraintViolation` objects
- Hard violations in red, soft in amber
- Close button (X) or click outside

### Step 8.6: InstructorSchedulePage.tsx

Reuses the same `WeeklyGrid` component but:
- Calls `GET /api/instructor/schedule/my`
- Title: "My Schedule"
- No year level tabs or department filter
- No export buttons (unless backend supports it)
- Simpler layout

---

## Phase 9 — Custom Hooks

### Step 9.1: useAuth.ts

```ts
function useAuth() {
  const { token, user, isAuthenticated, login, logout } = useAuthStore();

  const loginUser = async (email: string, password: string) => {
    const response = await loginApi({ email, password });
    // response = { success, message, token, user }
    if (response.success) {
      login(response.token, response.user);
      return { success: true };
    }
    return { success: false, message: response.message };
  };

  const logoutUser = () => {
    logout();
    // navigates to /login via the store's logout
  };

  return { token, user, isAuthenticated, loginUser, logoutUser };
}
```

### Step 9.2: useScheduleJob.ts

Already defined in the prompt. Uses `setInterval` polling.

Key behavior:
- Only polls when `jobId` is non-null
- Stops polling when status is `DONE` or `FAILED`
- Cleans up interval on unmount

---

## Phase 10 — Schedule Components

### Step 10.1: WeeklyGrid.tsx

The main grid component. Props:
```ts
interface WeeklyGridProps {
  entries: ScheduleEntryDTO[];
  title?: string;
  showFilters?: boolean;
  showExport?: boolean;
  scheduleId?: number;
}
```

Internally:
- Accepts flat `ScheduleEntryDTO[]`
- Groups into day×time matrix
- Renders `ScheduleCell` for each occupied slot
- Handles empty slots (render nothing or light border)

### Step 10.2: ScheduleCell.tsx

Props:
```ts
interface ScheduleCellProps {
  entry: ScheduleEntryDTO;
}
```

Renders:
- Background color from `sessionColorMap[entry.sessionType]`
- Padding: `p-1.5`
- Font: `body-sm` for details, `label-sm` for small text
- Red/amber dot top-right for violations
- Hover: slight brightness change + cursor pointer (for future click-to-expand)

### Step 10.3: GenerationStatus.tsx

Shows the job polling UI:
- `RUNNING`: spinner + "Generating..." + elapsed time
- `DONE`: checkmark + action buttons
- `FAILED`: X icon + error message + retry button

### Step 10.4: ConflictBadge.tsx

Renders violation list items:
```tsx
// Hard violation: text-red-600, left border red
// Soft violation: text-amber-600, left border amber
// Format: "ConstraintName — Section message"
```

---

## Phase 11 — Dark Mode

### Implementation:
1. Toggle button in Topbar (🌙/☀️)
2. Click → toggle `dark` class on `<html>` element
3. Persist preference in `localStorage('darkMode')`
4. On mount: check localStorage + system preference (`prefers-color-scheme: dark`)

The CSS variables already handle dark mode via `.dark` selector.

---

## Phase 12 — Polish & Edge Cases

### Step 12.1: Loading States
- Tables: show `Spinner` overlay during initial fetch
- Forms: button shows `loading` state during submit
- Grid: skeleton placeholder while loading

### Step 12.2: Empty States
- No courses: "No courses found. Create your first course."
- No schedule: "No schedule generated yet. Go to Generate page."
- No conflicts: "No constraint violations found. ✅"

### Step 12.3: Error Handling
- API errors: show toast notification (bottom-right, auto-dismiss 5s)
- Network error: "Connection lost. Check your server."
- Form validation: inline errors below each field

### Step 12.4: Toast Notifications
Create a simple toast system:
```tsx
// ToastProvider at App root
// useToast() returns { showToast(message, type) }
// Types: success (green), error (red), info (blue)
// Auto-dismiss after 5 seconds
// Stack multiple toasts
```

### Step 12.5: Egyptian Week Order
Days render as: SATURDAY, SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY (skip FRIDAY)

If data includes FRIDAY entries, render them in a 7th column.

---

## Phase 13 — Build & Verify

### Step 13.1: Vite Build

```bash
npm run build
```

Verify no TS errors, no unused imports, no `any` types.

### Step 13.2: Dev Server

```bash
npm run dev
```

Test:
1. Login with admin@uni.com / admin123
2. CRUD operations on each page
3. Schedule generation flow
4. Weekly grid display
5. PDF/Excel download
6. Dark mode toggle
7. Mobile responsive layout

---

## 📦 File Creation Order (for implementation)

To minimize context switching and ensure no file is missed, implement in this exact order:

```
 1. src/types/index.ts
 2. src/api/client.ts
 3. src/api/auth.ts
 4. src/api/courses.ts (and all CRUD APIs)
 5. src/store/authStore.ts
 6. src/hooks/useAuth.ts
 7. src/hooks/useScheduleJob.ts
 8. src/index.css (design system)
 9. src/components/ui/Spinner.tsx
10. src/components/ui/Button.tsx
11. src/components/ui/Card.tsx
12. src/components/ui/Input.tsx
13. src/components/ui/Select.tsx
14. src/components/ui/Badge.tsx
15. src/components/ui/Modal.tsx
16. src/components/ui/Table.tsx
17. src/components/layout/Sidebar.tsx
18. src/components/layout/Topbar.tsx
19. src/components/layout/AppShell.tsx
20. src/pages/LoginPage.tsx
21. src/pages/DashboardPage.tsx
22. src/pages/CoursesPage.tsx (pattern for all CRUD)
23. src/pages/RoomsPage.tsx
24. src/pages/InstructorsPage.tsx
25. src/pages/SectionsPage.tsx
26. src/pages/DepartmentsPage.tsx
27. src/pages/SemestersPage.tsx
28. src/pages/TimeSlotsPage.tsx
29. src/components/schedule/ScheduleCell.tsx
30. src/components/schedule/WeeklyGrid.tsx
31. src/components/schedule/ConflictBadge.tsx
32. src/components/schedule/GenerationStatus.tsx
33. src/pages/ScheduleGeneratorPage.tsx
34. src/pages/WeeklySchedulePage.tsx
35. src/pages/InstructorSchedulePage.tsx
36. src/App.tsx
37. src/main.tsx
38. vite.config.ts
39. index.html
```

---

## ⚠️ Critical Backend Adjustments Needed

During frontend implementation, these backend mismatches must be handled:

### 1. Login Response

Backend returns:
```json
{ "success": true, "message": "...", "token": "jwt...", "user": { "id": 1, "email": "admin@uni.com" } }
```

### 2. Course Create Request

Backend `CourseRequest` expects:
```json
{ "code": "CS101", "name": "Intro", "creditHours": 3, "departmentId": 1 }
```
Response `CourseResponse` includes `departmentName`, not `departmentId`.

### 3. Instructor Create

Backend `InstructorRequest`:
```json
{ "email": "...", "password": "...", "fullName": "...", "departmentId": 1 }
```

### 4. Section Create

Backend `SectionRequest`:
```json
{ "name": "...", "courseId": 1, "instructorId": 1, "semesterId": 1, "capacity": 30, "yearLevel": "FIRST", "sessionType": "LECTURE" }
```

### 5. Semester Status

Backend enum is: `DRAFT` | `PUBLISHED` | `CLOSED` (NOT `ACTIVE` / `ARCHIVED`)

### 6. Generate Schedule Response

`POST /schedules/generate/{semesterId}` returns a **plain string** (UUID), not JSON. Axios interceptor handles it as `response.data` = `"uuid-string"`.

### 7. Weekly Schedule Days

Backend `TimeSlot.day` uses `DayOfWeek` enum (MONDAY, TUESDAY, ...). The grid should display SAT→THU order but the API may return entries in standard DayOfWeek order. The frontend must sort/reorder days for Egyptian week display.

### 8. SectionController — Sections List

`GET /sections` might return a list of `SectionResponse` objects (not wrapped in `{ data: [...] }`). Check actual response format and adjust frontend types if needed.

### 9. Pagination

Backend endpoints might not support pagination yet. The frontend Table should assume all data is returned at once (no server-side pagination for now).

### 10. ScheduleDTO Unscheduled Sections

The `ScheduleDTO` has `unscheduledSectionIds: number[]`. The WeeklySchedulePage should show a warning banner if there are unscheduled sections: _"Warning: 7 sections could not be scheduled"_.

---

## ✅ Final Decision Points for You

Please confirm these before I start coding:

1. **State management:** Zustand (recommended) or Context API?
2. **Table library:** Custom Table component (as planned) or use a library like `@tanstack/react-table`?
3. **Date/time handling:** Plain JS Date or `date-fns` / `dayjs`?
4. **Toast notifications:** Custom (as planned) or `react-hot-toast`?
5. **Form validation:** Manual (as planned) or `react-hook-form` + `zod`?
6. **Icon library:** None (inline emoji/SVGs as planned) or `lucide-react`?
7. **Dark mode toggle default:** Follow system preference or default to light?
8. **Mobile-first or desktop-first:** Desktop-first (as planned) with responsive sidebar?
9. **Backend base URL in production:** Same Vite proxy or a separate production API URL?
10. **Do you want me to create a `README.md`** for the frontend project with setup instructions?
