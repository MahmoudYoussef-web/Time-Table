import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { SemesterRequest } from '../../types';
import { Input } from '../ui/Input';
import { Select } from '../ui/Select';
import { Button } from '../ui/Button';

const schema = z.object({
  name:      z.string().min(1, 'Name is required'),
  startDate: z.string().min(1, 'Start date is required'),
  endDate:   z.string().min(1, 'End date is required'),
  status:    z.enum(['DRAFT', 'PUBLISHED', 'CLOSED']),
});

type FormData = z.infer<typeof schema>;

interface SemesterFormProps {
  defaultValues?: Partial<FormData>;
  onSubmit: (data: SemesterRequest) => Promise<void>;
  onCancel: () => void;
}

export function SemesterForm({ defaultValues, onSubmit, onCancel }: SemesterFormProps) {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues,
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <Input label="Semester Name" {...register('name')} error={errors.name?.message} />
      <Input label="Start Date" type="date" {...register('startDate')} error={errors.startDate?.message} />
      <Input label="End Date" type="date" {...register('endDate')} error={errors.endDate?.message} />
      <Select label="Status" {...register('status')} error={errors.status?.message}>
        <option value="">Select status</option>
        <option value="DRAFT">Draft</option>
        <option value="PUBLISHED">Published</option>
        <option value="CLOSED">Closed</option>
      </Select>
      <div className="flex gap-2 mt-4">
        <Button type="submit" loading={isSubmitting}>Save</Button>
        <Button type="button" variant="secondary" onClick={onCancel}>Cancel</Button>
      </div>
    </form>
  );
}
