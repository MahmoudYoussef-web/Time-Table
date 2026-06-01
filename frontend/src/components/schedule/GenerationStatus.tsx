import { Loader2, CheckCircle, XCircle, Grid3x3, FileText, Table as TableIcon, AlertTriangle, RotateCcw } from 'lucide-react';
import { ScheduleGenerationJob, ConstraintViolation } from '../../types';
import { formatDuration } from '../../lib/utils';
import { Button } from '../ui/Button';
import { downloadPdf, downloadExcel } from '../../api/schedules';
import { ConflictBadge } from './ConflictBadge';
import { useNavigate } from 'react-router-dom';

interface GenerationStatusProps {
  jobId: string;
  elapsed: number;
  job: ScheduleGenerationJob | null;
  onRetry: () => void;
  scheduleId: number | null;
  conflicts: ConstraintViolation[];
}

export function GenerationStatus({ jobId, elapsed, job, onRetry, scheduleId, conflicts }: GenerationStatusProps) {
  const navigate = useNavigate();

  if (!job || job.status === 'RUNNING') {
    return (
      <div className="space-y-4">
        <div className="flex items-center gap-3">
          <Loader2 className="animate-spin" size={24} />
          <p className="headline-md">Generating schedule...</p>
        </div>
        <p className="label-sm text-[--muted-foreground]">Job ID: {jobId}</p>
        <p className="label-sm text-[--muted-foreground]">Status: RUNNING</p>
        <div className="h-1 bg-black/10 rounded-full overflow-hidden">
          <div className="h-full bg-[--primary] rounded-full animate-pulse" style={{ width: '30%' }} />
        </div>
        <p className="label-sm text-[--muted-foreground]">Elapsed: {formatDuration(elapsed)}</p>
      </div>
    );
  }

  if (job.status === 'COMPLETED' && scheduleId) {
    return (
      <div className="space-y-4">
        <div className="flex items-center gap-3">
          <CheckCircle className="text-[--success]" size={24} />
          <p className="headline-md text-[--success]">Schedule generated!</p>
        </div>
        <p className="label-sm text-[--muted-foreground]">Schedule ID: {scheduleId}</p>
        <div className="flex flex-wrap gap-2">
          <Button onClick={() => navigate(`/schedules/${scheduleId}/weekly`)}>
            <Grid3x3 size={18} /> View Weekly Schedule
          </Button>
          <Button variant="secondary" onClick={() => downloadPdf(scheduleId)}>
            <FileText size={18} /> Download PDF
          </Button>
          <Button variant="secondary" onClick={() => downloadExcel(scheduleId)}>
            <TableIcon size={18} /> Download Excel
          </Button>
        </div>
        {conflicts.length > 0 && (
          <div>
            <p className="flex items-center gap-2 headline-md mt-4 mb-2">
              <AlertTriangle size={20} className="text-[--warning]" />
              Constraint Violations ({conflicts.length})
            </p>
            {conflicts.map((v, i) => (
              <ConflictBadge key={i} violation={v} type={v.constraintName.toLowerCase().includes('soft') ? 'soft' : 'hard'} />
            ))}
          </div>
        )}
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-3">
        <XCircle className="text-[--destructive]" size={24} />
        <p className="headline-md text-[--destructive]">Generation failed</p>
      </div>
      <p className="body-md text-[--muted-foreground]">Server returned an error</p>
      <Button onClick={onRetry}>
        <RotateCcw size={18} /> Try Again
      </Button>
    </div>
  );
}
