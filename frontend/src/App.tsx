import { useEffect, lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { Toaster } from 'sonner';
import { AnimatePresence, motion } from 'framer-motion';
import { useAuthStore } from './store/authStore';
import { parseJwt } from './lib/utils';
import { ErrorBoundary } from './components/ErrorBoundary';
import { RoleGuard } from './components/RoleGuard';
import { HomePage } from './pages/HomePage';
import { AuthPage } from './pages/AuthPage';
import { AppShell } from './components/layout/AppShell';

const DashboardPage = lazy(() => import('./pages/DashboardPage').then(m => ({ default: m.DashboardPage })));
const CoursesPage = lazy(() => import('./pages/CoursesPage').then(m => ({ default: m.CoursesPage })));
const LecturersPage = lazy(() => import('./pages/LecturersPage').then(m => ({ default: m.LecturersPage })));
const RoomsPage = lazy(() => import('./pages/RoomsPage').then(m => ({ default: m.RoomsPage })));
const StudentsPage = lazy(() => import('./pages/StudentsPage').then(m => ({ default: m.StudentsPage })));
const EnrollmentsPage = lazy(() => import('./pages/EnrollmentsPage').then(m => ({ default: m.EnrollmentsPage })));
const SchedulesPage = lazy(() => import('./pages/SchedulesPage').then(m => ({ default: m.SchedulesPage })));
const AnalyticsPage = lazy(() => import('./pages/AnalyticsPage').then(m => ({ default: m.AnalyticsPage })));
const SettingsPage = lazy(() => import('./pages/SettingsPage').then(m => ({ default: m.SettingsPage })));
const ScheduleGeneratorPage = lazy(() => import('./pages/ScheduleGeneratorPage').then(m => ({ default: m.ScheduleGeneratorPage })));
const SemestersPage = lazy(() => import('./pages/SemestersPage').then(m => ({ default: m.SemestersPage })));
const SectionsPage = lazy(() => import('./pages/SectionsPage').then(m => ({ default: m.SectionsPage })));
const TimeSlotsPage = lazy(() => import('./pages/TimeSlotsPage').then(m => ({ default: m.TimeSlotsPage })));
const DepartmentsPage = lazy(() => import('./pages/DepartmentsPage').then(m => ({ default: m.DepartmentsPage })));
const WeeklySchedulePage = lazy(() => import('./pages/WeeklySchedulePage').then(m => ({ default: m.WeeklySchedulePage })));
const ScheduleDetailPage = lazy(() => import('./pages/ScheduleDetailPage').then(m => ({ default: m.ScheduleDetailPage })));
const InstructorSchedulePage = lazy(() => import('./pages/InstructorSchedulePage').then(m => ({ default: m.InstructorSchedulePage })));

function isTokenValid(token: string | null): boolean {
  if (!token) return false;
  const payload = parseJwt(token);
  if (!payload || typeof payload.exp !== 'number') return false;
  return payload.exp * 1000 > Date.now();
}

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const token = useAuthStore((s) => s.token);
  if (!isTokenValid(token)) {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    return <Navigate to="/auth" replace />;
  }
  return <>{children}</>;
}

function AnimatedPage({ children }: { children: React.ReactNode }) {
  return (
    <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -8 }} transition={{ duration: 0.3, ease: 'easeOut' }}>
      {children}
    </motion.div>
  );
}

function PageLoading() {
  return (
    <div className="flex items-center justify-center py-[76px]">
      <div className="w-6 h-6 border-2 border-[--border] border-t-[--foreground] rounded-full animate-spin" />
    </div>
  );
}

const ADMIN_SCHEDULER = ['ADMIN', 'SCHEDULER'];
const ALL_ROLES = ['ADMIN', 'SCHEDULER', 'INSTRUCTOR'];
const ADMIN_INSTRUCTOR = ['ADMIN', 'INSTRUCTOR'];

function AppRoutes() {
  const location = useLocation();
  const initialize = useAuthStore((s) => s.initialize);

  useEffect(() => { initialize(); }, [initialize]);

  return (
    <AnimatePresence mode="wait">
      <Routes location={location} key={location.pathname}>
        <Route path="/" element={<HomePage />} />
        <Route path="/auth" element={<AuthPage />} />

        <Route
          element={
            <ProtectedRoute>
              <ErrorBoundary>
                <AppShell />
              </ErrorBoundary>
            </ProtectedRoute>
          }
        >
          <Route path="/dashboard" element={<Suspense fallback={<PageLoading />}><AnimatedPage><DashboardPage /></AnimatedPage></Suspense>} />
          <Route path="/courses" element={<Suspense fallback={<PageLoading />}><RoleGuard allowedRoles={ADMIN_SCHEDULER}><AnimatedPage><CoursesPage /></AnimatedPage></RoleGuard></Suspense>} />
          <Route path="/lecturers" element={<Suspense fallback={<PageLoading />}><RoleGuard allowedRoles={ADMIN_SCHEDULER}><AnimatedPage><LecturersPage /></AnimatedPage></RoleGuard></Suspense>} />
          <Route path="/rooms" element={<Suspense fallback={<PageLoading />}><RoleGuard allowedRoles={ADMIN_SCHEDULER}><AnimatedPage><RoomsPage /></AnimatedPage></RoleGuard></Suspense>} />
          <Route path="/students" element={<Suspense fallback={<PageLoading />}><RoleGuard allowedRoles={ADMIN_SCHEDULER}><AnimatedPage><StudentsPage /></AnimatedPage></RoleGuard></Suspense>} />
          <Route path="/enrollments" element={<Suspense fallback={<PageLoading />}><RoleGuard allowedRoles={ADMIN_SCHEDULER}><AnimatedPage><EnrollmentsPage /></AnimatedPage></RoleGuard></Suspense>} />
          <Route path="/schedules" element={<Suspense fallback={<PageLoading />}><RoleGuard allowedRoles={ADMIN_SCHEDULER}><AnimatedPage><SchedulesPage /></AnimatedPage></RoleGuard></Suspense>} />
          <Route path="/analytics" element={<Suspense fallback={<PageLoading />}><RoleGuard allowedRoles={ALL_ROLES}><AnimatedPage><AnalyticsPage /></AnimatedPage></RoleGuard></Suspense>} />
          <Route path="/settings" element={<Suspense fallback={<PageLoading />}><RoleGuard allowedRoles={ALL_ROLES}><AnimatedPage><SettingsPage /></AnimatedPage></RoleGuard></Suspense>} />
          <Route path="/generate" element={<Suspense fallback={<PageLoading />}><RoleGuard allowedRoles={ADMIN_SCHEDULER}><AnimatedPage><ScheduleGeneratorPage /></AnimatedPage></RoleGuard></Suspense>} />
          <Route path="/semesters" element={<Suspense fallback={<PageLoading />}><RoleGuard allowedRoles={ADMIN_SCHEDULER}><AnimatedPage><SemestersPage /></AnimatedPage></RoleGuard></Suspense>} />
          <Route path="/sections" element={<Suspense fallback={<PageLoading />}><RoleGuard allowedRoles={ADMIN_SCHEDULER}><AnimatedPage><SectionsPage /></AnimatedPage></RoleGuard></Suspense>} />
          <Route path="/timeslots" element={<Suspense fallback={<PageLoading />}><RoleGuard allowedRoles={ADMIN_SCHEDULER}><AnimatedPage><TimeSlotsPage /></AnimatedPage></RoleGuard></Suspense>} />
          <Route path="/departments" element={<Suspense fallback={<PageLoading />}><RoleGuard allowedRoles={ADMIN_SCHEDULER}><AnimatedPage><DepartmentsPage /></AnimatedPage></RoleGuard></Suspense>} />
          <Route path="/schedules/:id/weekly" element={<Suspense fallback={<PageLoading />}><RoleGuard allowedRoles={ADMIN_SCHEDULER}><AnimatedPage><WeeklySchedulePage /></AnimatedPage></RoleGuard></Suspense>} />
          <Route path="/schedules/:id" element={<Suspense fallback={<PageLoading />}><RoleGuard allowedRoles={ADMIN_SCHEDULER}><AnimatedPage><ScheduleDetailPage /></AnimatedPage></RoleGuard></Suspense>} />
          <Route path="/instructor/schedule" element={<Suspense fallback={<PageLoading />}><RoleGuard allowedRoles={ADMIN_INSTRUCTOR}><AnimatedPage><InstructorSchedulePage /></AnimatedPage></RoleGuard></Suspense>} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Route>
      </Routes>
    </AnimatePresence>
  );
}

function SkipLink() {
  return (
    <a href="#main-content" className="sr-only focus:not-sr-only focus:fixed focus:top-2 focus:left-2 focus:z-[9999] focus:bg-[--background] focus:text-[--foreground] focus:p-3 focus:rounded-[--radius-sm] focus:border focus:border-[--border] focus:outline-none">
      Skip to main content
    </a>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <SkipLink />
      <Toaster
        position="bottom-right"
        richColors
        closeButton
        toastOptions={{
          style: {
            background: 'var(--card)',
            color: 'var(--card-foreground)',
            border: '1px solid var(--border)',
            fontFamily: 'var(--font-body)',
          },
        }}
      />
      <AppRoutes />
    </BrowserRouter>
  );
}
