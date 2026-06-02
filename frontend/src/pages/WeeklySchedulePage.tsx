import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { CalendarOff } from 'lucide-react';
import { toast } from 'sonner';
import { getWeeklySchedule } from '../api/weeklySchedule';
import { WeeklyGrid } from '../components/schedule/WeeklyGrid';
import { WeeklyScheduleDTO } from '../types';
import { Button } from '../components/ui/Button';
import { Spinner } from '../components/ui/Spinner';

export function WeeklySchedulePage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [data, setData] = useState<WeeklyScheduleDTO | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    getWeeklySchedule(Number(id))
      .then(setData)
      .catch(() => toast.error('Failed to load schedule'))
      .finally(() => setLoading(false));
  }, [id]);

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
        <p className="display-sm text-[--muted-foreground]">No schedule generated yet</p>
        <Button onClick={() => navigate('/generate')}>Generate Schedule</Button>
      </div>
    );
  }

  return (
    <div>
      <WeeklyGrid data={data} scheduleId={Number(id)} showFilter showExport />
    </div>
  );
}
