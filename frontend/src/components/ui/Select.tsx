import { SelectHTMLAttributes, forwardRef } from 'react';
import { AlertCircle } from 'lucide-react';
import { cn } from '../../lib/utils';

interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label?: string;
  error?: string;
}

export const Select = forwardRef<HTMLSelectElement, SelectProps>(
  ({ label, error, className, children, ...props }, ref) => (
    <div className="mb-3">
      {label && <label className="label-md block mb-1">{label}</label>}
      <select
        ref={ref}
        className={cn(
          'border border-[--border] rounded-[--radius-sm] bg-[--background] px-3 py-2 w-full body-md',
          'focus:outline-none focus:ring-1 focus:ring-[--primary]',
          className
        )}
        {...props}
      >
        {children}
      </select>
      {error && (
        <p className="text-[--destructive] body-md mt-1 flex items-center gap-1">
          <AlertCircle size={14} />
          {error}
        </p>
      )}
    </div>
  )
);
