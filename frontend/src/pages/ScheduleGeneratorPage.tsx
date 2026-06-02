import { useState, useEffect, useCallback } from 'react';
import { motion } from 'framer-motion';
import { Zap } from 'lucide-react';
import { toast } from 'sonner';
import { Semester } from '../types';
import { getSemesters } from '../api/semesters';
import { generateSchedule, downloadPdf, downloadExcel, downloadPng } from '../api/schedules';
import { useScheduleJob } from '../hooks/useScheduleJob';
import { useScheduleStore } from '../store/scheduleStore';
import { GenerationStatus } from '../components/schedule/GenerationStatus';
import { useNavigate } from 'react-router-dom';

const container = { hidden: {}, show: { transition: { staggerChildren: 0.06 } } };
const item = { hidden: { opacity: 0, y: 12 }, show: { opacity: 1, y: 0 } };

export function ScheduleGeneratorPage() {
  const navigate = useNavigate();
  const [semesters, setSemesters] = useState<Semester[]>([]);
  const [selectedSemesterId, setSelectedSemesterId] = useState<number | ''>('');
  const [loadingSemesters, setLoadingSemesters] = useState(true);

  const [jobId, setJobId] = useState<string | null>(null);
  const { job, elapsed } = useScheduleJob(jobId);
  const [isGenerating, setIsGenerating] = useState(false);
  const { setLastScheduleId } = useScheduleStore();

  useEffect(() => {
    getSemesters()
      .then(setSemesters)
      .catch(() => toast.error('Failed to load semesters'))
      .finally(() => setLoadingSemesters(false));
  }, []);

  useEffect(() => {
    if (job && job.status === 'COMPLETED' && job.scheduleId) {
      setLastScheduleId(job.scheduleId);
    }
  }, [job, setLastScheduleId]);

  const handleGenerate = useCallback(async () => {
    if (!selectedSemesterId) {
      toast.error('Please select a semester');
      return;
    }
    setIsGenerating(true);
    try {
      const id = await generateSchedule(selectedSemesterId as number);
      setJobId(id);
    } catch {
      toast.error('Failed to start generation');
      setIsGenerating(false);
    }
  }, [selectedSemesterId]);

  const handleRetry = () => {
    setJobId(null);
    setIsGenerating(false);
  };

  const handleViewSchedule = (scheduleId: number) => {
    navigate(`/schedules/${scheduleId}/weekly`);
  };

  const handleDownloadPdf = () => {
    if (job?.scheduleId) downloadPdf(job.scheduleId);
  };

  const handleDownloadExcel = () => {
    if (job?.scheduleId) downloadExcel(job.scheduleId);
  };

  const handleDownloadPng = () => {
    if (job?.scheduleId) downloadPng(job.scheduleId);
  };

  const hasJob = !!(jobId && job);

  return (
    <div>
      <motion.div variants={container} initial="hidden" animate="show" className="max-w-[600px] mx-auto">
        {!jobId && !isGenerating && (
          <motion.div variants={item}>
            <h1 className="display-md mb-2">Generate Schedule</h1>
            <p className="body-lg mb-8">Select a semester and run the constraint-based scheduler.</p>

            <div className="mb-6">
              <label className="label-sm text-[--muted-foreground] mb-1 block">Semester</label>
              {loadingSemesters ? (
                <div className="w-full h-10 bg-[--muted] rounded-[--radius-sm] animate-pulse" />
              ) : (
                <select
                  value={selectedSemesterId}
                  onChange={(e) => setSelectedSemesterId(e.target.value ? Number(e.target.value) : '')}
                  className="bg-transparent border-b border-[--border] h-11 px-0 py-2 body-md w-full focus:outline-none focus:border-[--foreground] transition-colors duration-200"
                >
                  <option value="">Select semester...</option>
                  {semesters.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.name} ({s.status})
                    </option>
                  ))}
                </select>
              )}
            </div>

            <button
              onClick={handleGenerate}
              disabled={!selectedSemesterId}
              className="w-full max-w-[320px] h-11 bg-[--primary] text-[--primary-foreground] rounded-[--radius-md] font-medium cursor-pointer flex items-center justify-center gap-2 disabled:opacity-50 hover:opacity-90 transition-opacity"
            >
              <Zap size={16} /> Generate Schedule
            </button>
          </motion.div>
        )}

        {(isGenerating && !job) && (
          <motion.div variants={item} className="text-center py-12">
            <div className="w-6 h-6 border-2 border-[--border] border-t-[--foreground] rounded-full animate-spin mx-auto mb-4" />
            <p className="body-lg text-[--muted-foreground]">Starting generation job...</p>
          </motion.div>
        )}

        {hasJob && (
          <motion.div variants={item}>
            <GenerationStatus
              job={job}
              elapsedSeconds={elapsed}
              onViewSchedule={handleViewSchedule}
              onRetry={handleRetry}
              onDownloadPdf={handleDownloadPdf}
              onDownloadExcel={handleDownloadExcel}
              onDownloadPng={handleDownloadPng}
            />
          </motion.div>
        )}
      </motion.div>
    </div>
  );
}
