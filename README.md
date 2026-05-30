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

</div>

---

## 📁 Project Structure

```
time-table/
├── backend/           # Spring Boot REST API (Java 21, Maven)
│   ├── src/           # Controllers, services, entities, GA engine
│   ├── pom.xml
│   └── README.md
├── frontend/          # React SPA (TypeScript, Vite, Tailwind)
│   ├── src/           # Pages, components, store, API client
│   ├── package.json
│   └── README.md
└── README.md          # You are here
```

| Module | Stack | Port | Description |
|--------|-------|------|-------------|
| **Backend** | Java 21, Spring Boot 3.2, MySQL 8 | `:8081` | REST API with Genetic Algorithm scheduling |
| **Frontend** | React 18, TypeScript, Vite, Tailwind CSS 4 | `:5173` | Administrative SPA with dark mode |

---

## ✨ Features

### Backend (`./backend`)
- Genetic Algorithm engine with 6 hard + 5 soft constraints
- JWT authentication with ADMIN / SCHEDULER / INSTRUCTOR roles
- Full CRUD: courses, instructors, rooms, sections, semesters, time slots, departments
- Premium PDF export (A4 landscape, color-coded, per year-level)
- Excel export (multi-sheet, freeze pane, colors)
- Async schedule generation with job polling

### Frontend (`./frontend`)
- 18 pages: landing, auth, dashboard, CRUD for all 7 entities, schedule generator, weekly view, analytics
- JWT-based auth with auto-expiry check and 401 redirect
- Dark/light theme with noise overlay and custom CSS variables
- Responsive layout with collapsible sidebar
- Form validation via Zod + React Hook Form
- Animated page transitions (Framer Motion)
- Real-time schedule generation status polling
- Weekly timetable grid filterable by year/department
- PDF/Excel download integration

---

## 🚀 Quick Start

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

<div align="center">
  <sub>Graduation Project 2026 — Mahmoud Youssef · El Shorouk Academy</sub>
  <br/>
  <a href="https://github.com/MahmoudYoussef-web"><img src="https://img.shields.io/badge/GitHub-MahmoudYoussef--web-181717?style=flat-square&logo=github"/></a>
</div>
