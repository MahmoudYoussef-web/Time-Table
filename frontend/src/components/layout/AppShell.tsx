import { useEffect, useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { Menu, Sun, Moon } from 'lucide-react';
import { Sidebar } from './Sidebar';
import { useAuthStore } from '../../store/authStore';

const pageTitles: Record<string, string> = {
  '/dashboard': 'Dashboard',
  '/courses': 'Courses',
  '/departments': 'Departments',
  '/lecturers': 'Instructors',
  '/rooms': 'Rooms',
  '/sections': 'Sections',
  '/semesters': 'Semesters',
  '/timeslots': 'Time Slots',
  '/generate': 'Schedule Generator',
  '/analytics': 'Analytics',
  '/settings': 'Settings',
};

function matchRoute(pathname: string): string {
  if (pageTitles[pathname]) return pageTitles[pathname];
  if (pathname.startsWith('/schedules/')) return 'Weekly Schedule';
  if (pathname.startsWith('/instructor/')) return 'My Schedule';
  return 'Untitled';
}

export function AppShell() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [dark, setDark] = useState(false);
  const location = useLocation();
  const user = useAuthStore((s) => s.user);

  useEffect(() => {
    const saved = localStorage.getItem('theme')
      ?? (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
    setDark(saved === 'dark');
    if (saved === 'dark') document.documentElement.classList.add('dark');
    else document.documentElement.classList.remove('dark');
  }, []);

  const toggleDark = () => {
    const next = !dark;
    setDark(next);
    document.documentElement.classList.toggle('dark', next);
    localStorage.setItem('theme', next ? 'dark' : 'light');
  };

  const pageTitle = matchRoute(location.pathname);

  return (
    <div className="flex h-screen overflow-hidden">
      <div className="hidden md:block">
        <Sidebar />
      </div>

      {sidebarOpen && (
        <div className="fixed inset-0 z-40 md:hidden">
          <div className="fixed inset-0 bg-black/20" onClick={() => setSidebarOpen(false)} />
          <div className="fixed left-0 top-0 h-full z-50">
            <Sidebar />
          </div>
        </div>
      )}

      <div className="flex flex-col flex-1 min-w-0">
        <header className="h-14 border-b border-[--border] flex items-center justify-between px-6 bg-[--background]" style={{ borderBottomWidth: '0.5px' }}>
          <div className="flex items-center gap-3">
            <button onClick={() => setSidebarOpen((p) => !p)} className="md:hidden cursor-pointer" aria-label="Toggle sidebar">
              <Menu size={20} />
            </button>
            <span className="display-sm">{pageTitle}</span>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={toggleDark}
              className="w-8 h-8 flex items-center justify-center cursor-pointer hover:bg-[--muted] rounded-[--radius-sm] transition-colors"
              aria-label="Toggle dark mode"
            >
              {dark ? <Sun size={15} /> : <Moon size={15} />}
            </button>
            {user && (
              <span className="label-sm text-[--muted-foreground]">{user.email}</span>
            )}
          </div>
        </header>

        <main id="main-content" className="flex-1 overflow-y-auto p-6 md:p-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
