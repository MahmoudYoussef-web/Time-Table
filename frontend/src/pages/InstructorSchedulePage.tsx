import { useEffect, useState } from 'react';
import { CalendarOff, UserX } from 'lucide-react';
import { getMySchedule } from '../api/weeklySchedule';
import { WeeklyGrid } from '../components/schedule/WeeklyGrid';
import { WeeklyScheduleDTO } from '../types';
import { Spinner } from '../components/ui/Spinner';

export function InstructorSchedulePage() {
  const [data, setData] = useState<WeeklyScheduleDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [notInstructor, setNotInstructor] = useState(false);

  useEffect(() => {
    getMySchedule()
      .then(setData)
      .catch((err) => {
        if (err?.response?.status === 404) {
          setNotInstructor(true);
        }
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center py-[76px]">
        <Spinner size="lg" />
      </div>
    );
  }

  if (notInstructor) {
    return (
      <div className="flex flex-col items-center justify-center h-full gap-4">
        <UserX className="w-12 h-12 text-[--muted-foreground]" />
        <p className="display-md text-[--muted-foreground]">No instructor profile found</p>
        <p className="body-md text-[--muted-foreground]">This account is not linked to an instructor profile.</p>
      </div>
    );
  }

  if (!data || !data.days?.length) {
    return (
      <div className="flex flex-col items-center justify-center h-full gap-4">
        <CalendarOff className="w-12 h-12 text-[--muted-foreground]" />
        <p className="display-md text-[--muted-foreground]">No schedule assigned yet</p>
      </div>
    );
  }

  return (
    <div>
      <h1 className="display-lg mb-6">My Schedule</h1>
      <div className="bg-[--card] border border-[--border] rounded-[--radius-md] p-4">
        <WeeklyGrid data={data} showFilter={false} showExport={false} />
      </div>
    </div>
  );
}
