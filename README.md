<div align="center">

<img src="https://readme-typing-svg.demolab.com?font=Fira+Code&weight=700&size=28&pause=1000&color=22C55E&center=true&vCenter=true&width=600&lines=TimeTable+Scheduler;Academic+Scheduling+Platform;Backend+%7C+Frontend+%7C+Monorepo" alt="Typing SVG" />

<br/>

**Full-stack university timetabling system** — Spring Boot backend + React frontend.  
Upload courses, instructors, rooms, and sections — let the Genetic Algorithm find the optimal schedule.

<br/>

[![Backend](https://img.shields.io/badge/Backend-Spring_Boot_3.2-6DB33F?style=flat-square&logo=springboot)](./backend)
[![Frontend](https://img.shields.io/badge/Frontend-React_18-61DAFB?style=flat-square&logo=react)](./frontend)
[![Java](https://img.shields.io/badge/Java_21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](./backend)
[![TypeScript](https://img.shields.io/badge/TypeScript_5.6-3178C6?style=flat-square&logo=typescript&logoColor=white)](./frontend)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker)](.)
[![CI](https://img.shields.io/badge/CI-GitHub_Actions-2088FF?style=flat-square&logo=githubactions)](.github/workflows/ci.yml)

</div>

---

## 📁 Project Structure

```
time-table/
├── backend/             # Spring Boot REST API (Java 21, Maven)
│   ├── src/             # Controllers, services, entities, GA engine
│   ├── Dockerfile
│   ├── pom.xml
│   └── README.md
├── frontend/            # React SPA (TypeScript, Vite, Tailwind)
│   ├── src/             # Pages, components, store, API client
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── package.json
│   └── README.md
├── .github/workflows/   # CI pipeline (build + test + lint)
├── docker-compose.yml   # MySQL + Backend + Frontend
└── README.md            # You are here
```

| Module | Stack | Port | Description |
|--------|-------|------|-------------|
| **Backend** | Java 21, Spring Boot 3.2, MySQL 8 | `:8081` | REST API with Genetic Algorithm scheduling |
| **Frontend** | React 18, TypeScript, Vite, Tailwind CSS 4 | `:5173` | Administrative SPA with dark mode |

---

## ✨ Features

### Backend (`./backend`)
- **Genetic Algorithm** — Tournament selection, single-point crossover, random mutation, elitism, early stopping
- **7 hard + 5 soft constraints** — Room conflict, instructor overlap, capacity, time overlap, section duplication, student conflict, room type matching
- **Schedule lifecycle** — DRAFT → VALIDATED → PUBLISHED → LOCKED with status transition guards
- **JWT authentication** — ADMIN / SCHEDULER / INSTRUCTOR roles + role-based endpoint protection
- **Full CRUD** — courses, instructors, rooms, sections, semesters, time slots, departments, **students**, **enrollments**
- **Instructor availability** — Per-instructor unavailable time slots with ADMIN override
- **Analytics API** — Room utilization, instructor workload, conflict metrics, efficiency scoring
- **Premium PDF export** — A4 landscape, color-coded per year-level, AM/PM, legend, page numbers
- **PNG schedule export** — NAVY / BLACK theming options
- **Excel export** — Multi-sheet, freeze pane, colors, auto-size columns
- **Profile & password management** — Authenticated user endpoints
- **Async generation** — Non-blocking job with status polling
- **Consistent error responses** — Uniform `ErrorResponse` shape across all endpoints
- **OpenAPI / Swagger** — Fully documented API with security scheme
- **Dev profile** — Debug logging, detailed stacktraces

### Frontend (`./frontend`)
- **20+ pages** — Landing, auth, dashboard, CRUD (courses, departments, instructors, rooms, sections, semesters, time slots, **students**, **enrollments**), schedule generator, weekly view, **schedules list**, instructor schedule, **analytics**, settings
- **JWT-based auth** — Auto-expiry check, 401 interceptor, role-based route guarding (`RoleGuard`)
- **Role-aware UI** — Sidebar and routes adapt to ADMIN / SCHEDULER / INSTRUCTOR roles
- **Real analytics dashboard** — Room utilization charts, instructor workload, KPIs with live data
- **Instructor availability modal** — Day-grouped slot grid with toggle buttons per instructor
- **Schedule list / history** — Browse all generated schedules, delete DRAFTs, download PDF/Excel
- **Real students CRUD** — Student form with user + department selectors
- **Enrollments CRUD** — Student-section assignment with course/status/grade
- **Settings page** — Profile editing, password change wired to real API
- **Export toolbar** — Theme toggle (NAVY / BLACK) + year dropdown + PDF / Excel / PNG export
- **Interactive weekly grid** — Color-coded cells by session type, conflict indicators (red/yellow dots), click for details, filterable by year/department
- **Dark/light theme** — Custom CSS variables, noise overlay, refined dark mode palette
- **Responsive layout** — 220px sidebar → mobile overlay, horizontal scroll on grid
- **Animated page transitions** — Framer Motion fade + slide, `prefers-reduced-motion` respected
- **Form validation** — Zod schemas + React Hook Form with type-safe resolvers
- **Route-level code splitting** — `React.lazy()` + `<Suspense>` for all pages (36% smaller main bundle)
- **Optimized rendering** — `React.memo` on ScheduleCell and WeeklyGrid
- **Accessibility** — Skip link, ARIA labels, `aria-live` regions, focus indicators, semantic dialog
- **Skeleton loading states** — Animated pulse placeholders on all CRUD pages
- **Empty states** — Consistent empty state component with icon + action button
- **Custom modal confirmations** — Replace `window.confirm()` with accessible `ConfirmModal`
- **Toast notifications** — Success / error feedback on all mutations

---

## 🐳 Docker Deployment

```bash
docker-compose up --build
```

Starts three services:
| Service | Image | Port |
|---------|-------|------|
| **MySQL** | `mysql:8.0` | `:3306` |
| **Backend** | Custom (`Dockerfile`) | `:8080` |
| **Frontend** | Custom (Nginx) | `:80` |

Nginx serves the React SPA and proxies `/api` requests to the backend.

---

## 🚀 Quick Start (Local)

```bash
# Backend
cd backend
mvn clean install
mvn spring-boot:run          # → http://localhost:8081

# Frontend (separate terminal)
cd frontend
npm install
npm run dev                   # → http://localhost:5173
```

Default admin: `admin@uni.com` / `admin123`

---

## 🧪 Testing

```bash
# Backend
cd backend && mvn test

# Frontend
cd frontend && npm test
```

### CI Pipeline (`.github/workflows/ci.yml`)
- **Backend**: JDK 21 → compile → test
- **Frontend**: Node 22 → install → lint → typecheck → test → build

---

<div align="center">
  <sub>Graduation Project 2026 — Mahmoud Youssef · El Shorouk Academy</sub>
  <br/>
  <a href="https://github.com/MahmoudYoussef-web"><img src="https://img.shields.io/badge/GitHub-MahmoudYoussef--web-181717?style=flat-square&logo=github"/></a>
</div>
