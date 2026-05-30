import { HTMLAttributes, forwardRef } from 'react';
import { cn } from '../../lib/utils';

interface CardProps extends HTMLAttributes<HTMLDivElement> {
  hover?: boolean;
}

export const Card = forwardRef<HTMLDivElement, CardProps>(
  ({ className, children, hover, ...props }, ref) => (
    <div
      ref={ref}
      className={cn(
        'bg-[--card] border border-[--border] rounded-[--radius-md] p-4',
        hover && 'hover:brightness-95 transition-all cursor-pointer',
        className
      )}
      {...props}
    >
      {children}
    </div>
  )
);
