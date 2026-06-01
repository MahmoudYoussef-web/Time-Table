import { useState, useEffect, useRef } from 'react';
import { getJobStatus } from '../api/schedules';
import { ScheduleGenerationJob } from '../types';

export function useScheduleJob(jobId: string | null) {
  const [job, setJob]         = useState<ScheduleGenerationJob | null>(null);
  const [elapsed, setElapsed] = useState(0);
  const intervalRef           = useRef<ReturnType<typeof setInterval> | null>(null);
  const timerRef              = useRef<ReturnType<typeof setInterval> | null>(null);

  useEffect(() => {
    if (!jobId) return;

    let isMounted = true;

    timerRef.current = setInterval(() => {
      if (isMounted) setElapsed(e => e + 1);
    }, 1000);

    const poll = async () => {
      try {
        const data = await getJobStatus(jobId);
        if (!isMounted) return;
        setJob(data);
        if (data.status === 'COMPLETED' || data.status === 'FAILED') {
          clearInterval(intervalRef.current!);
          clearInterval(timerRef.current!);
        }
      } catch {
        if (isMounted) clearInterval(intervalRef.current!);
      }
    };

    poll();
    intervalRef.current = setInterval(poll, 2000);

    return () => {
      isMounted = false;
      if (intervalRef.current) clearInterval(intervalRef.current);
      if (timerRef.current)    clearInterval(timerRef.current);
    };
  }, [jobId]);

  return { job, elapsed };
}
