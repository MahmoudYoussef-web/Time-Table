import { useEffect, useState } from 'react';
import { Moon, Sun, Menu } from 'lucide-react';
import { useAuthStore } from '../../store/authStore';

interface TopbarProps {
  onMenuToggle?: () => void;
}

export function Topbar({ onMenuToggle }: TopbarProps) {
  const [dark, setDark] = useState(false);
  const user = useAuthStore((s) => s.user);

  useEffect(() => {
    const saved = localStorage.getItem('theme')
      ?? (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
    setDark(saved === 'dark');
    if (saved === 'dark') document.documentElement.classList.add('dark');
  }, []);

  const toggleDark = () => {
    const next = !dark;
    setDark(next);
    document.documentElement.classList.toggle('dark', next);
    localStorage.setItem('theme', next ? 'dark' : 'light');
  };

  return (
    <header className="h-14 bg-[--background] border-b border-[--border] flex items-center justify-between px-6">
      <div className="flex items-center gap-3">
        <button onClick={onMenuToggle} className="md:hidden cursor-pointer">
          <Menu size={20} />
        </button>
        <span className="label-md">{'Timetable Scheduler'}</span>
      </div>
      <div className="flex items-center gap-3">
        <span className="label-sm text-[--muted-foreground]">{user?.email}</span>
        <button onClick={toggleDark} className="cursor-pointer hover:opacity-70">
          {dark ? <Sun size={18} /> : <Moon size={18} />}
        </button>
      </div>
    </header>
  );
}
