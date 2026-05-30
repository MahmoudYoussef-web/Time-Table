import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { SectionRequest } from '../../types';
import { Input } from '../ui/Input';
import { Select } from '../ui/Select';
import { Button } from '../ui/Button';

const schema = z.object({
  name:         z.string().min(1, 'Name is required'),
  capacity:     z.number().min(1, 'Capacity must be at least 1'),
  yearLevel:    z.enum(['FIRST', 'SECOND', 'THIRD', 'FOURTH']),
  sessionType:  z.enum(['LECTURE', 'LAB', 'TUTORIAL', 'SEMINAR', 'SECTION']),
  courseId:     z.number().min(1, 'Select a course'),
  instructorId: z.number().min(1, 'Select an instructor'),
  semesterId:   z.number().min(1, 'Select a semester'),
});

type FormData = z.infer<typeof schema>;

interface SectionFormProps {
  defaultValues?: Partial<FormData>;
  onSubmit: (data: SectionRequest) => Promise<void>;
  onCancel: () => void;
  courses: { id: number; code: string; name: string }[];
  instructors: { id: number; name: string }[];
  semesters: { id: number; name: string }[];
}

export function SectionForm({ defaultValues, onSubmit, onCancel, courses, instructors, semesters }: SectionFormProps) {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues,
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <Input label="Section Name" {...register('name')} error={errors.name?.message} />
      <Input label="Capacity" type="number" {...register('capacity', { valueAsNumber: true })} error={errors.capacity?.message} />
      <Select label="Year Level" {...register('yearLevel')} error={errors.yearLevel?.message}>
        <option value="">Select year</option>
        <option value="FIRST">First</option>
        <option value="SECOND">Second</option>
        <option value="THIRD">Third</option>
        <option value="FOURTH">Fourth</option>
      </Select>
      <Select label="Session Type" {...register('sessionType')} error={errors.sessionType?.message}>
        <option value="">Select session type</option>
        <option value="LECTURE">Lecture</option>
        <option value="LAB">Lab</option>
        <option value="TUTORIAL">Tutorial</option>
        <option value="SEMINAR">Seminar</option>
        <option value="SECTION">Section</option>
      </Select>
      <Select label="Course" {...register('courseId', { valueAsNumber: true })} error={errors.courseId?.message}>
        <option value="">Select course</option>
        {courses.map(c => <option key={c.id} value={c.id}>{c.code} — {c.name}</option>)}
      </Select>
      <Select label="Instructor" {...register('instructorId', { valueAsNumber: true })} error={errors.instructorId?.message}>
        <option value="">Select instructor</option>
        {instructors.map(i => <option key={i.id} value={i.id}>{i.name}</option>)}
      </Select>
      <Select label="Semester" {...register('semesterId', { valueAsNumber: true })} error={errors.semesterId?.message}>
        <option value="">Select semester</option>
        {semesters.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
      </Select>
      <div className="flex gap-2 mt-4">
        <Button type="submit" loading={isSubmitting}>Save</Button>
        <Button type="button" variant="secondary" onClick={onCancel}>Cancel</Button>
      </div>
    </form>
  );
}
