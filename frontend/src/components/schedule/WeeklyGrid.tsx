import { useState, useMemo, memo } from 'react';
import { AlertTriangle, FileText, Table as TableIcon, Image } from 'lucide-react';
import { motion } from 'framer-motion';
import { ScheduleEntryDTO, DayOfWeek, YearLevel, WeeklyScheduleDTO, ConstraintViolation } from '../../types';
import { dayLabel, cn } from '../../lib/utils';
import { Button } from '../ui/Button';
import { downloadPdf, downloadExcel, downloadPng, getConflicts } from '../../api/schedules';
import { ScheduleCell } from './ScheduleCell';

const EGYPTIAN_DAYS: DayOfWeek[] = ['SATURDAY', 'SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY'];

const container = { hidden: {}, show: { transition: { staggerChildren: 0.04 } } };
const rowItem = { hidden: { opacity: 0, y: 6 }, show: { opacity: 1, y: 0, transition: { duration: 0.25, ease: 'easeOut' as const } } };

interface WeeklyGridProps {
  data: WeeklyScheduleDTO;
  scheduleId?: number;
  showFilter?: boolean;
  showExport?: boolean;
}

export const WeeklyGrid = memo(function WeeklyGrid({ data, scheduleId, showFilter = true, showExport = true }: WeeklyGridProps) {
  const [selectedYear, setSelectedYear] = useState<YearLevel | 'ALL'>('ALL');
  const [selectedDept, setSelectedDept] = useState<string>('ALL');
  const [showConflicts, setShowConflicts] = useState(false);
  const [conflicts, setConflicts] = useState<ConstraintViolation[]>([]);
  const [exportTheme, setExportTheme] = useState<'NAVY' | 'BLACK'>('NAVY');
  const [exportYear, setExportYear] = useState<string>('');

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
      </div>

      {showExport && scheduleId && (
        <div className="flex items-center gap-3 mb-3 flex-wrap">
          <div className="flex rounded-lg overflow-hidden border border-[--border]">
            {(['NAVY', 'BLACK'] as const).map(t => (
              <button
                key={t}
                onClick={() => setExportTheme(t)}
                className={`px-3 py-1 text-sm ${exportTheme === t
                  ? 'bg-[--primary] text-white'
                  : 'bg-[--surface] text-[--text-secondary]'}`}
              >
                {t}
              </button>
            ))}
          </div>
          <select
            value={exportYear}
            onChange={e => setExportYear(e.target.value)}
            className="text-sm border border-[--border] rounded px-2 py-1 bg-[--surface]"
          >
            <option value="">All Years</option>
            <option value="FIRST">First Year</option>
            <option value="SECOND">Second Year</option>
            <option value="THIRD">Third Year</option>
            <option value="FOURTH">Fourth Year</option>
          </select>
          <Button size="sm" variant="secondary" onClick={() => downloadPdf(scheduleId, exportTheme, exportYear || undefined)}>
            <FileText size={16} /> PDF
          </Button>
          <Button size="sm" variant="secondary" onClick={() => downloadExcel(scheduleId, exportTheme)}>
            <TableIcon size={16} /> Excel
          </Button>
          <Button size="sm" variant="secondary" onClick={() => downloadPng(scheduleId, exportTheme, exportYear || undefined)}>
            <Image size={16} /> PNG
          </Button>
          <Button size="sm" variant="secondary" onClick={loadConflicts}>
            <AlertTriangle size={16} /> Conflicts
          </Button>
        </div>
      )}

      <div className="overflow-x-auto -mx-4 px-4 md:mx-0 md:px-0">
        <div className="min-w-[680px]">
          <table className="w-full border-collapse">
            <thead>
              <tr>
                <th className="w-16 mono-sm text-right pr-3 py-2 border-b border-[--border] text-[--muted-foreground] whitespace-nowrap" style={{ borderBottomWidth: '0.5px' }} />
                {EGYPTIAN_DAYS.map((day) => (
                  <th key={day} className="overline text-center py-2 border-b border-[--border] min-w-[100px] whitespace-nowrap" style={{ borderBottomWidth: '0.5px' }}>
                    {dayLabel(day)}
                  </th>
                ))}
              </tr>
            </thead>
            <motion.tbody variants={container} initial="hidden" animate="show">
              {timeSlots.map((time) => (
                <motion.tr key={time} variants={rowItem}>
                  <td className="w-16 mono-sm text-right pr-3 py-2 text-[--muted-foreground] border-b border-[--border] align-top whitespace-nowrap" style={{ borderBottomWidth: '0.5px' }}>
                    {time}
                  </td>
                  {EGYPTIAN_DAYS.map((day) => {
                    const entry = getEntry(day, time);
                    if (!entry) {
                      return (
                        <td key={day} className="border-b border-[--border] p-1 align-top h-[70px] group" style={{ borderBottomWidth: '0.5px' }}>
                          <div className="h-full w-full rounded-[--radius-sm] border border-dashed border-transparent group-hover:border-[--border]/40 transition-colors">
                            <div className="empty-cell text-center text-[--muted-foreground] text-sm py-3">&mdash;</div>
                          </div>
                        </td>
                      );
                    }
                    return (
                      <td key={day} className="border-b border-[--border] p-1 align-top h-[70px]" style={{ borderBottomWidth: '0.5px' }}>
                        <ScheduleCell entry={entry} />
                      </td>
                    );
                  })}
                </motion.tr>
              ))}
            </motion.tbody>
          </table>
        </div>
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
});
