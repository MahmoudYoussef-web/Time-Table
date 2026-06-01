import { useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AnimatePresence, motion } from 'framer-motion';
import { useAuthStore } from './store/authStore';
import { parseJwt } from './lib/utils';
import { ErrorBoundary } from './components/ErrorBoundary';
import { HomePage } from './pages/HomePage';
import { AuthPage } from './pages/AuthPage';
import { AppShell } from './components/layout/AppShell';
import { DashboardPage } from './pages/DashboardPage';
import { CoursesPage } from './pages/CoursesPage';
import { LecturersPage } from './pages/LecturersPage';
import { RoomsPage } from './pages/RoomsPage';
import { StudentsPage } from './pages/StudentsPage';
import { AnalyticsPage } from './pages/AnalyticsPage';
import { SettingsPage } from './pages/SettingsPage';
import { ScheduleGeneratorPage } from './pages/ScheduleGeneratorPage';
import { SemestersPage } from './pages/SemestersPage';
import { SectionsPage } from './pages/SectionsPage';
import { TimeSlotsPage } from './pages/TimeSlotsPage';
import { DepartmentsPage } from './pages/DepartmentsPage';
import { WeeklySchedulePage } from './pages/WeeklySchedulePage';
import { InstructorSchedulePage } from './pages/InstructorSchedulePage';

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
          <Route path="/dashboard" element={<AnimatedPage><DashboardPage /></AnimatedPage>} />
          <Route path="/courses" element={<AnimatedPage><CoursesPage /></AnimatedPage>} />
          <Route path="/lecturers" element={<AnimatedPage><LecturersPage /></AnimatedPage>} />
          <Route path="/rooms" element={<AnimatedPage><RoomsPage /></AnimatedPage>} />
          <Route path="/students" element={<AnimatedPage><StudentsPage /></AnimatedPage>} />
          <Route path="/analytics" element={<AnimatedPage><AnalyticsPage /></AnimatedPage>} />
          <Route path="/settings" element={<AnimatedPage><SettingsPage /></AnimatedPage>} />
          <Route path="/generate" element={<AnimatedPage><ScheduleGeneratorPage /></AnimatedPage>} />
          <Route path="/semesters" element={<AnimatedPage><SemestersPage /></AnimatedPage>} />
          <Route path="/sections" element={<AnimatedPage><SectionsPage /></AnimatedPage>} />
          <Route path="/timeslots" element={<AnimatedPage><TimeSlotsPage /></AnimatedPage>} />
          <Route path="/departments" element={<AnimatedPage><DepartmentsPage /></AnimatedPage>} />
          <Route path="/schedules/:id/weekly" element={<AnimatedPage><WeeklySchedulePage /></AnimatedPage>} />
          <Route path="/instructor/schedule" element={<AnimatedPage><InstructorSchedulePage /></AnimatedPage>} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Route>
      </Routes>
    </AnimatePresence>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <Toaster
        position="bottom-right"
        toastOptions={{
          style: {
            background: 'var(--card)',
            color: 'var(--card-foreground)',
            border: '1px solid var(--border)',
            borderRadius: 'var(--radius-sm)',
            fontFamily: 'var(--font-body)',
          },
          success: { iconTheme: { primary: '#16a34a', secondary: '#fff' } },
          error: { iconTheme: { primary: '#dc2626', secondary: '#fff' } },
        }}
      />
      <AppRoutes />
    </BrowserRouter>
  );
}
