import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useMemo } from 'react';
import { z } from 'zod';
import { StudentRequest } from '../../types';
import { Input } from '../ui/Input';
import { Select } from '../ui/Select';
import { Button } from '../ui/Button';

interface StudentFormProps {
  defaultValues?: Partial<StudentFormData>;
  onSubmit: (data: StudentRequest) => Promise<void>;
  onCancel: () => void;
  departments: { id: number; name: string }[];
  isEditing?: boolean;
}

type StudentFormData = {
  fullName: string;
  email: string;
  password?: string;
  academicYear: string;
  level: number;
  departmentId: number;
};

const createSchema = (isEditing: boolean) => z.object({
  fullName: z.string().min(1, 'Full name is required'),
  email: z.string().email('Invalid email'),
  password: isEditing
    ? z.string().min(8, 'Password must be at least 8 characters').optional().or(z.literal(''))
    : z.string().min(8, 'Password must be at least 8 characters'),
  academicYear: z.string().min(1, 'Academic year is required'),
  level: z.coerce.number().positive(),
  departmentId: z.coerce.number().positive(),
});

export function StudentForm({ defaultValues, onSubmit, onCancel, departments, isEditing = false }: StudentFormProps) {
  const schema = useMemo(() => createSchema(isEditing), [isEditing]);
  type FormData = z.infer<typeof schema>;

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema) as any,
    defaultValues,
  });

  return (
    <form onSubmit={handleSubmit((data) => onSubmit(data as StudentRequest))}>
      <Input label="Full Name" {...register('fullName')} error={errors.fullName?.message} />
      <Input label="Email" type="email" {...register('email')} error={errors.email?.message} />
      {!isEditing && (
        <Input label="Password" type="password" {...register('password')} error={errors.password?.message} />
      )}
      <Input label="Academic Year" placeholder="2025/2026" {...register('academicYear')} error={errors.academicYear?.message} />
      <Input label="Level" type="number" {...register('level')} error={errors.level?.message} />
      <Select label="Department" {...register('departmentId')} error={errors.departmentId?.message}>
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
