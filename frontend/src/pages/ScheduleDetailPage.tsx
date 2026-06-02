import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, CalendarDays, AlertTriangle, CheckCircle, Lock, Download, ShieldCheck } from 'lucide-react';
import { toast } from 'sonner';
import { ScheduleDTO, ConstraintViolation } from '../types';
import * as schedulesApi from '../api/schedules';
import { Button } from '../components/ui/Button';
import { ErrorState } from '../components/ui/ErrorState';

const STATUS_STYLES: Record<string, { bg: string; text: string }> = {
  DRAFT: { bg: '#F3F4F6', text: '#374151' },
  VALIDATED: { bg: '#DBEAFE', text: '#1E40AF' },
  LOCKED: { bg: '#FFF3CD', text: '#856404' },
  PUBLISHED: { bg: '#D4EDDA', text: '#155724' },
};

export function ScheduleDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [schedule, setSchedule] = useState<ScheduleDTO | null>(null);
  const [conflicts, setConflicts] = useState<ConstraintViolation[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  const fetchData = async () => {
    if (!id) return;
    setLoading(true);
    setError(false);
    try {
      const [s, c] = await Promise.all([
        schedulesApi.getSchedule(Number(id)),
        schedulesApi.getConflicts(Number(id)),
      ]);
      setSchedule(s);
      setConflicts(c);
    } catch {
      toast.error('Failed to load schedule');
      setError(true);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchData(); }, [id]);

  const handleValidate = async () => {
    if (!id) return;
    try {
      await schedulesApi.validateSchedule(Number(id));
      toast.success('Schedule validated');
      fetchData();
    } catch { toast.error('Failed to validate'); }
  };

  const handleLock = async () => {
    if (!id) return;
    try {
      await schedulesApi.lockSchedule(Number(id));
      toast.success('Schedule locked');
      fetchData();
    } catch { toast.error('Failed to lock'); }
  };

  const handleExport = async (format: 'pdf' | 'excel' | 'png') => {
    if (!id) return;
    try {
      if (format === 'pdf') await schedulesApi.downloadPdf(Number(id));
      else if (format === 'excel') await schedulesApi.downloadExcel(Number(id));
      else await schedulesApi.downloadPng(Number(id));
      toast.success(`Exported as ${format.toUpperCase()}`);
    } catch { toast.error(`Failed to export`); }
  };

  if (loading) return <div className="h-64 bg-[--muted] rounded animate-pulse" />;

  if (error || !schedule) {
    return (
      <ErrorState
        message="Failed to load schedule details"
        onRetry={fetchData}
      />
    );
  }

  const statusStyle = STATUS_STYLES[schedule.status] ?? { bg: '#F3F4F6', text: '#374151' };
  const hardConflicts = conflicts.filter(c => !c.constraintName.toLowerCase().includes('soft'));
  const softConflicts = conflicts.filter(c => c.constraintName.toLowerCase().includes('soft'));

  return (
    <div>
      <button
        onClick={() => navigate('/schedules')}
        className="flex items-center gap-1 text-sm text-[--text-secondary] hover:text-[--text] mb-4 transition-colors"
      >
        <ArrowLeft size={16} /> Back to Schedules
      </button>

      <div className="flex items-center justify-between mb-6 flex-wrap gap-3">
        <div>
          <h1 className="display-lg">Schedule #{schedule.id}</h1>
          <p className="text-[--text-secondary] mt-1">{schedule.semesterName || `Semester schedule`}</p>
        </div>
        <div className="flex gap-2">
          <span
            className="inline-flex items-center gap-1 px-3 py-1 rounded text-xs font-medium"
            style={{ backgroundColor: statusStyle.bg, color: statusStyle.text }}
          >
            {schedule.status}
          </span>
          <Button size="sm" variant="secondary" onClick={() => navigate(`/schedules/${id}/weekly`)}>
            <CalendarDays size={16} /> Weekly View
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">
        <div className="bg-[--card] border border-[--border] rounded-[--radius-md] p-5">
          <p className="text-sm text-[--muted-foreground] mb-1">Fitness Score</p>
          <p className="text-2xl font-bold font-mono">{schedule.fitnessScore.toFixed(2)}</p>
        </div>
        <div className="bg-[--card] border border-[--border] rounded-[--radius-md] p-5">
          <p className="text-sm text-[--muted-foreground] mb-1">Hard Violations</p>
          <p className="text-2xl font-bold text-red-500">{schedule.hardViolations}</p>
        </div>
        <div className="bg-[--card] border border-[--border] rounded-[--radius-md] p-5">
          <p className="text-sm text-[--muted-foreground] mb-1">Soft Violations</p>
          <p className="text-2xl font-bold text-orange-500">{schedule.softViolations}</p>
        </div>
      </div>

      <div className="flex gap-2 mb-6 flex-wrap">
        {schedule.status === 'DRAFT' && (
          <>
            <Button onClick={handleValidate}><CheckCircle size={16} /> Validate</Button>
            <Button variant="secondary" onClick={() => handleExport('pdf')}><Download size={16} /> Export PDF</Button>
            <Button variant="secondary" onClick={() => handleExport('excel')}><Download size={16} /> Export Excel</Button>
          </>
        )}
        {schedule.status === 'VALIDATED' && (
          <>
            <Button onClick={handleLock}><Lock size={16} /> Lock</Button>
            <Button variant="secondary" onClick={() => handleExport('pdf')}><Download size={16} /> Export PDF</Button>
          </>
        )}
        {schedule.status === 'LOCKED' && (
          <>
            <span className="inline-flex items-center gap-1 px-3 py-1.5 text-sm rounded bg-green-50 text-green-700 border border-green-200">
              <ShieldCheck size={16} /> Published
            </span>
            <Button variant="secondary" onClick={() => handleExport('pdf')}><Download size={16} /> Export PDF</Button>
          </>
        )}
      </div>

      <div className="bg-[--card] border border-[--border] rounded-[--radius-md] p-5">
        <div className="flex items-center gap-2 mb-4">
          <AlertTriangle size={20} className="text-orange-500" />
          <h2 className="display-md">Constraint Violations ({conflicts.length})</h2>
        </div>
        {conflicts.length === 0 ? (
          <p className="text-sm text-[--muted-foreground] py-8 text-center">No violations found.</p>
        ) : (
          <div className="space-y-2">
            {hardConflicts.map((v, i) => (
              <div key={i} className="border-l-2 border-red-500 bg-red-50/50 dark:bg-red-950/20 p-3 rounded-r-[--radius-sm]">
                <p className="text-sm font-medium">{v.constraintName}</p>
                <p className="text-xs text-[--text-secondary] mt-0.5">{v.message}</p>
              </div>
            ))}
            {softConflicts.map((v, i) => (
              <div key={i} className="border-l-2 border-orange-400 bg-orange-50/50 dark:bg-orange-950/20 p-3 rounded-r-[--radius-sm]">
                <p className="text-sm font-medium">{v.constraintName}</p>
                <p className="text-xs text-[--text-secondary] mt-0.5">{v.message}</p>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}