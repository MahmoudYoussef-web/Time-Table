import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Calendar, AlertTriangle, DoorOpen, FileText } from 'lucide-react';
import { Navbar } from '../components/layout/Navbar';

/* ------------------------------------------------------------------ */
/*  Feature strip data                                                */
/* ------------------------------------------------------------------ */
const FEATURES = [
  { icon: Calendar,     title: 'Automatic Generation',  desc: 'Schedules 100+ sections in under 10 seconds' },
  { icon: AlertTriangle, title: 'Conflict Detection',    desc: 'Hard and soft constraint checking' },
  { icon: DoorOpen,     title: 'Room Allocation',        desc: 'Matches sections to rooms by type and capacity' },
  { icon: FileText,     title: 'Export Ready',           desc: 'PDF and Excel export from day one' },
];

/* ------------------------------------------------------------------ */
/*  Steps data                                                        */
/* ------------------------------------------------------------------ */
const STEPS = [
  { label: 'Add Courses & Departments' },
  { label: 'Assign Instructors' },
  { label: 'Define Time Slots' },
  { label: 'Generate Schedule' },
  { label: 'Export & Publish' },
];

/* ------------------------------------------------------------------ */
/*  Timetable preview card (right column)                             */
/* ------------------------------------------------------------------ */
const HOURS_PREVIEW = ['8:00','9:00','10:00','11:00','12:00','13:00','14:00','15:00','16:00'];
const DAYS_PREVIEW  = ['SAT','SUN','MON','TUE','WED','THU'];
const RH_P = 42;
const CW_P = 88;
const LW_P = 40;

const BLOCKS_PREVIEW = [
  { day: 0, start: 0, end: 2, code: 'CS401', accent: true },
  { day: 0, start: 4, end: 6, code: 'CS302', accent: false },
  { day: 0, start: 7, end: 9, code: 'CS201', accent: false },
  { day: 1, start: 1, end: 3, code: 'CS305', accent: false },
  { day: 1, start: 5, end: 7, code: 'CS403', accent: true },
  { day: 2, start: 0, end: 3, code: 'CS450', accent: true },
  { day: 2, start: 4, end: 6, code: 'CS401', accent: false },
  { day: 2, start: 7, end: 9, code: 'CS101', accent: false },
  { day: 3, start: 1, end: 3, code: 'CS305', accent: false },
  { day: 3, start: 4, end: 6, code: 'CS302', accent: true },
  { day: 3, start: 6, end: 8, code: 'CS403', accent: false },
  { day: 4, start: 0, end: 2, code: 'CS450', accent: true },
  { day: 4, start: 3, end: 5, code: 'CS403', accent: false },
  { day: 4, start: 6, end: 8, code: 'CS201', accent: false },
  { day: 5, start: 2, end: 4, code: 'CS401', accent: false },
  { day: 5, start: 5, end: 7, code: 'CS302', accent: true },
];

function SchedulePreview() {
  const W = DAYS_PREVIEW.length * CW_P + LW_P;
  const H = HOURS_PREVIEW.length * RH_P + 24;

  return (
    <motion.div
      style={{
        background: '#0D0D0D',
        borderRadius: 12,
        overflow: 'hidden',
        boxShadow: '0 24px 64px rgba(0,0,0,0.22), 0 4px 16px rgba(0,0,0,0.12)',
        transform: 'rotate(-1.2deg)',
        transformOrigin: 'center center',
        width: '100%',
        maxWidth: 620,
      }}
      whileHover={{ rotate: 0, transition: { duration: 0.35, ease: 'easeOut' } }}
    >
      {/* Header bar */}
      <div style={{
        padding: '12px 20px',
        borderBottom: '0.5px solid rgba(255,255,255,0.08)',
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      }}>
        <span style={{
          fontSize: 10, fontWeight: 500, letterSpacing: '0.1em',
          textTransform: 'uppercase', color: 'rgba(255,255,255,0.28)',
          fontFamily: 'Geist, sans-serif',
        }}>Fall 2026 Schedule</span>
        <div style={{ display: 'flex', gap: 5 }}>
          {[1,2,3].map(i => (
            <div key={i} style={{ width: 7, height: 7, borderRadius: '50%', background: `rgba(255,255,255,${i===1?0.5:i===2?0.25:0.12})` }} />
          ))}
        </div>
      </div>

      {/* Grid */}
      <div style={{ overflowX: 'auto', padding: '12px 16px 16px' }}>
        <svg viewBox={`0 0 ${W} ${H}`} style={{ width: '100%', minWidth: W, display: 'block' }}>
          {/* Day headers */}
          {DAYS_PREVIEW.map((d, i) => (
            <text key={d}
              x={LW_P + i * CW_P + CW_P / 2} y={14}
              textAnchor="middle" fontSize="9"
              fontFamily="Geist, sans-serif" fontWeight="500"
              letterSpacing="0.08em" fill="rgba(255,255,255,0.25)"
            >{d}</text>
          ))}
          {/* Hour labels */}
          {HOURS_PREVIEW.map((h, i) => (
            <text key={h}
              x={LW_P - 6} y={24 + i * RH_P + RH_P / 2 + 4}
              textAnchor="end" fontSize="8.5"
              fontFamily="DM Mono, monospace" fill="rgba(255,255,255,0.18)"
            >{h}</text>
          ))}
          {/* Grid lines */}
          {HOURS_PREVIEW.map((_, i) => (
            <line key={`h${i}`}
              x1={LW_P} y1={24 + i * RH_P}
              x2={W}    y2={24 + i * RH_P}
              stroke="rgba(255,255,255,0.05)" strokeWidth="0.5"
            />
          ))}
          {DAYS_PREVIEW.map((_, i) => (
            <line key={`v${i}`}
              x1={LW_P + i * CW_P} y1={20}
              x2={LW_P + i * CW_P} y2={H}
              stroke="rgba(255,255,255,0.05)" strokeWidth="0.5"
            />
          ))}
          {/* Blocks */}
          {BLOCKS_PREVIEW.map((b, i) => (
            <g key={i}>
              <rect
                x={LW_P + b.day * CW_P + 3}
                y={24 + b.start * RH_P + 2}
                width={CW_P - 6}
                height={(b.end - b.start) * RH_P - 4}
                rx={3}
                fill={b.accent ? 'rgba(200,184,154,0.50)' : 'rgba(255,255,255,0.10)'}
              />
              <text
                x={LW_P + b.day * CW_P + 10}
                y={24 + b.start * RH_P + 17}
                fontSize="8.5"
                fontFamily="Geist, sans-serif" fontWeight="600"
                fill={b.accent ? 'rgba(20,12,0,0.8)' : 'rgba(255,255,255,0.65)'}
              >{b.code}</text>
            </g>
          ))}
        </svg>
      </div>
    </motion.div>
  );
}

/* ------------------------------------------------------------------ */
/*  Page                                                              */
/* ------------------------------------------------------------------ */
export function HomePage() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-[--background] text-[--foreground] flex flex-col">
      <Navbar />

      {/* Section 1 — Hero */}
      <section className="flex flex-col lg:flex-row max-w-[1400px] mx-auto w-full px-8 md:px-14 pt-16 pb-8 gap-10 lg:gap-16 items-center">
        <motion.div
          className="lg:w-1/2 flex flex-col justify-center"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.7 }}
        >
          <span className="overline mb-4">ACADEMIC SCHEDULING PLATFORM</span>
          <h1
            className="italic mb-8"
            style={{
              fontFamily: 'Instrument Serif, serif',
              fontSize: 'clamp(72px, 9vw, 132px)',
              fontWeight: 400,
              lineHeight: 0.88,
              letterSpacing: '-0.03em',
              color: 'var(--foreground)',
            }}
          >
            Build<br />Timetables<br />in Minutes.
          </h1>
          <p className="mb-10" style={{ fontSize: 16, lineHeight: 1.7, color: 'var(--muted-foreground)', maxWidth: 460 }}>
            Automate conflict detection, room allocation, and faculty scheduling for your entire university — in seconds.
          </p>
          <div className="flex items-center gap-3 mb-8">
            <button
              onClick={() => navigate('/auth')}
              className="h-11 px-7 bg-[--primary] text-[--primary-foreground] text-sm font-medium rounded-[--radius-md] cursor-pointer hover:opacity-90 transition-opacity"
            >
              Get Started
            </button>
            <button
              onClick={() => document.getElementById('features')?.scrollIntoView({ behavior: 'smooth' })}
              className="h-11 px-7 bg-transparent border border-[--border] text-[--foreground] text-sm font-medium rounded-[--radius-md] cursor-pointer hover:bg-[--muted] transition-colors"
            >
              Learn More
            </button>
          </div>
          <div className="flex items-center gap-5 label-sm text-[--muted-foreground]">
            <span>✓ Conflict-Free</span>
            <span>✓ SAT–THU Week</span>
            <span>✓ PDF Export</span>
          </div>
        </motion.div>

<motion.div
  className="lg:w-1/2 flex items-center justify-center lg:justify-end"
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, delay: 0.2 }}
        >
          <SchedulePreview />
        </motion.div>
      </section>

      {/* Section 2 — Feature Strip */}
      <section id="features" className="py-10 mt-4" style={{ borderTop: '0.5px solid var(--border)', borderBottom: '0.5px solid var(--border)' }}>
        <div className="max-w-[1280px] mx-auto px-6 md:px-10">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-0">
            {FEATURES.map((f, i) => (
              <div
                key={f.title}
                className="flex items-start gap-4 px-6 py-2"
                style={{
                  borderLeft: i > 0 ? '0.5px solid var(--border)' : 'none',
                }}
              >
                <f.icon size={18} className="flex-shrink-0 mt-0.5" style={{ color: 'var(--foreground)' }} />
                <div>
                  <h3 className="display-sm mb-1">{f.title}</h3>
                  <p className="body-md" style={{ color: 'var(--muted-foreground)' }}>{f.desc}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Section 3 — How It Works */}
      <section className="py-20">
        <div className="max-w-[1280px] mx-auto px-6 md:px-10 text-center">
          <h2 className="display-lg mb-16">From data to timetable in five steps</h2>
          <div className="relative flex justify-between items-start max-w-[760px] mx-auto">
            {/* Connector line — behind circles, full width */}
            <div
              className="absolute"
              style={{
                top: 16,
                left: '10%',
                right: '10%',
                height: '0.5px',
                background: 'var(--border)',
                zIndex: 0,
              }}
            />
            {STEPS.map((s, i) => (
              <div key={s.label} className="flex flex-col items-center relative z-10" style={{ width: '20%' }}>
                <div
                  className="w-8 h-8 rounded-full flex items-center justify-center mono-sm"
                  style={{
                    background: i <= 2 ? 'var(--foreground)' : 'var(--background)',
                    border: i <= 2 ? 'none' : '0.5px solid var(--border)',
                    color: i <= 2 ? 'var(--primary-foreground)' : 'var(--muted-foreground)',
                  }}
                >
                  {i + 1}
                </div>
                <span className="label-md mt-3 text-center leading-tight"
                  style={{ maxWidth: 96 }}>
                  {s.label}
                </span>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Section 4 — CTA */}
      <section className="py-16 bg-[--muted]/50 border-y border-[--border]">
        <div className="max-w-[640px] mx-auto px-6 text-center">
          <h2 className="display-lg italic mb-8">Ready to schedule smarter?</h2>
          <button
            onClick={() => navigate('/auth')}
            className="h-12 px-8 bg-[--primary] text-[--primary-foreground] text-sm font-medium rounded-[--radius-md] cursor-pointer hover:opacity-90 transition-opacity"
          >
            Start Now
          </button>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-[--border] py-4" style={{ borderTopWidth: '0.5px' }}>
        <div className="max-w-[1280px] mx-auto px-6 md:px-10 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-5 h-5 bg-[--foreground] rounded-[--radius-sm] flex items-center justify-center">
              <svg width="10" height="10" viewBox="0 0 18 18" fill="none">
                <rect x="2" y="2" width="14" height="14" rx="1.5" stroke="var(--primary-foreground)" strokeWidth="1.1" fill="none" />
                <line x1="2" y1="6.5" x2="16" y2="6.5" stroke="var(--primary-foreground)" strokeWidth="1.1" />
              </svg>
            </div>
            <span className="label-sm">CampusGrid</span>
          </div>
          <span className="label-sm text-[--muted-foreground]">© 2026 CampusGrid</span>
          <span className="label-sm text-[--muted-foreground] cursor-pointer hover:text-[--foreground]">GitHub</span>
        </div>
      </footer>
    </div>
  );
}
