import { memo } from 'react';
import { ScheduleEntryDTO } from '../../types';
import { getDeptColor } from '../../lib/constants';

interface ScheduleCellProps {
  entry: ScheduleEntryDTO;
  onClick?: (entry: ScheduleEntryDTO) => void;
}

export const ScheduleCell = memo(function ScheduleCell({ entry, onClick }: ScheduleCellProps) {
  const deptColor = getDeptColor(entry.departmentName);
  return (
    <div
      className="relative p-1.5 rounded-[--radius-sm] h-full min-h-[70px] border-l-[3px] bg-[--muted]/40 cursor-pointer hover:bg-[--muted]/60 transition-colors group"
      style={{ borderLeftColor: deptColor }}
      onClick={() => onClick?.(entry)}
      title={`${entry.courseCode} - ${entry.courseName}\nInstructor: ${entry.instructorName}\nRoom: ${entry.roomNumber}\nTime: ${entry.startTime} - ${entry.endTime}\nDept: ${entry.departmentName} (${entry.yearLevel})\n${entry.hardViolations > 0 ? `Hard violations: ${entry.hardViolations}` : ''}${entry.softViolations > 0 ? `\nSoft violations: ${entry.softViolations}` : ''}`}
    >
      {entry.hardViolations > 0 && (
        <span className="absolute top-1 right-1 w-2 h-2 rounded-full bg-[--destructive]" title={`${entry.hardViolations} hard violation(s)`} />
      )}
      {entry.softViolations > 0 && entry.hardViolations === 0 && (
        <span className="absolute top-1 right-1 w-2 h-2 rounded-full bg-[--warning]" title={`${entry.softViolations} soft violation(s)`} />
      )}
      <p className="text-sm font-semibold truncate">{entry.courseCode}</p>
      <p className="text-xs text-[--muted-foreground] truncate">{entry.courseName}</p>
      <p className="text-xs text-[--muted-foreground] truncate">{entry.instructorName}</p>
      <p className="text-xs text-[--muted-foreground]">{entry.roomNumber}</p>
      <div className="absolute bottom-1 right-1 opacity-0 group-hover:opacity-100 transition-opacity">
        <span className="text-[10px] px-1 rounded" style={{ backgroundColor: deptColor + '30', color: deptColor }}>
          {entry.departmentName.split(' ').map(w => w[0]).join('').slice(0, 3)}
        </span>
      </div>
    </div>
  );
});
