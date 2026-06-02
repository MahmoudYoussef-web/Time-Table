import { useState } from 'react';
import { useNavigate, Navigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useAuth } from '../hooks/useAuth';
import { Input } from '../components/ui/Input';
import { Button } from '../components/ui/Button';

const schema = z.object({
  email:    z.string().email('Valid email is required'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
});

type FormData = z.infer<typeof schema>;

export function LoginPage() {
  const navigate = useNavigate();
  const { loginUser, isAuthenticated } = useAuth();
  const [error, setError] = useState('');

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  if (isAuthenticated) return <Navigate to="/dashboard" replace />;

  const onSubmit = async (data: FormData) => {
    setError('');
    const result = await loginUser(data.email, data.password);
    if (result.success) {
      navigate('/dashboard');
    } else {
      setError(result.message || 'Invalid credentials');
    }
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-[--background]">
      <div className="max-w-sm w-full bg-[--card] border border-[--border] rounded-[--radius-md] p-[38px]">
        <h1 className="display-xl text-center mb-[38px]">Timetable Scheduler</h1>
        <form onSubmit={handleSubmit(onSubmit)}>
          <Input label="Email" {...register('email')} error={errors.email?.message} />
          <Input label="Password" type="password" {...register('password')} error={errors.password?.message} />
          <Button type="submit" loading={isSubmitting} className="w-full mt-[22px]">Sign In</Button>
        </form>
        {error && <p className="text-[--destructive] body-md mt-2 text-center">{error}</p>}
      </div>
    </div>
  );
}

