import { InputHTMLAttributes, forwardRef } from 'react';
import { AlertCircle } from 'lucide-react';
import { cn } from '../../lib/utils';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  variant?: 'outline' | 'underline';
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, variant = 'outline', className, ...props }, ref) => (
    <div className="mb-3">
      {label && <label className="label-md block mb-1">{label}</label>}
      <input
        ref={ref}
        className={cn(
          variant === 'outline'
            ? 'border border-[--border] rounded-[--radius-sm] bg-[--background] px-3 py-2 w-full body-md focus:outline-none focus:ring-1 focus:ring-[--primary]'
            : 'bg-transparent border-b border-[--border] h-11 px-0 py-2 body-md focus:outline-none focus:border-[--foreground] transition-colors duration-200 placeholder:text-[--muted-foreground] w-full',
          className
        )}
        {...props}
      />
      {error && (
        <p className="text-[--destructive] body-md mt-1 flex items-center gap-1">
          <AlertCircle size={14} />
          {error}
        </p>
      )}
    </div>
  )
);
