import { ConstraintViolation } from '../../types';

interface ConflictBadgeProps {
  violation: ConstraintViolation;
  type: 'hard' | 'soft';
}

export function ConflictBadge({ violation, type }: ConflictBadgeProps) {
  const colors = type === 'hard'
    ? 'border-l-2 border-[--destructive] bg-[--destructive]/5'
    : 'border-l-2 border-[--warning] bg-[--warning]/5';

  return (
    <div className={`${colors} p-3 rounded-[--radius-sm] mb-2`}>
      <p className="text-sm font-semibold">{violation.constraintName}</p>
      <p className="text-sm text-[--muted-foreground]">{violation.message}</p>
    </div>
  );
}
