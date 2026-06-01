import { ScheduleEntryDTO } from '../../types';

export function ScheduleCell({ entry }: { entry: ScheduleEntryDTO }) {
  return (
    <div className="relative p-1.5 rounded-[--radius-sm] h-full min-h-[70px] border-l-[3px] bg-[--muted]/40 border-[--foreground]/20">
      {entry.hardViolations > 0 && (
        <span className="absolute top-1 right-1 w-2 h-2 rounded-full bg-[--destructive]" />
      )}
      {entry.softViolations > 0 && entry.hardViolations === 0 && (
        <span className="absolute top-1 right-1 w-2 h-2 rounded-full bg-[--warning]" />
      )}
      <p className="text-sm font-semibold truncate">{entry.courseCode}</p>
      <p className="text-xs text-[--muted-foreground] truncate">{entry.instructorName}</p>
      <p className="text-xs text-[--muted-foreground]">{entry.roomNumber} ({entry.sessionType})</p>
    </div>
  );
}
