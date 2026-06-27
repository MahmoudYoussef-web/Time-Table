import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { InstructorRequest } from '../../types';
import { Input } from '../ui/Input';
import { Select } from '../ui/Select';
import { Button } from '../ui/Button';

const schema = z.object({
  name:         z.string().trim().min(1, 'Name is required'),
  email:        z.string().trim().email('Valid email is required'),
  password:     z.string().min(8, 'Password must be at least 8 characters').optional().or(z.literal('')),
  departmentId: z.number().min(1, 'Select a department'),
});

type FormData = z.infer<typeof schema>;

interface InstructorFormProps {
  defaultValues?: Partial<FormData>;
  onSubmit: (data: InstructorRequest) => Promise<void>;
  onCancel: () => void;
  departments: { id: number; name: string }[];
}

export function InstructorForm({ defaultValues, onSubmit, onCancel, departments }: InstructorFormProps) {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues,
  });

  const handleFormSubmit = (formData: FormData) => {
    return onSubmit(formData as InstructorRequest);
  };

  return (
    <form onSubmit={handleSubmit(handleFormSubmit)}>
      <Input label="Name" {...register('name')} error={errors.name?.message} />
      <Input label="Email" {...register('email')} error={errors.email?.message} />
      <Input label="Password" type="password" {...register('password')} error={errors.password?.message} />
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
