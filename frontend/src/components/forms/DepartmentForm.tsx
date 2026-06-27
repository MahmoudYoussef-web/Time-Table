import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { DepartmentRequest } from '../../types';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';

const schema = z.object({
  code: z.string().trim().min(1, 'Code is required'),
  name: z.string().trim().min(1, 'Name is required'),
});

type FormData = z.infer<typeof schema>;

interface DepartmentFormProps {
  defaultValues?: Partial<FormData>;
  onSubmit: (data: DepartmentRequest) => Promise<void>;
  onCancel: () => void;
}

export function DepartmentForm({ defaultValues, onSubmit, onCancel }: DepartmentFormProps) {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues,
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <Input label="Code" {...register('code')} error={errors.code?.message} />
      <Input label="Name" {...register('name')} error={errors.name?.message} />
      <div className="flex gap-2 mt-4">
        <Button type="submit" loading={isSubmitting}>Save</Button>
        <Button type="button" variant="secondary" onClick={onCancel}>Cancel</Button>
      </div>
    </form>
  );
}
