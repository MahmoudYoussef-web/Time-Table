import { useState, useMemo } from 'react';
import { AlertTriangle, FileText, Table as TableIcon } from 'lucide-react';
import { motion } from 'framer-motion';
import { ScheduleEntryDTO, DayOfWeek, YearLevel, WeeklyScheduleDTO, ConstraintViolation } from '../../types';
import { dayLabel, cn } from '../../lib/utils';
import { Button } from '../ui/Button';
import { downloadPdf, downloadExcel, getConflicts } from '../../api/schedules';

const EGYPTIAN_DAYS: DayOfWeek[] = ['SATURDAY', 'SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY'];

const BORDER_COLORS: Record<string, string> = {
  LECTURE:  'var(--info)',
  LAB:      'var(--success)',
  TUTORIAL: 'var(--warning)',
  SEMINAR:  '#7C6FAF',
  SECTION:  'var(--accent)',
};

const container = { hidden: {}, show: { transition: { staggerChildren: 0.04 } } };
const rowItem = { hidden: { opacity: 0, y: 6 }, show: { opacity: 1, y: 0, transition: { duration: 0.25, ease: 'easeOut' } } };

interface WeeklyGridProps {
  data: WeeklyScheduleDTO;
  scheduleId?: number;
  showFilter?: boolean;
  showExport?: boolean;
}

export function WeeklyGrid({ data, scheduleId, showFilter = true, showExport = true }: WeeklyGridProps) {
  const [selectedYear, setSelectedYear] = useState<YearLevel | 'ALL'>('ALL');
  const [selectedDept, setSelectedDept] = useState<string>('ALL');
  const [showConflicts, setShowConflicts] = useState(false);
  const [conflicts, setConflicts] = useState<ConstraintViolation[]>([]);

  const timeSlots = useMemo(() => {
    const slots = new Set<string>();
    data.days.forEach((d) => d.slots.forEach((s) => slots.add(s.startTime)));
    return [...slots].sort();
  }, [data]);

  const departments = useMemo(() => {
    const depts = new Set<string>();
    data.days.forEach((d) => d.slots.forEach((s) => {
      if (s.entry) depts.add(s.entry.departmentName);
    }));
    return [...depts].sort();
  }, [data]);

  const entryMap = useMemo(() => {
    const map = new Map<string, ScheduleEntryDTO>();
    data.days.forEach((day) => {
      day.slots.forEach((slot) => {
        if (slot.entry) {
          map.set(`${day.day}-${slot.startTime}`, slot.entry);
        }
      });
    });
    return map;
  }, [data]);

  const getEntry = (day: DayOfWeek, time: string): ScheduleEntryDTO | undefined => {
    const entry = entryMap.get(`${day}-${time}`);
    if (!entry) return undefined;
    if (selectedYear !== 'ALL' && entry.yearLevel !== selectedYear) return undefined;
    if (selectedDept !== 'ALL' && entry.departmentName !== selectedDept) return undefined;
    return entry;
  };

  const loadConflicts = async () => {
    if (scheduleId && !conflicts.length) {
      try {
        const c = await getConflicts(scheduleId);
        setConflicts(c);
      } catch { /* ignore */ }
    }
    setShowConflicts((p) => !p);
  };

  const yearLevels: (YearLevel | 'ALL')[] = ['ALL', 'FIRST', 'SECOND', 'THIRD', 'FOURTH'];

  return (
    <div>
      <div className="flex items-center justify-between mb-4 flex-wrap gap-2">
        {showFilter && (
          <div className="flex items-center gap-2">
            {yearLevels.map((y) => (
              <button
                key={y}
                onClick={() => setSelectedYear(y)}
                className={cn(
                  'px-3 py-1 rounded-[--radius-sm] label-md transition-colors cursor-pointer',
                  selectedYear === y ? 'bg-[--primary] text-[--primary-foreground]' : 'bg-[--muted] hover:bg-[--muted]/70'
                )}
              >
                {y === 'ALL' ? 'All' : y.charAt(0) + y.slice(1).toLowerCase()}
              </button>
            ))}
            <select
              value={selectedDept}
              onChange={(e) => setSelectedDept(e.target.value)}
              className="border border-[--border] rounded-[--radius-sm] bg-[--background] px-2 py-1 label-md ml-2"
            >
              <option value="ALL">All Departments</option>
              {departments.map((d) => <option key={d} value={d}>{d}</option>)}
            </select>
          </div>
        )}
        {showExport && scheduleId && (
          <div className="flex items-center gap-2">
            <Button size="sm" variant="secondary" onClick={() => downloadPdf(scheduleId)}>
              <FileText size={16} /> PDF
            </Button>
            <Button size="sm" variant="secondary" onClick={() => downloadExcel(scheduleId)}>
              <TableIcon size={16} /> Excel
            </Button>
            <Button size="sm" variant="secondary" onClick={loadConflicts}>
              <AlertTriangle size={16} /> Conflicts
            </Button>
          </div>
        )}
      </div>

      <div className="overflow-x-auto">
        <table className="w-full border-collapse" style={{ minWidth: 700 }}>
          <thead>
            <tr>
              <th className="w-16 mono-sm text-right pr-3 py-2 border-b border-[--border] text-[--muted-foreground]" style={{ borderBottomWidth: '0.5px' }} />
              {EGYPTIAN_DAYS.map((day) => (
                <th key={day} className="overline text-center py-2 border-b border-[--border] min-w-[100px]" style={{ borderBottomWidth: '0.5px' }}>
                  {dayLabel(day)}
                </th>
              ))}
            </tr>
          </thead>
          <motion.tbody variants={container} initial="hidden" animate="show">
            {timeSlots.map((time) => (
              <motion.tr key={time} variants={rowItem}>
                <td className="w-16 mono-sm text-right pr-3 py-2 text-[--muted-foreground] border-b border-[--border] align-top" style={{ borderBottomWidth: '0.5px' }}>
                  {time}
                </td>
                {EGYPTIAN_DAYS.map((day) => {
                  const entry = getEntry(day, time);
                  if (!entry) {
                    return (
                      <td key={day} className="border-b border-[--border] p-1 align-top h-[70px] group" style={{ borderBottomWidth: '0.5px' }}>
                        <div className="h-full w-full rounded-[--radius-sm] border border-dashed border-transparent group-hover:border-[--border]/40 transition-colors" />
                      </td>
                    );
                  }
                  const borderColor = BORDER_COLORS[entry.sessionType] || 'var(--accent)';
                  const hasHardViolation = entry.hardViolations > 0;
                  return (
                    <td key={day} className="border-b border-[--border] p-1 align-top h-[70px]" style={{ borderBottomWidth: '0.5px' }}>
                      <div
                        className="h-full rounded-[--radius-sm] bg-[--card] px-2 py-1.5 flex flex-col justify-center"
                        style={{
                          borderLeft: `2px solid ${borderColor}`,
                          ...(hasHardViolation ? { border: '2px solid var(--destructive)' } : {}),
                        }}
                      >
                        <span className="label-sm font-semibold">{entry.courseCode}</span>
                        <span className="label-sm text-[--muted-foreground]">{entry.roomNumber}</span>
                        <span className="label-sm text-[--muted-foreground] truncate">{entry.instructorName}</span>
                      </div>
                    </td>
                  );
                })}
              </motion.tr>
            ))}
          </motion.tbody>
        </table>
      </div>

      {showConflicts && (
        <div className="fixed right-0 top-0 h-full w-[380px] bg-[--card] border-l border-[--border] shadow-lg z-40 overflow-y-auto" style={{ borderLeftWidth: '0.5px' }}>
          <div className="p-4">
            <div className="flex items-center justify-between mb-4">
              <p className="display-sm">Constraint Violations ({conflicts.length})</p>
              <button onClick={() => setShowConflicts(false)} className="cursor-pointer label-md hover:opacity-70">Close</button>
            </div>
            {conflicts.length === 0 ? (
              <p className="body-md text-[--muted-foreground]">No violations found.</p>
            ) : (
              conflicts.map((v, i) => {
                const isHard = v.constraintName.toLowerCase().includes('soft') ? 'soft' : 'hard';
                const colors = isHard === 'hard'
                  ? 'border-l-2 border-[--destructive] bg-[--destructive]/5'
                  : 'border-l-2 border-[--warning] bg-[--warning]/5';
                return (
                  <div key={i} className={`${colors} p-3 rounded-[--radius-sm] mb-2`}>
                    <p className="label-sm font-semibold">{v.constraintName}</p>
                    <p className="label-sm text-[--muted-foreground]">{v.message}</p>
                  </div>
                );
              })
            )}
          </div>
        </div>
      )}
    </div>
  );
}
