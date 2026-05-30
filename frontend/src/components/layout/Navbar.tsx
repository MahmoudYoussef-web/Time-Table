import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Sun, Moon, User, Calendar } from 'lucide-react';

export function Navbar() {
  const navigate = useNavigate();
  const [dark, setDark] = useState(() => document.documentElement.classList.contains('dark'));

  const toggleDark = () => {
    const next = !dark;
    setDark(next);
    document.documentElement.classList.toggle('dark', next);
    localStorage.setItem('theme', next ? 'dark' : 'light');
  };

  return (
    <header className="sticky top-0 z-50 h-16 flex items-center justify-between px-6 md:px-10 bg-[--background]/80 backdrop-blur-md border-b border-[--border]" style={{ borderBottomWidth: '0.5px' }}>
      <div className="flex items-center gap-3 cursor-pointer group" onClick={() => navigate('/')}>
        <div className="w-[30px] h-[30px] bg-[--foreground] flex items-center justify-center flex-shrink-0 transition-transform group-hover:scale-105 rounded-[--radius-sm]">
          <Calendar size={16} className="text-[--background]" />
        </div>
        <div>
          <div className="text-sm font-semibold tracking-tight leading-tight">CampusGrid</div>
          <div className="label-sm text-[--muted-foreground] leading-tight">Academic Scheduling Platform</div>
        </div>
      </div>

      <div className="flex items-center gap-3">
        <button onClick={toggleDark} className="w-8 h-8 flex items-center justify-center cursor-pointer hover:bg-[--muted] rounded-[--radius-sm] transition-colors" aria-label="Toggle dark mode">
          {dark ? <Sun size={15} /> : <Moon size={15} />}
        </button>
        <button
          onClick={() => navigate('/auth')}
          className="w-8 h-8 flex items-center justify-center cursor-pointer hover:bg-[--muted] rounded-[--radius-sm] transition-colors"
          aria-label="Profile"
        >
          <User size={16} />
        </button>
      </div>
    </header>
  );
}
