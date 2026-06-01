import { motion } from 'framer-motion';
import { CheckCircle, Building2, Users, TrendingUp } from 'lucide-react';

const kpis = [
  { icon: CheckCircle, value: '97%', label: 'Conflict-Free', accent: true },
  { icon: Building2, value: '89%', label: 'Room Utilization' },
  { icon: Users, value: '93%', label: 'Lecturer Satisfaction' },
  { icon: TrendingUp, value: '94%', label: 'Efficiency' },
];

const roomData = [
  { label: 'Hall A', value: 92 },
  { label: 'Hall B', value: 78 },
  { label: 'Lab 1', value: 95 },
  { label: 'Lab 2', value: 64 },
  { label: 'Seminar 1', value: 81 },
  { label: 'Seminar 2', value: 70 },
];

const lecturerWorkload = [
  { name: 'Dr. Ahmed', load: 85, courses: 4 },
  { name: 'Prof. Lisa', load: 60, courses: 3 },
  { name: 'Dr. Chen', load: 92, courses: 5 },
  { name: 'Dr. Patel', load: 45, courses: 2 },
  { name: 'Prof. Omar', load: 75, courses: 3 },
];

const timelineSteps = [
  { week: 'Week 1', conflicts: 24, efficiency: 72 },
  { week: 'Week 2', conflicts: 16, efficiency: 80 },
  { week: 'Week 3', conflicts: 10, efficiency: 85 },
  { week: 'Week 4', conflicts: 4, efficiency: 94 },
];

const container = { hidden: {}, show: { transition: { staggerChildren: 0.06 } } };
const item = { hidden: { opacity: 0, y: 12 }, show: { opacity: 1, y: 0 } };

function ConflictRing({ pct, label }: { pct: number; label: string }) {
  const circumference = 2 * Math.PI * 32;
  const offset = circumference - (pct / 100) * circumference;
  return (
    <div className="flex flex-col items-center gap-2">
      <svg width="80" height="80" viewBox="0 0 80 80" className="rotate-[-90deg]">
        <circle cx="40" cy="40" r="32" fill="none" stroke="var(--muted)" strokeWidth="5" />
        <circle cx="40" cy="40" r="32" fill="none" stroke="var(--foreground)" strokeWidth="5" strokeDasharray={circumference} strokeDashoffset={offset} strokeLinecap="round" />
      </svg>
      <span className="text-lg font-bold">{pct}%</span>
      <span className="label-xs text-[--muted-foreground] text-center">{label}</span>
    </div>
  );
}

export function AnalyticsPage() {
  const maxRoom = Math.max(...roomData.map((d) => d.value));

  return (
    <div>
      <h1 className="headline-lg mb-6">Analytics</h1>

      <motion.div variants={container} initial="hidden" animate="show" className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        {kpis.map((k) => (
          <motion.div key={k.label} variants={item} className="bg-[--card] border border-[--border] rounded-[--radius-md] p-5">
            <div className="flex items-center justify-between mb-3">
              <k.icon size={18} className={k.accent ? 'text-green-500' : 'text-[--muted-foreground]'} />
              {k.accent && <span className="text-xs text-green-600 bg-green-500/10 px-2 py-0.5 rounded-full font-medium">-2.1%</span>}
            </div>
            <p className={`text-3xl font-bold ${k.accent ? 'text-green-600' : ''}`}>{k.value}</p>
            <p className="text-sm text-[--muted-foreground] mt-0.5">{k.label}</p>
          </motion.div>
        ))}
      </motion.div>

      <motion.div variants={container} initial="hidden" animate="show" className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
        <motion.div variants={item} className="bg-[--card] border border-[--border] rounded-[--radius-md] p-5">
          <h2 className="headline-md mb-5">Room Utilization</h2>
          <div className="flex items-end gap-3" style={{ height: 128 }}>
            {roomData.map((r) => (
              <div key={r.label} className="flex-1 flex flex-col items-center gap-1.5 h-full justify-end">
                <span className="text-xs font-medium">{r.value}%</span>
                <div className="w-full rounded-[--radius-sm]" style={{ height: `${(r.value / maxRoom) * 100}%`, maxHeight: '100%', backgroundColor: 'var(--primary)', opacity: 0.7 + (r.value / maxRoom) * 0.3 }} />
                <span className="label-xs text-[--muted-foreground] text-center">{r.label}</span>
              </div>
            ))}
          </div>
        </motion.div>

        <motion.div variants={item} className="bg-[--card] border border-[--border] rounded-[--radius-md] p-5">
          <h2 className="headline-md mb-5">Lecturer Workload</h2>
          <div className="space-y-4">
            {lecturerWorkload.map((l) => (
              <div key={l.name}>
                <div className="flex items-center justify-between mb-1.5">
                  <span className="body-md">{l.name}</span>
                  <span className="label-sm text-[--muted-foreground]">{l.load}% &middot; {l.courses} courses</span>
                </div>
                <div className="w-full h-2.5 bg-[--muted] rounded-full overflow-hidden">
                  <div className="h-full rounded-full bg-[--foreground] transition-all" style={{ width: `${l.load}%` }} />
                </div>
              </div>
            ))}
          </div>
        </motion.div>
      </motion.div>

      <motion.div variants={container} initial="hidden" animate="show" className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <motion.div variants={item} className="bg-[--card] border border-[--border] rounded-[--radius-md] p-5">
          <h2 className="headline-md mb-5">Schedule Efficiency</h2>
          <div className="space-y-0">
            {timelineSteps.map((s, i) => (
              <div key={s.week} className="relative flex items-start gap-4 pb-6 last:pb-0">
                {i < timelineSteps.length - 1 && (
                  <div className="absolute left-[7px] top-4 bottom-0 w-px bg-[--border]" />
                )}
                <div className={`w-3.5 h-3.5 mt-1 rounded-full border-2 shrink-0 ${i === timelineSteps.length - 1 ? 'bg-[--foreground] border-[--foreground]' : 'bg-[--card] border-[--border]'}`} />
                <div className="flex-1 flex items-center justify-between">
                  <div>
                    <p className="body-md font-medium">{s.week}</p>
                    <p className="label-sm text-[--muted-foreground]">{s.conflicts} conflicts</p>
                  </div>
                  <span className="text-lg font-bold">{s.efficiency}%</span>
                </div>
              </div>
            ))}
          </div>
        </motion.div>

        <motion.div variants={item} className="bg-[--card] border border-[--border] rounded-[--radius-md] p-5">
          <h2 className="headline-md mb-5">Conflict Reduction</h2>
          <div className="grid grid-cols-3 gap-4">
            <ConflictRing pct={92} label="Room Conflicts" />
            <ConflictRing pct={88} label="Lecturer Conflicts" />
            <ConflictRing pct={96} label="Student Conflicts" />
          </div>
        </motion.div>
      </motion.div>
    </div>
  );
}
