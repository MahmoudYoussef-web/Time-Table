<div align="center">

<img src="https://readme-typing-svg.demolab.com?font=Fira+Code&weight=700&size=28&pause=1000&color=22C55E&center=true&vCenter=true&width=700&lines=CampusGrid;Academic+Scheduling+Platform;React+%7C+TypeScript+%7C+Tailwind+CSS+4" alt="Typing SVG" />

<br/>

**Modern administrative SPA for university timetable management.**  
Full CRUD for academic data, real-time schedule generation tracking, interactive weekly grid, and PDF/Excel export integration.

<br/>

![React](https://img.shields.io/badge/React_18-61DAFB?style=flat-square&logo=react&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript_5.6-3178C6?style=flat-square&logo=typescript&logoColor=white)
![Vite](https://img.shields.io/badge/Vite_5-646CFF?style=flat-square&logo=vite&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS_4-06B6D4?style=flat-square&logo=tailwindcss&logoColor=white)
![React Router](https://img.shields.io/badge/React_Router_6-CA4245?style=flat-square&logo=reactrouter&logoColor=white)

[![Zustand](https://img.shields.io/badge/State-Zustand_5-brown?style=flat-square)](https://github.com/pmndrs/zustand)
[![Form](https://img.shields.io/badge/Form-RHF_%2B_Zod-purple?style=flat-square)](https://react-hook-form.com/)
[![Animation](https://img.shields.io/badge/Animation-Framer_Motion-0055FF?style=flat-square)](https://www.framer.com/motion/)
[![API](https://img.shields.io/badge/HTTP-Axios-5A29E4?style=flat-square)](https://axios-http.com/)

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Architecture](#-architecture)
- [Features](#-features)
- [Pages & Routes](#-pages--routes)
- [Component Library](#-component-library)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)

---

## 🌐 Overview

**CampusGrid** is the frontend administrative interface for the Timetable Scheduler system. It provides a complete UI for managing university academic data, triggering Genetic Algorithm schedule generation, and viewing/exporting the resulting timetables.

The app features a **warm neutral design system** with full dark mode support, responsive layout, animated page transitions, and real-time status polling for async generation jobs.

---

## 🏗️ Architecture

```
┌────────────────────────────────────────────────────────────┐
│                    React SPA (:5173)                        │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │  Pages   │  │Components│  │  Store   │  │API Client│   │
│  │ (18 pgs) │  │(23 comps)│  │(Zustand) │  │ (Axios)  │   │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘   │
│       └──────────────┴─────────────┴──────────────┘         │
└──────────────────────────┬──────────────────────────────────┘
                           │ Proxy (/api → :8080)
┌──────────────────────────▼──────────────────────────────────┐
│              Spring Boot Backend (:8081)                      │
└──────────────────────────────────────────────────────────────┘
```

### Design Decisions

| Decision | Rationale |
|---|---|
| **Zustand over Redux** | Minimal boilerplate, single auth store, built-in persistence |
| **CSS custom properties** | Theme switching (light/dark) without Tailwind `dark:` class prefix |
| **Axios interceptors** | Centralized JWT expiry check and 401 redirect |
| **Framer Motion** | Page-level enter/exit animations for polished UX |
| **Zod + RHF** | Type-safe form validation with minimal re-renders |
| **Tailwind CSS 4** | Utility-first styling with `@theme` custom design tokens |

---

## ✨ Features

### 🔐 Authentication
- Login/Register with JWT-based auth
- Auto token expiry detection on each request
- 401 interceptor → clear storage → redirect
- Protected route wrapper with fallback redirect

### 🏫 Academic Data Management (CRUD)
7 entity management pages with consistent table/modal patterns:

| Entity | Page | Form Fields |
|---|---|---|
| **Courses** | `/courses` | Code, name, credits, department |
| **Departments** | `/departments` | Code, name |
| **Instructors** | `/lecturers` | Name, email, password, department |
| **Rooms** | `/rooms` | Building, room number, capacity, type |
| **Sections** | `/sections` | Name, capacity, year, session type, course, instructor, semester |
| **Semesters** | `/semesters` | Name, start/end date, status |
| **Time Slots** | `/timeslots` | Day, start time, end time |

Each CRUD page features:
- `Table` component with animated rows and hover-reveal actions
- `Modal` with `Form` component (Zod validation)
- Loading skeletons and empty states
- Toast notifications on success/error

### 🧬 Schedule Generation
- Semester selector → trigger generation
- Real-time job polling (every 2s) with progress bar
- Completed: view weekly, download PDF/Excel, see conflicts
- Failed: retry button with error details

### 📊 Weekly Timetable Viewer
- Interactive grid: SAT–THU × time slots
- Filter by year level (FIRST–FOURTH) and department
- Color-coded cells by session type:
  - 🔵 Blue — Lecture
  - 🟢 Green — Lab/Tutorial/Section
  - 🟡 Beige — Break
- Conflict indicators: 🔴 red dot (hard), 🟡 yellow (soft)
- Instructor personal schedule (`/instructor/schedule`)

### 🎨 Design System
- Warm neutral palette (`#F7F4EF` background)
- 3 font families: Instrument Serif (display), Geist (body), DM Mono (code)
- Custom CSS variables for all colors (no Tailwind `dark:` prefix)
- Subtle noise texture overlay
- Framer Motion page transitions (fade + slide)
- Responsive sidebar: 220px fixed → mobile overlay
- Dark/light/system theme selector

### 📦 Data Export
- PDF download (triggers backend endpoint)
- Excel download (triggers backend endpoint)
- Direct download via `window.open` with JWT header

### 📈 Analytics Dashboard (Mock)
- KPI cards: conflict-free %, room utilization, satisfaction, efficiency
- Room utilization bar chart
- Lecturer workload distribution
- Schedule efficiency timeline
- Conflict reduction ring visualization

### ⚙️ Settings (UI)
- Profile editing (name, email, bio)
- Notification toggles (email push, schedule updates)
- Appearance: light / dark / system
- Security: password change, 2FA toggle (UI only)

---

## 🗺️ Pages & Routes

| Path | Page | Auth | Description |
|---|---|---|---|
| `/` | HomePage | Public | Landing page with hero, features, how-it-works flow |
| `/auth` | AuthPage | Public | Sign-in / Sign-up tabs |
| `/dashboard` | DashboardPage | ✓ | Stats cards, quick actions |
| `/courses` | CoursesPage | ✓ | Course CRUD |
| `/departments` | DepartmentsPage | ✓ | Department CRUD |
| `/lecturers` | LecturersPage | ✓ | Instructor CRUD (cards) |
| `/rooms` | RoomsPage | ✓ | Room CRUD |
| `/sections` | SectionsPage | ✓ | Section CRUD |
| `/semesters` | SemestersPage | ✓ | Semester CRUD |
| `/timeslots` | TimeSlotsPage | ✓ | Time slot CRUD |
| `/generate` | ScheduleGeneratorPage | ✓ | Generate schedule |
| `/schedules/:id/weekly` | WeeklySchedulePage | ✓ | View weekly grid |
| `/instructor/schedule` | InstructorSchedulePage | ✓ | My schedule |
| `/students` | StudentsPage | ✓ | Student list (mock) |
| `/analytics` | AnalyticsPage | ✓ | Analytics dashboard (mock) |
| `/settings` | SettingsPage | ✓ | Settings (UI only) |

---

## 🧩 Component Library

### Layout (`components/layout/`)
| Component | Description |
|---|---|
| `AppShell` | Authenticated layout: sidebar + topbar + `<Outlet />` |
| `Sidebar` | 220px fixed nav with grouped links + logout |
| `Navbar` | Public landing page navbar with logo + dark mode toggle |
| `Topbar` | Alternative top bar with menu toggle |

### UI (`components/ui/`)
| Component | Props | Description |
|---|---|---|
| `Button` | variant, size, loading | Styled button with spinner |
| `Input` | label, error, variant | Form input with validation |
| `Select` | label, error, options | Form select dropdown |
| `Modal` | size, open, onClose | Portal-based modal |
| `Card` | hover | Container card |
| `Table` | columns, data, loading | Animated CRUD table |
| `Spinner` | size | SVG loader |
| `Badge` | + SessionBadge, StatusBadge, YearBadge | Colored labels |

### Forms (`components/forms/`)
| Component | Entity | Validation |
|---|---|---|
| `CourseForm` | Course | code, name, credits, department |
| `DepartmentForm` | Department | code, name |
| `InstructorForm` | Instructor | name, email, password, department |
| `RoomForm` | Room | building, room, capacity, type |
| `SectionForm` | Section | name, capacity, year, type, course, instructor, semester |
| `SemesterForm` | Semester | name, dates, status |
| `TimeslotForm` | TimeSlot | day, start, end |

### Schedule (`components/schedule/`)
| Component | Description |
|---|---|
| `WeeklyGrid` | Full timetable table with filters + export |
| `ScheduleCell` | Individual cell with conflict indicators |
| `ConflictBadge` | Hard/soft violation display |
| `GenerationStatus` | Running/completed/failed states |

---

## 🛠️ Tech Stack

| Category | Library | Version | Purpose |
|---|---|---|---|
| Framework | React | 18.3.1 | UI library |
| Language | TypeScript | 5.6.2 | Type safety |
| Build | Vite | 5.4.10 | Dev server + bundler |
| Styling | Tailwind CSS | 4.3.0 | Utility-first CSS |
| Routing | React Router DOM | 6.30.4 | Client-side routing |
| State | Zustand | 5.0.14 | Global state management |
| HTTP | Axios | 1.16.1 | API client |
| Forms | React Hook Form | 7.76.1 | Form state management |
| Validation | Zod | 4.4.3 | Schema validation |
| Resolvers | @hookform/resolvers | 5.4.0 | RHF + Zod bridge |
| Animation | Framer Motion | 12.40.0 | Page transitions |
| Icons | Lucide React | 1.17.0 | SVG icons |
| Dates | Dayjs | 1.11.21 | Date/time formatting |
| Toasts | React Hot Toast | 2.6.0 | Notifications |
| Linting | ESLint + typescript-eslint | 9.x | Code quality |

---

## 📁 Project Structure

```
frontend/
├── index.html                    # Entry HTML
├── vite.config.ts                # Vite config + API proxy
├── package.json
├── tsconfig.json
├── eslint.config.js
├── public/
│   └── vite.svg
└── src/
    ├── main.tsx                  # App entry (theme init + render)
    ├── App.tsx                   # Router + routes + protected wrapper
    ├── index.css                 # Tailwind + theme variables + typography
    ├── vite-env.d.ts
    ├── api/                      # Axios client + 10 resource modules
    │   ├── client.ts             # Axios instance with JWT interceptors
    │   ├── auth.ts               # login / register
    │   ├── courses.ts            # CRUD
    │   ├── rooms.ts              # CRUD
    │   ├── instructors.ts        # CRUD
    │   ├── sections.ts           # CRUD
    │   ├── semesters.ts          # CRUD
    │   ├── timeslots.ts          # CRUD
    │   ├── departments.ts        # CRUD
    │   ├── schedules.ts          # generate / poll / export
    │   └── weeklySchedule.ts     # weekly / instructor schedule
    ├── components/
    │   ├── layout/
    │   │   ├── AppShell.tsx
    │   │   ├── Sidebar.tsx
    │   │   ├── Navbar.tsx
    │   │   └── Topbar.tsx
    │   ├── ui/
    │   │   ├── Button.tsx
    │   │   ├── Input.tsx
    │   │   ├── Select.tsx
    │   │   ├── Modal.tsx
    │   │   ├── Card.tsx
    │   │   ├── Spinner.tsx
    │   │   ├── Table.tsx
    │   │   └── Badge.tsx
    │   ├── forms/
    │   │   ├── CourseForm.tsx
    │   │   ├── DepartmentForm.tsx
    │   │   ├── InstructorForm.tsx
    │   │   ├── RoomForm.tsx
    │   │   ├── SectionForm.tsx
    │   │   ├── SemesterForm.tsx
    │   │   └── TimeslotForm.tsx
    │   ├── schedule/
    │   │   ├── WeeklyGrid.tsx
    │   │   ├── ScheduleCell.tsx
    │   │   ├── ConflictBadge.tsx
    │   │   └── GenerationStatus.tsx
    │   └── ErrorBoundary.tsx
    ├── pages/
    │   ├── HomePage.tsx          # Landing
    │   ├── AuthPage.tsx          # Login / Register
    │   ├── DashboardPage.tsx     # Stats + quick actions
    │   ├── CoursesPage.tsx       # CRUD
    │   ├── DepartmentsPage.tsx   # CRUD
    │   ├── LecturersPage.tsx     # CRUD (cards)
    │   ├── RoomsPage.tsx         # CRUD
    │   ├── SectionsPage.tsx      # CRUD
    │   ├── SemestersPage.tsx     # CRUD
    │   ├── TimeSlotsPage.tsx     # CRUD
    │   ├── ScheduleGeneratorPage.tsx  # Generate + poll
    │   ├── WeeklySchedulePage.tsx     # View schedule
    │   ├── InstructorSchedulePage.tsx # My schedule
    │   ├── StudentsPage.tsx      # Mock
    │   ├── AnalyticsPage.tsx     # Mock
    │   └── SettingsPage.tsx      # UI only
    ├── store/
    │   └── authStore.ts          # Zustand auth store
    ├── hooks/
    │   ├── useAuth.ts            # Auth hook
    │   └── useScheduleJob.ts     # Job polling hook
    ├── lib/
    │   └── utils.ts              # cn(), formatters, constants
    └── types/
        └── index.ts              # All TypeScript interfaces
```

---

## 🚀 Getting Started

### Prerequisites
- Node.js 18+
- Backend running on `http://localhost:8081` (see [backend README](../backend/README.md))

### Setup

```bash
# Clone and navigate to frontend
cd frontend

# Install dependencies
npm install

# Start dev server
npm run dev
```

The Vite dev server proxies `/api` requests to `http://localhost:8080`.

### Build for Production

```bash
npm run build
npm run preview   # Preview the build locally
```

Output goes to `frontend/dist/`.

### Default Login

| Field | Value |
|---|---|
| URL | `http://localhost:5173` |
| Email | `admin@uni.com` |
| Password | `admin123` |

---

## 🔌 API Integration

All API calls go through a centralized Axios client (`src/api/client.ts`):

```typescript
// Auto-attaches JWT token
const client = axios.create({ baseURL: '/api' });
client.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Auto-redirect on 401
client.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.clear();
      window.location.href = '/auth';
    }
    return Promise.reject(err);
  }
);
```

---

## 🧪 Linting

```bash
npm run lint
```

---

## 👤 Author

**Mahmoud Youssef** · El Shorouk Academy, 2026

[![GitHub](https://img.shields.io/badge/GitHub-MahmoudYoussef--web-181717?style=flat-square&logo=github)](https://github.com/MahmoudYoussef-web)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-mahmoud--youssef--dev-0A66C2?style=flat-square&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/mahmoud-youssef-dev/)

---

<div align="center">
  <sub>Open to remote & international opportunities · Graduation Project 2026</sub>
</div>
