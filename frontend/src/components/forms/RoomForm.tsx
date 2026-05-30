import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { RoomRequest } from '../../types';
import { Input } from '../ui/Input';
import { Select } from '../ui/Select';
import { Button } from '../ui/Button';

const schema = z.object({
  building:  z.string().min(1, 'Building is required'),
  roomNumber: z.string().min(1, 'Room number is required'),
  capacity:  z.number().min(1, 'Capacity must be at least 1'),
  roomType:  z.enum(['LECTURE_HALL', 'LAB', 'SEMINAR_ROOM']),
});

type FormData = z.infer<typeof schema>;

interface RoomFormProps {
  defaultValues?: Partial<FormData>;
  onSubmit: (data: RoomRequest) => Promise<void>;
  onCancel: () => void;
}

export function RoomForm({ defaultValues, onSubmit, onCancel }: RoomFormProps) {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues,
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <Input label="Building" {...register('building')} error={errors.building?.message} />
      <Input label="Room Number" {...register('roomNumber')} error={errors.roomNumber?.message} />
      <Input label="Capacity" type="number" {...register('capacity', { valueAsNumber: true })} error={errors.capacity?.message} />
      <Select label="Room Type" {...register('roomType')} error={errors.roomType?.message}>
        <option value="">Select room type</option>
        <option value="LECTURE_HALL">Lecture Hall</option>
        <option value="LAB">Lab</option>
        <option value="SEMINAR_ROOM">Seminar Room</option>
      </Select>
      <div className="flex gap-2 mt-4">
        <Button type="submit" loading={isSubmitting}>Save</Button>
        <Button type="button" variant="secondary" onClick={onCancel}>Cancel</Button>
      </div>
    </form>
  );
}
