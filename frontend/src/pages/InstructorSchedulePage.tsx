import { useEffect, useState } from 'react';
import { CalendarOff } from 'lucide-react';
import toast from 'react-hot-toast';
import { getMySchedule } from '../api/weeklySchedule';
import { WeeklyGrid } from '../components/schedule/WeeklyGrid';
import { WeeklyScheduleDTO } from '../types';
import { Spinner } from '../components/ui/Spinner';

export function InstructorSchedulePage() {
  const [data, setData] = useState<WeeklyScheduleDTO | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getMySchedule()
      .then(setData)
      .catch(() => toast.error('Failed to load your schedule'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center py-[76px]">
        <Spinner size="lg" />
      </div>
    );
  }

  if (!data || !data.days?.length) {
    return (
      <div className="flex flex-col items-center justify-center h-full gap-4">
        <CalendarOff className="w-12 h-12 text-[--muted-foreground]" />
        <p className="headline-md text-[--muted-foreground]">No schedule assigned yet</p>
      </div>
    );
  }

  return (
    <div>
      <h1 className="headline-lg mb-6">My Schedule</h1>
      <div className="bg-[--card] border border-[--border] rounded-[--radius-md] p-4">
        <WeeklyGrid data={data} showFilter={false} showExport={false} />
      </div>
    </div>
  );
}
