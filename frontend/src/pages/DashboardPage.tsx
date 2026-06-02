import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowRight } from 'lucide-react';
import { getCourses } from '../api/courses';
import { getRooms } from '../api/rooms';
import { getInstructors } from '../api/instructors';
import { getSemesters } from '../api/semesters';
import { getSections } from '../api/sections';
import { getTimeSlots } from '../api/timeslots';
import { Semester } from '../types';

const container = { hidden: {}, show: { transition: { staggerChildren: 0.07 } } };
const item = { hidden: { opacity: 0, y: 10 }, show: { opacity: 1, y: 0 } };

function greeting(): string {
  const h = new Date().getHours();
  if (h < 12) return 'Good morning';
  if (h < 17) return 'Good afternoon';
  return 'Good evening';
}

export function DashboardPage() {
  const navigate = useNavigate();
  const [stats, setStats] = useState<{ value: string; label: string; to?: string }[]>([]);
  const [loading, setLoading] = useState(true);
  const [publishedSemester, setPublishedSemester] = useState<Semester | null>(null);
  const [timeslotCount, setTimeslotCount] = useState(0);
  const [sectionCount, setSectionCount] = useState(0);

  useEffect(() => {
    Promise.all([
      getCourses(), getRooms(), getInstructors(), getSemesters(), getSections(), getTimeSlots()
    ])
      .then(([courses, rooms, instructors, semesters, sections, timeslots]) => {
        setTimeslotCount(timeslots.length);
        setSectionCount(sections.length);
        const pub = semesters.find((s) => s.status === 'PUBLISHED') ?? null;
        setPublishedSemester(pub);
        setStats([
          { value: String(courses.length), label: 'Courses', to: '/courses' },
          { value: String(rooms.length), label: 'Rooms', to: '/rooms' },
          { value: String(instructors.length), label: 'Instructors', to: '/lecturers' },
          { value: String(sections.length), label: 'Sections', to: '/sections' },
          { value: String(semesters.length), label: 'Semesters', to: '/semesters' },
        ]);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const semesterLabel = publishedSemester
    ? `${publishedSemester.name} · Academic Year ${new Date(publishedSemester.startDate).getFullYear()}–${new Date(publishedSemester.endDate).getFullYear()}`
    : 'No active semester';

  return (
    <div>
      <div className="mb-8">
        <h1 className="display-md">{greeting()}</h1>
        <p className="label-sm text-[--muted-foreground] mt-1">{semesterLabel}</p>
      </div>

      {loading ? (
        <div className="grid sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-4 mb-8">
          {[1,2,3,4,5].map(i => (
            <div key={i} className="bg-[--card] border border-[--border] rounded-[--radius-md] p-6 animate-pulse">
              <div className="w-20 h-4 bg-[--muted] rounded mb-3" />
              <div className="w-12 h-9 bg-[--muted] rounded mb-1" />
              <div className="w-24 h-3 bg-[--muted] rounded" />
            </div>
          ))}
        </div>
      ) : (
        <motion.div variants={container} initial="hidden" animate="show" className="grid sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-4 mb-8">
          {stats.map(({ value, label, to }) => (
            <motion.div
              key={label}
              variants={item}
              onClick={() => to && navigate(to)}
              className="bg-[--card] border border-[--border] rounded-[--radius-md] p-6 cursor-pointer hover:border-[--foreground] transition-colors"
            >
              <span className="overline block mb-2">{label}</span>
              <p className="display-lg">{value}</p>
              <p className="label-sm text-[--muted-foreground] mt-1">Active this semester</p>
            </motion.div>
          ))}
        </motion.div>
      )}

      {/* Suggested Actions */}
      <motion.div
        variants={item}
        initial="hidden"
        animate="show"
        className="bg-[--card] border border-[--border] rounded-[--radius-md] p-6 max-w-[480px]"
      >
        <h2 className="label-md mb-3">SUGGESTED ACTIONS</h2>
        <div className="space-y-0.5">
          {publishedSemester && (
            <button
              onClick={() => navigate('/generate')}
              className="w-full flex items-center gap-3 p-3 rounded-[--radius-sm] hover:bg-[--muted] transition-colors cursor-pointer text-left"
            >
              <ArrowRight size={14} className="text-[--muted-foreground]" />
              <span className="body-md">Generate schedule for {publishedSemester.name}</span>
            </button>
          )}
          {timeslotCount === 0 && (
            <button
              onClick={() => navigate('/timeslots')}
              className="w-full flex items-center gap-3 p-3 rounded-[--radius-sm] hover:bg-[--muted] transition-colors cursor-pointer text-left"
            >
              <ArrowRight size={14} className="text-[--muted-foreground]" />
              <span className="body-md">Add time slots (none defined yet)</span>
            </button>
          )}
          <button
            onClick={() => navigate('/sections')}
            className="w-full flex items-center gap-3 p-3 rounded-[--radius-sm] hover:bg-[--muted] transition-colors cursor-pointer text-left"
          >
            <ArrowRight size={14} className="text-[--muted-foreground]" />
            <span className="body-md">Create sections for new semester</span>
          </button>
          {publishedSemester && sectionCount > 0 && (
            <button
              onClick={() => navigate('/generate')}
              className="w-full flex items-center gap-3 p-3 rounded-[--radius-sm] hover:bg-[--muted] transition-colors cursor-pointer text-left"
            >
              <ArrowRight size={14} className="text-[--muted-foreground]" />
              <span className="body-md">View weekly schedule</span>
            </button>
          )}
        </div>
      </motion.div>
    </div>
  );
}
