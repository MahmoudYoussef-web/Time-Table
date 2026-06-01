import { useState, useEffect, useCallback } from 'react';
import { motion } from 'framer-motion';
import { Zap, CheckCircle, RefreshCw, AlertTriangle } from 'lucide-react';
import toast from 'react-hot-toast';
import { Semester } from '../types';
import { getSemesters } from '../api/semesters';
import { generateSchedule, getJobStatus, getConflicts, downloadPdf, downloadExcel } from '../api/schedules';
import { useScheduleJob } from '../hooks/useScheduleJob';
import { ScheduleGenerationJob, ConstraintViolation } from '../types';
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
  const [conflicts, setConflicts] = useState<ConstraintViolation[]>([]);
  const [scheduleId, setScheduleId] = useState<number | null>(null);
  const [isGenerating, setIsGenerating] = useState(false);

  useEffect(() => {
    getSemesters()
      .then(setSemesters)
      .catch(() => toast.error('Failed to load semesters'))
      .finally(() => setLoadingSemesters(false));
  }, []);

  useEffect(() => {
    if (job && job.status === 'COMPLETED' && job.scheduleId) {
      setScheduleId(job.scheduleId);
      getConflicts(job.scheduleId)
        .then(setConflicts)
        .catch(() => {});
    }
  }, [job]);

  const handleGenerate = useCallback(async () => {
    if (!selectedSemesterId) {
      toast.error('Please select a semester');
      return;
    }
    setIsGenerating(true);
    setConflicts([]);
    setScheduleId(null);
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
    setConflicts([]);
    setScheduleId(null);
  };

  return (
    <div>
      <motion.div variants={container} initial="hidden" animate="show" className="max-w-[600px] mx-auto">
        {/* Idle state */}
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

        {/* Running state */}
        {(isGenerating && !job) && (
          <motion.div variants={item} className="text-center py-12">
            <RefreshCw size={32} className="mx-auto mb-4 text-[--muted-foreground] animate-spin" />
            <p className="body-lg text-[--muted-foreground]">Starting generation job...</p>
          </motion.div>
        )}

        {(jobId && job) && (
          <motion.div variants={item}>
            {job.status === 'RUNNING' && (
              <div className="space-y-6 py-8">
                <span className="overline block">RUNNING JOB</span>
                <h2 className="display-sm">Generating your schedule...</h2>
                <p className="mono-sm text-[--muted-foreground]">Job ID: {jobId.slice(0, 8)}...</p>
                <div className="h-1 bg-[--muted] rounded-full overflow-hidden">
                  <div className="h-full w-1/2 bg-[--primary] rounded-full animate-pulse" />
                </div>
                <p className="label-sm text-[--muted-foreground]">This usually takes 5–30 seconds · {elapsed}s elapsed</p>
              </div>
            )}

            {job.status === 'COMPLETED' && (
              <div className="space-y-6 py-8">
                <span className="overline block">COMPLETED</span>
                <h2 className="display-sm">Schedule ready</h2>
                {scheduleId && (
                  <p className="label-sm text-[--muted-foreground]">Schedule #{scheduleId} · {conflicts.length > 0 ? `${conflicts.length} conflicts` : 'No conflicts'}</p>
                )}
                <div className="flex flex-wrap gap-3">
                  <button
                    onClick={() => navigate(`/schedules/${scheduleId}/weekly`)}
                    className="inline-flex items-center gap-2 px-5 h-10 bg-[--primary] text-[--primary-foreground] rounded-[--radius-md] text-sm font-medium cursor-pointer hover:opacity-90 transition-opacity"
                  >
                    View Weekly
                  </button>
                  <button
                    onClick={() => scheduleId && downloadPdf(scheduleId)}
                    className="inline-flex items-center gap-2 px-5 h-10 border border-[--border] rounded-[--radius-md] text-sm font-medium cursor-pointer hover:bg-[--muted] transition-colors"
                  >
                    Download PDF
                  </button>
                  <button
                    onClick={() => scheduleId && downloadExcel(scheduleId)}
                    className="inline-flex items-center gap-2 px-5 h-10 border border-[--border] rounded-[--radius-md] text-sm font-medium cursor-pointer hover:bg-[--muted] transition-colors"
                  >
                    Download Excel
                  </button>
                </div>
                {conflicts.length > 0 && (
                  <div className="mt-4">
                    <span className="overline block mb-3">CONSTRAINT VIOLATIONS</span>
                    {conflicts.map((v, i) => (
                      <div key={i} className="border-l-2 border-[--destructive] bg-[--destructive]/5 p-3 rounded-[--radius-sm] mb-2">
                        <p className="label-sm font-semibold">{v.constraintName}</p>
                        <p className="label-sm text-[--muted-foreground]">{v.message}</p>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}

            {job.status === 'FAILED' && (
              <div className="space-y-6 py-8">
                <span className="overline block text-[--destructive]">FAILED</span>
                <h2 className="display-sm">Generation failed</h2>
                <p className="body-lg">An error occurred during schedule generation. Please try again.</p>
                <button
                  onClick={handleRetry}
                  className="inline-flex items-center gap-2 px-5 h-10 bg-[--primary] text-[--primary-foreground] rounded-[--radius-md] text-sm font-medium cursor-pointer hover:opacity-90 transition-opacity"
                >
                  Try Again
                </button>
              </div>
            )}
          </motion.div>
        )}
      </motion.div>
    </div>
  );
}
