import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { CourseRequest } from '../../types';
import { Input } from '../ui/Input';
import { Select } from '../ui/Select';
import { Button } from '../ui/Button';

const schema = z.object({
  code:         z.string().min(1, 'Code is required'),
  name:         z.string().min(1, 'Name is required'),
  creditHours:  z.number().min(1).max(6),
  departmentId: z.number().min(1, 'Select a department'),
});

type FormData = z.infer<typeof schema>;

interface CourseFormProps {
  defaultValues?: Partial<FormData>;
  onSubmit: (data: CourseRequest) => Promise<void>;
  onCancel: () => void;
  departments: { id: number; name: string }[];
}

export function CourseForm({ defaultValues, onSubmit, onCancel, departments }: CourseFormProps) {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues,
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <Input label="Course Code" {...register('code')} error={errors.code?.message} />
      <Input label="Course Name" {...register('name')} error={errors.name?.message} />
      <Input label="Credit Hours" type="number" {...register('creditHours', { valueAsNumber: true })} error={errors.creditHours?.message} />
      <Select label="Department" {...register('departmentId', { valueAsNumber: true })} error={errors.departmentId?.message}>
        <option value="">Select department</option>
        {departments.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
      </Select>
      <div className="flex gap-2 mt-4">
        <Button type="submit" loading={isSubmitting}>Save</Button>
        <Button type="button" variant="secondary" onClick={onCancel}>Cancel</Button>
      </div>
    </form>
  );
}
