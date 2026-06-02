import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { EnrollmentRequest } from '../../types';
import { Select } from '../ui/Select';
import { Button } from '../ui/Button';

const schema = z.object({
  studentId: z.number().positive(),
  sectionId: z.number().positive(),
  status: z.string().min(1, 'Status is required'),
});

type FormData = z.infer<typeof schema>;

interface EnrollmentFormProps {
  defaultValues?: Partial<FormData>;
  onSubmit: (data: EnrollmentRequest) => Promise<void>;
  onCancel: () => void;
  students: { id: number; fullName: string }[];
  sections: { id: number; name: string }[];
}

export function EnrollmentForm({ defaultValues, onSubmit, onCancel, students, sections }: EnrollmentFormProps) {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues,
  });

  const handleFormSubmit = (formData: FormData) => {
    return onSubmit(formData as EnrollmentRequest);
  };

  return (
    <form onSubmit={handleSubmit(handleFormSubmit)}>
      <Select label="Student" {...register('studentId', { valueAsNumber: true })} error={errors.studentId?.message}>
        <option value="">Select student</option>
        {students.map(s => <option key={s.id} value={s.id}>{s.fullName}</option>)}
      </Select>
      <Select label="Section" {...register('sectionId', { valueAsNumber: true })} error={errors.sectionId?.message}>
        <option value="">Select section</option>
        {sections.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
      </Select>
      <Select label="Status" {...register('status')} error={errors.status?.message}>
        <option value="">Select status</option>
        <option value="ACTIVE">Active</option>
        <option value="DROPPED">Dropped</option>
        <option value="COMPLETED">Completed</option>
      </Select>
      <div className="flex gap-2 mt-4">
        <Button type="submit" loading={isSubmitting}>Save</Button>
        <Button type="button" variant="secondary" onClick={onCancel}>Cancel</Button>
      </div>
    </form>
  );
}
