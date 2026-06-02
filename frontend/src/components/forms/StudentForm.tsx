import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { StudentRequest } from '../../types';
import { Input } from '../ui/Input';
import { Select } from '../ui/Select';
import { Button } from '../ui/Button';

const schema = z.object({
  userId: z.number().positive(),
  academicYear: z.string().min(1, 'Academic year is required'),
  level: z.number().positive(),
  departmentId: z.number().positive(),
});

type FormData = z.infer<typeof schema>;

interface StudentFormProps {
  defaultValues?: Partial<FormData>;
  onSubmit: (data: StudentRequest) => Promise<void>;
  onCancel: () => void;
  departments: { id: number; name: string }[];
}

export function StudentForm({ defaultValues, onSubmit, onCancel, departments }: StudentFormProps) {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues,
  });

  const handleFormSubmit = (formData: FormData) => {
    return onSubmit(formData as StudentRequest);
  };

  return (
    <form onSubmit={handleSubmit(handleFormSubmit)}>
      <Input label="User ID" type="number" {...register('userId', { valueAsNumber: true })} error={errors.userId?.message} />
      <Input label="Academic Year" placeholder="2025/2026" {...register('academicYear')} error={errors.academicYear?.message} />
      <Input label="Level" type="number" {...register('level', { valueAsNumber: true })} error={errors.level?.message} />
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
