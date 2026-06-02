import { Loader2, CheckCircle, XCircle, Grid3x3, FileText, Table as TableIcon, Image, RotateCcw } from 'lucide-react';
import { ScheduleGenerationJob } from '../../types';
import { formatDuration } from '../../lib/utils';
import { Button } from '../ui/Button';

interface GenerationStatusProps {
  job: ScheduleGenerationJob | null;
  elapsedSeconds: number;
  onViewSchedule: (scheduleId: number) => void;
  onRetry: () => void;
  onDownloadPdf: () => void;
  onDownloadExcel: () => void;
  onDownloadPng: () => void;
}

export function GenerationStatus({
  job, elapsedSeconds, onViewSchedule, onRetry,
  onDownloadPdf, onDownloadExcel, onDownloadPng
}: GenerationStatusProps) {

  if (!job || job.status === 'RUNNING') {
    return (
      <div className="space-y-4">
        <div className="flex items-center gap-3">
          <Loader2 className="animate-spin" size={24} />
          <p className="display-md">Generating schedule...</p>
        </div>
        <p className="label-sm text-[--muted-foreground]">Status: RUNNING</p>
        <div className="h-1 bg-black/10 rounded-full overflow-hidden">
          <div className="h-full bg-[--primary] rounded-full animate-pulse" style={{ width: '30%' }} />
        </div>
        <p className="label-sm text-[--muted-foreground]">Elapsed: {formatDuration(elapsedSeconds)}</p>
      </div>
    );
  }

  if (job.status === 'COMPLETED' && job.scheduleId) {
    return (
      <div className="space-y-4">
        <div className="flex items-center gap-3">
          <CheckCircle className="text-[--success]" size={24} />
          <p className="display-md text-[--success]">Schedule generated!</p>
        </div>
        <p className="label-sm text-[--muted-foreground]">Schedule ID: {job.scheduleId}</p>
        <div className="flex flex-wrap gap-2">
          <Button onClick={() => onViewSchedule(job.scheduleId)}>
            <Grid3x3 size={18} /> View Weekly Schedule
          </Button>
          <Button variant="secondary" onClick={onDownloadPdf}>
            <FileText size={18} /> Download PDF
          </Button>
          <Button variant="secondary" onClick={onDownloadExcel}>
            <TableIcon size={18} /> Download Excel
          </Button>
          <Button variant="secondary" onClick={onDownloadPng}>
            <Image size={18} /> Download PNG
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-3">
        <XCircle className="text-[--destructive]" size={24} />
        <p className="display-md text-[--destructive]">Generation failed</p>
      </div>
      <p className="body-md text-[--muted-foreground]">Server returned an error</p>
      <Button onClick={onRetry}>
        <RotateCcw size={18} /> Try Again
      </Button>
    </div>
  );
}
