import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { TimeSlotRequest } from '../../types';
import { Select } from '../ui/Select';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';

const schema = z.object({
  day:       z.enum(['SATURDAY','SUNDAY','MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY']),
  startTime: z.string().min(1, 'Start time is required'),
  endTime:   z.string().min(1, 'End time is required'),
});

type FormData = z.infer<typeof schema>;
const days = ['SATURDAY','SUNDAY','MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY'] as const;

interface TimeslotFormProps {
  defaultValues?: Partial<FormData>;
  onSubmit: (data: TimeSlotRequest) => Promise<void>;
  onCancel: () => void;
}

export function TimeslotForm({ defaultValues, onSubmit, onCancel }: TimeslotFormProps) {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues,
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <Select label="Day" {...register('day')} error={errors.day?.message}>
        <option value="">Select day</option>
        {days.map(d => <option key={d} value={d}>{d.charAt(0) + d.slice(1).toLowerCase()}</option>)}
      </Select>
      <Input label="Start Time" type="time" {...register('startTime')} error={errors.startTime?.message} />
      <Input label="End Time" type="time" {...register('endTime')} error={errors.endTime?.message} />
      <div className="flex gap-2 mt-4">
        <Button type="submit" loading={isSubmitting}>Save</Button>
        <Button type="button" variant="secondary" onClick={onCancel}>Cancel</Button>
      </div>
    </form>
  );
}
