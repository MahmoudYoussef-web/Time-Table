# Frontend Implementation Plan

> Based on: `react-component-refactor`, `vercel-react-best-practices`, `web-design-guidelines`, `design-systems`, `color-mode-and-theme`

---

## Overview

| Phase | Effort | Impact | Area |
|-------|--------|--------|------|
| 1 | ⚡ Quick Wins | High | Critical fixes, dead code, broken links |
| 2 | 🧹 Technical Debt | High | Component refactoring, DRY, dead code removal |
| 3 | 🚀 New Features | High | PNG export, Enrollments, Schedule list |
| 4 | 🎨 UI Polish | Medium | Accessibility, responsive, animations |
| 5 | ⚡ Performance | Medium | Code splitting, React Query, lazy loading |

---

## Phase 1 — Quick Wins (P0)

### 1.1 Fix Broken CSS Classes
**Files:** `src/index.css`
- Add `headline-lg` and `headline-md` typography classes
- Currently referenced but undefined — pages break silently
- **Skill:** `web-design-guidelines` → `a11y-color-contrast`

### 1.2 Fix Sidebar "Weekly View" Link
**File:** `src/components/layout/Sidebar.tsx`
- Links to `/schedules` → 404 → redirects to `/dashboard`
- **Fix:** Remove the link or change to `/schedules/:id/weekly` with the latest schedule ID from a new store/context
- **Skill:** `react-component-refactor` → analyze component

### 1.3 Remove Dead Code
**Files to delete:**
- `LoginPage.tsx` — replaced by `AuthPage.tsx`
- `InstructorsPage.tsx` — duplicate of `LecturersPage.tsx`
- `Topbar.tsx` — not used anywhere
- **Skill:** `react-component-refactor` → clean up

### 1.4 Add Export PNG to Frontend
**Files:** `src/api/schedules.ts`, `src/components/schedule/WeeklyGrid.tsx`, `src/pages/ScheduleGeneratorPage.tsx`
- Backend has: `GET /api/schedules/{id}/year/{year}/png?theme=NAVY|BLACK`
- Frontend missing: no `downloadPng()`, no button
- **Add:** `downloadPng(scheduleId, year, theme)` in `schedules.ts`
- **Add:** Download button in `WeeklyGrid` and `ScheduleGeneratorPage`
- **Skill:** `vercel-react-best-practices` → `async-parallel` (parallel downloads)

### 1.5 Add Year + Theme Params to PDF/Excel Exports
**Files:** `src/api/schedules.ts`, `WeeklyGrid.tsx`, `ScheduleGeneratorPage.tsx`
- Current: `GET /api/schedules/{id}/pdf` (no params, NAVY only)
- Backend now supports: `?theme=NAVY|BLACK` and `/year/{year}/pdf`
- **Add:** Year picker + theme toggle (NAVY/BLACK) in UI
- **Update:** `downloadPdf()`, `downloadExcel()` signatures to accept `year` and `theme`
- **Skill:** `design-systems` → consistent component props

---

## Phase 2 — Technical Debt (P1)

### 2.1 Integrate `ScheduleCell.tsx` into `WeeklyGrid`
**Files:** `src/components/schedule/`
- `ScheduleCell` exists but `WeeklyGrid` renders cells inline
- **Fix:** Make `WeeklyGrid` use `ScheduleCell` component
- **Skill:** `react-component-refactor` → extract sub-components

### 2.2 Integrate `GenerationStatus.tsx` into `ScheduleGeneratorPage`
- Duplicate UI: component exists but page has its own inline version
- **Fix:** Make the page use the shared component, or delete the component
- **Skill:** `react-component-refactor` → DRY

### 2.3 Refactor `RoomsPage` to Use `RoomForm.tsx`
**File:** `src/pages/RoomsPage.tsx`
- Has its own inline form instead of using the existing `RoomForm` component
- **Fix:** Replace inline form with `<RoomForm />`
- **Skill:** `react-component-refactor` → extract

### 2.4 Add Role-Based Route Guarding
**Files:** `src/App.tsx`, `src/store/authStore.ts`
- JWT has `role` field but frontend never reads it
- Instructor-only pages (`/instructor/schedule`) accessible by anyone
- **Fix:** Check `role` in `ProtectedRoute`, restrict routes
- **Skill:** `web-design-guidelines` → `a11y-screen-reader` + security

### 2.5 Replace `window.confirm()` with Custom Modal
**File:** All CRUD pages
- Uses browser native `confirm()` dialog
- **Fix:** Use the existing `Modal` component instead
- **Skill:** `web-design-guidelines` → `a11y-keyboard-nav`

### 2.6 Add Loading States to All CRUD Pages
**Files:** All entity pages
- Some pages show "Loading..." text instead of skeleton/spinner
- **Fix:** Use `<Spinner />` or skeleton placeholders consistently
- **Skill:** `vercel-react-best-practices` → `rendering-content-visibility`

---

## Phase 3 — New Features (P2)

### 3.1 Implement Real Students Page
**Files:** `src/api/students.ts` (new), `src/pages/StudentsPage.tsx` (rewrite)
- Currently 10 hardcoded students with local search
- Backend has student endpoints — check availability
- **Add:** Full CRUD with `Table` + form
- **Skill:** `react-component-refactor` → replicate existing CRUD pattern

### 3.2 Build Enrollments Page
**Files:** `src/api/enrollments.ts` (new), `src/pages/EnrollmentsPage.tsx` (new), `src/App.tsx`
- Entity exists in types/index.ts
- Backend has enrollment endpoints
- **Add:** Route + full CRUD
- **Skill:** `design-systems` → consistent with other CRUD pages

### 3.3 Add Schedule List/History Page
**Files:** `src/pages/SchedulesPage.tsx` (new), `src/api/schedules.ts` (new endpoint)
- No way to browse all generated schedules
- **Add:** `GET /api/schedules` endpoint + `SchedulesPage` with table listing all schedules
- **Skill:** `vercel-react-best-practices` → `server-parallel-fetching`

### 3.4 Wire Settings Page to Backend
**File:** `src/pages/SettingsPage.tsx`
- Profile, notifications, security — all UI-only
- **Add:** API calls to save settings
- **Skill:** `web-design-guidelines` → `form-submit-feedback`

### 3.5 Wire Analytics Page to Real Data
**File:** `src/pages/AnalyticsPage.tsx`
- All KPIs and charts are hardcoded
- **Add:** Backend analytics endpoint → real data
- **Skill:** `color-mode-and-theme` → dark mode for charts

---

## Phase 4 — UI Polish & Accessibility (P3)

### 4.1 Accessibility Audit
**Files:** All UI components
- Run `web-design-guidelines` checklist:
  - ✅ Skip link to bypass navigation
  - ✅ ARIA labels on icon-only buttons
  - ✅ Form error messages linked via `aria-describedby`
  - ✅ Focus indicators visible on all elements
  - ✅ `prefers-reduced-motion` respected
  - ✅ Heading hierarchy sequential
- **Skill:** `web-design-guidelines` → full checklist

### 4.2 Improve Dark Mode
**Files:** `src/index.css`, all components
- Current dark mode uses direct inversion — can be improved
- Apply `color-mode-and-theme` principles:
  - Surface hierarchy via lightness, not shadows
  - Reduce brand color saturation in dark mode
  - Increase font weight for light-on-dark text
  - No pure black (`#000000`) for surfaces
- **Skill:** `color-mode-and-theme` → dark mode tokens

### 4.3 Add Responsive Improvements
**Files:** `src/components/layout/AppShell.tsx`, `src/components/schedule/WeeklyGrid.tsx`
- Sidebar already collapses on mobile
- Weekly grid needs horizontal scroll on small screens
- **Skill:** `responsive-design` (available but expert in related workflows)

### 4.4 Add Skeleton Loading States
**Files:** All CRUD pages
- Replace "Loading..." text with animated skeleton placeholders
- Match the existing Dashboard skeleton pattern
- **Skill:** `vercel-react-best-practices` → `rendering-content-visibility`

---

## Phase 5 — Performance (P4)

### 5.1 Add Route-Level Code Splitting
**File:** `src/App.tsx`
- All routes eagerly imported
- **Fix:** Use `React.lazy()` + `<Suspense>` for each page
- **Skill:** `vercel-react-best-practices` → `bundle-dynamic-imports`

### 5.2 Adopt TanStack Query
**Files:** All CRUD pages
- Replace manual `useEffect` + `useState` data fetching
- Benefits: caching, deduplication, background refetch, stale-while-revalidate
- **Skill:** `vercel-react-best-practices` → `client-swr-dedup`

### 5.3 Add Pagination / Virtual Scrolling
**Files:** `src/components/ui/Table.tsx`
- All tables load all records — breaks with large datasets
- **Option A:** Server-side pagination with page controls
- **Option B:** Client-side virtual scrolling via `@tanstack/react-virtual`
- **Skill:** `vercel-react-best-practices` → `js-min-max-loop`

### 5.4 Memoize Expensive Components
**Files:** `WeeklyGrid.tsx`, `ScheduleCell.tsx`
- Add `React.memo()` where re-renders are expensive
- Fix `useCallback`/`useMemo` dependency arrays
- **Skill:** `vercel-react-best-practices` → `rerender-memo`

---

## Implementation Order

```
Phase 1 ─► Phase 2 ─► Phase 3 ─► Phase 4 ─► Phase 5
  (now)     (next)     (soon)     (later)    (eventually)
```

### Week 1: Phase 1 + Phase 2
- Fix broken CSS, sidebar link, dead code
- Add PNG export + theme/year to exports
- Integrate unused components, refactor RoomsPage
- Add role-based guarding

### Week 2: Phase 3
- Real Students page
- Enrollments CRUD
- Schedule list/history
- Wire Settings + Analytics

### Week 3: Phase 4
- Accessibility audit + fixes
- Dark mode refinement
- Responsive improvements
- Skeleton loading states

### Week 4: Phase 5 (optional)
- Code splitting
- TanStack Query migration
- Pagination
- Memo optimization

---

## Skills Used

| Skill | Applied In |
|-------|-----------|
| `react-component-refactor` | Phase 2 — integrate ScheduleCell, GenerationStatus, RoomForm |
| `vercel-react-best-practices` | Phase 1 (async-parallel), Phase 3 (server-parallel-fetching), Phase 5 (bundle-dynamic-imports, rerender-memo) |
| `web-design-guidelines` | Phase 4 — accessibility audit, form patterns, keyboard nav |
| `design-systems` | Phase 1 (consistent props), Phase 3 (consistent CRUD patterns) |
| `color-mode-and-theme` | Phase 4 — dark mode token refinement |
