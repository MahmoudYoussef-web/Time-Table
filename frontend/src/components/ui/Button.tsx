import { ButtonHTMLAttributes, forwardRef } from 'react';
import { cn } from '../../lib/utils';
import { Spinner } from './Spinner';

type Variant = 'primary' | 'secondary' | 'ghost' | 'link' | 'danger';
type Size = 'sm' | 'md' | 'lg';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
  size?: Size;
  loading?: boolean;
}

const variantStyles: Record<Variant, string> = {
  primary:   'bg-[--primary] text-[--primary-foreground] border-transparent',
  secondary: 'bg-transparent text-[--foreground] border-[--foreground]',
  ghost:     'bg-transparent border border-[--border] text-[--foreground] hover:bg-[--muted]',
  link:      'bg-transparent text-[--foreground] underline border-none p-0',
  danger:    'bg-[--destructive] text-white border-transparent',
};

const sizeStyles: Record<Size, string> = {
  sm: 'px-3 py-1 text-sm min-h-[32px]',
  md: 'px-4 py-2 text-base min-h-[40px]',
  lg: 'px-6 py-3 text-lg min-h-[48px]',
};

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ variant = 'primary', size = 'md', loading, disabled, children, className, ...props }, ref) => (
    <button
      ref={ref}
      disabled={disabled || loading}
      className={cn(
        'rounded-[--radius-sm] cursor-pointer border inline-flex items-center justify-center gap-2 transition-opacity',
        variantStyles[variant],
        sizeStyles[size],
        (disabled || loading) && 'opacity-50 cursor-not-allowed',
        className
      )}
      {...props}
    >
      {loading && <Spinner size="sm" />}
      {children}
    </button>
  )
);
