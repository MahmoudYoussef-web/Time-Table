import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard, BookOpen, Users, DoorOpen, GraduationCap,
  Zap, Clock, Building2, Layers, LogOut, Calendar,
} from 'lucide-react';
import { useAuthStore } from '../../store/authStore';

type NavGroup = {
  label: string;
  items: { icon: React.ElementType; label: string; to: string }[];
}[];

const navGroups: NavGroup = [
  {
    label: 'OVERVIEW',
    items: [
      { icon: LayoutDashboard, label: 'Dashboard', to: '/dashboard' },
    ],
  },
  {
    label: 'ACADEMIC DATA',
    items: [
      { icon: BookOpen,     label: 'Courses',     to: '/courses' },
      { icon: Building2,    label: 'Departments',  to: '/departments' },
      { icon: Users,        label: 'Instructors',  to: '/lecturers' },
      { icon: DoorOpen,     label: 'Rooms',        to: '/rooms' },
      { icon: Layers,       label: 'Sections',     to: '/sections' },
      { icon: Clock,        label: 'Semesters',    to: '/semesters' },
      { icon: Clock,        label: 'Time Slots',   to: '/timeslots' },
    ],
  },
  {
    label: 'SCHEDULING',
    items: [
      { icon: Zap,          label: 'Generate',     to: '/generate' },
      { icon: Calendar,     label: 'Weekly View',  to: '/schedules' },
    ],
  },
];

export function Sidebar() {
  const logout = useAuthStore((s) => s.logout);
  const user = useAuthStore((s) => s.user);

  return (
    <aside className="w-[220px] h-full bg-[--sidebar] border-r border-[--sidebar-border] flex flex-col flex-shrink-0" style={{ borderRightWidth: '0.5px' }}>
      <div className="pt-6">
        <div className="px-5 mb-6">
          <div className="flex items-center gap-3">
            <div className="w-[30px] h-[30px] bg-[--foreground] flex items-center justify-center flex-shrink-0 rounded-[--radius-sm]">
              <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                <rect x="2" y="2" width="14" height="14" rx="1.5" stroke="var(--primary-foreground)" strokeWidth="1.1" fill="none" />
                <line x1="2" y1="6.5" x2="16" y2="6.5" stroke="var(--primary-foreground)" strokeWidth="1.1" />
                <line x1="9" y1="2" x2="9" y2="16" stroke="var(--primary-foreground)" strokeWidth="1.1" />
                <line x1="6" y1="6.5" x2="6" y2="16" stroke="var(--primary-foreground)" strokeWidth="0.7" />
                <line x1="12" y1="6.5" x2="12" y2="16" stroke="var(--primary-foreground)" strokeWidth="0.7" />
              </svg>
            </div>
            <span className="label-md font-semibold">CampusGrid</span>
          </div>
        </div>

        <nav className="flex flex-col gap-5 px-2">
          {navGroups.map((group) => (
            <div key={group.label}>
              <span className="overline px-5 block mb-1.5">{group.label}</span>
              <div className="flex flex-col gap-0.5">
                {group.items.map(({ icon: Icon, label, to }) => (
                  <NavLink
                    key={to}
                    to={to}
                    className={({ isActive }) =>
                      `flex items-center gap-3 px-5 py-2.5 rounded-[--radius-sm] text-sm transition-colors duration-150 ${
                        isActive
                          ? 'bg-[--muted] text-[--foreground] font-medium'
                          : 'text-[--muted-foreground] hover:text-[--foreground] hover:bg-[--muted]/50'
                      }`
                    }
                  >
                    <Icon size={18} />
                    {label}
                  </NavLink>
                ))}
              </div>
            </div>
          ))}
        </nav>
      </div>

      <div className="flex-1" />

      <div className="border-t border-[--sidebar-border] px-2 py-3" style={{ borderTopWidth: '0.5px' }}>
        {user && (
          <div className="px-5 pb-2 label-sm text-[--muted-foreground] truncate">{user.email}</div>
        )}
        <button
          onClick={logout}
          className="flex items-center gap-3 px-5 py-2.5 w-full text-sm text-[--muted-foreground] rounded-[--radius-sm] hover:text-[--destructive] transition-colors cursor-pointer"
        >
          <LogOut size={18} />
          Logout
        </button>
      </div>
    </aside>
  );
}
