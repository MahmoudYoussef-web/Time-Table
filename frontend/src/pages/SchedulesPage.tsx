import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Calendar, CalendarDays, Download, FileText, Image, Lock, Table2, Eye, Trash2, CheckCircle, ShieldCheck } from 'lucide-react';
import { toast } from 'sonner';
import { ScheduleSummary } from '../types';
import * as schedulesApi from '../api/schedules';
import { Button } from '../components/ui/Button';
import { EmptyState } from '../components/ui/EmptyState';
import { ConfirmModal } from '../components/ui/ConfirmModal';

const STATUS_STYLES: Record<string, { bg: string; text: string }> = {
  DRAFT: { bg: '#F3F4F6', text: '#374151' },
  VALIDATED: { bg: '#DBEAFE', text: '#1E40AF' },
  LOCKED: { bg: '#FFF3CD', text: '#856404' },
  PUBLISHED: { bg: '#D4EDDA', text: '#155724' },
};

function formatFitness(score: number | null | undefined): string {
  if (score == null || isNaN(score)) return 'N/A';
  if (score === 0) return '0.00';
  if (Math.abs(score) < 0.01) return score.toExponential(2);
  return score.toFixed(2);
}

export function SchedulesPage() {
  const navigate = useNavigate();
  const [schedules, setSchedules] = useState<ScheduleSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [deleteTarget, setDeleteTarget] = useState<ScheduleSummary | null>(null);
  const [deleting, setDeleting] = useState(false);

  const fetchData = async () => {
    try {
      const data = await schedulesApi.getSchedules();
      setSchedules(data);
    } catch {
      toast.error('Failed to load schedules');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchData(); }, []);

  const handleDeleteConfirm = async () => {
    if (!deleteTarget) return;
    setDeleting(true);
    try {
      await schedulesApi.deleteSchedule(deleteTarget.id);
      toast.success('Schedule deleted');
      fetchData();
    } catch {
      toast.error('Failed to delete schedule');
    } finally {
      setDeleting(false);
      setDeleteTarget(null);
    }
  };

  const handleExport = async (id: number, format: 'pdf' | 'excel' | 'png') => {
    try {
      if (format === 'pdf') await schedulesApi.downloadPdf(id);
      else if (format === 'excel') await schedulesApi.downloadExcel(id);
      else await schedulesApi.downloadPng(id);
      toast.success(`Exported as ${format.toUpperCase()}`);
    } catch {
      toast.error(`Failed to export ${format.toUpperCase()}`);
    }
  };

  const handleValidate = async (id: number) => {
    try {
      await schedulesApi.validateSchedule(id);
      toast.success('Schedule validated');
      fetchData();
    } catch {
      toast.error('Failed to validate schedule');
    }
  };

  const handleLock = async (id: number) => {
    try {
      await schedulesApi.lockSchedule(id);
      toast.success('Schedule locked');
      fetchData();
    } catch {
      toast.error('Failed to lock schedule');
    }
  };

  if (loading) return <div className="h-32 bg-[--muted] rounded animate-pulse" />;

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h1 className="display-lg">Schedule History <span className="text-sm text-[--text-secondary] font-normal">({schedules.length})</span></h1>
      </div>
      {schedules.length === 0 ? (
        <EmptyState
          icon={<Calendar size={48} />}
          title="No schedules generated yet"
          description="Generate your first schedule from the Schedule Generator page"
          action={<Button onClick={() => navigate('/generate')}>Generate Schedule</Button>}
        />
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full border-collapse">
            <thead>
              <tr className="border-b border-[--border]">
                <th className="text-left py-3 px-3 text-sm font-medium text-[--text-secondary]">ID</th>
                <th className="text-left py-3 px-3 text-sm font-medium text-[--text-secondary]">Semester</th>
                <th className="text-left py-3 px-3 text-sm font-medium text-[--text-secondary]">Status</th>
                <th className="text-left py-3 px-3 text-sm font-medium text-[--text-secondary]">Fitness</th>
                <th className="text-left py-3 px-3 text-sm font-medium text-[--text-secondary]">Hard</th>
                <th className="text-left py-3 px-3 text-sm font-medium text-[--text-secondary]">Soft</th>
                <th className="text-left py-3 px-3 text-sm font-medium text-[--text-secondary]">Created</th>
                <th className="text-left py-3 px-3 text-sm font-medium text-[--text-secondary]">Actions</th>
              </tr>
            </thead>
            <tbody>
              {schedules.map(s => {
                const statusStyle = STATUS_STYLES[s.status] ?? { bg: '#F3F4F6', text: '#374151' };
                return (
                  <tr key={s.id} className="border-b border-[--border] hover:bg-[--muted] transition-colors">
                    <td className="py-3 px-3 text-sm">{s.id}</td>
                    <td className="py-3 px-3 text-sm">{s.semesterName}</td>
                    <td className="py-3 px-3 text-sm">
                      <span
                        className="inline-block px-2 py-0.5 rounded text-xs font-medium"
                        style={{ backgroundColor: statusStyle.bg, color: statusStyle.text }}
                      >
                        {s.status}
                      </span>
                    </td>
                    <td className="py-3 px-3 text-sm font-mono">{formatFitness(s.fitnessScore)}</td>
                    <td className="py-3 px-3 text-sm">
                      <span className={s.hardViolations > 0 ? 'text-red-600 font-medium' : ''}>{s.hardViolations}</span>
                    </td>
                    <td className="py-3 px-3 text-sm">{s.softViolations}</td>
                    <td className="py-3 px-3 text-sm text-[--text-secondary]">
                      {new Date(s.createdAt).toLocaleDateString()}
                    </td>
                    <td className="py-3 px-3 text-sm">
                      <div className="flex gap-1">
                        <button
                          title="View Details"
                          onClick={() => navigate(`/schedules/${s.id}`)}
                          className="p-1.5 rounded hover:bg-[--border] transition-colors"
                        ><Eye size={16} /></button>
                        <button
                          title="Weekly View"
                          onClick={() => navigate(`/schedules/${s.id}/weekly`)}
                          className="p-1.5 rounded hover:bg-[--border] transition-colors"
                        ><CalendarDays size={16} /></button>
                        <div className="relative group">
                          <button
                            title="Export"
                            className="p-1.5 rounded hover:bg-[--border] transition-colors"
                          ><Download size={16} /></button>
                          <div className="absolute right-0 top-full mt-1 bg-[--surface] border border-[--border] rounded-lg shadow-lg py-1 min-w-[160px] hidden group-hover:block z-10">
                            <button onClick={() => handleExport(s.id, 'pdf')} className="w-full flex items-center gap-2 px-3 py-1.5 text-sm hover:bg-[--muted]">
                              <FileText size={14} /> Export PDF
                            </button>
                            <button onClick={() => handleExport(s.id, 'excel')} className="w-full flex items-center gap-2 px-3 py-1.5 text-sm hover:bg-[--muted]">
                              <Table2 size={14} /> Export Excel
                            </button>
                            <button onClick={() => handleExport(s.id, 'png')} className="w-full flex items-center gap-2 px-3 py-1.5 text-sm hover:bg-[--muted]">
                              <Image size={14} /> Export PNG
                            </button>
                          </div>
                        </div>
                        {s.status === 'DRAFT' && (
                          <>
                            <button
                              title="Validate"
                              onClick={() => handleValidate(s.id)}
                              className="p-1.5 rounded hover:bg-[--border] transition-colors text-blue-600"
                            ><CheckCircle size={16} /></button>
                            <button
                              title="Delete"
                              onClick={() => setDeleteTarget(s)}
                              className="p-1.5 rounded hover:bg-[--border] transition-colors text-red-500"
                            ><Trash2 size={16} /></button>
                          </>
                        )}
                        {s.status === 'VALIDATED' && (
                          <button
                            title="Lock"
                            onClick={() => handleLock(s.id)}
                            className="p-1.5 rounded hover:bg-[--border] transition-colors text-orange-500"
                          ><Lock size={16} /></button>
                        )}
                        {s.status === 'LOCKED' && (
                          <button
                            title="Published"
                            className="p-1.5 rounded text-green-600"
                          ><ShieldCheck size={16} /></button>
                        )}
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
      <ConfirmModal
        isOpen={deleteTarget !== null}
        title="Delete Schedule"
        message={`Are you sure you want to delete schedule #${deleteTarget?.id}?`}
        onConfirm={handleDeleteConfirm}
        onCancel={() => setDeleteTarget(null)}
        loading={deleting}
      />
    </div>
  );
}
