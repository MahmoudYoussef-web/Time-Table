import React from 'react';
import { cn } from '../../lib/utils';
import { SessionType, SemesterStatus, YearLevel } from '../../types';

const baseBadge = 'rounded-[--radius-sm] px-2 py-0.5 label-sm inline-block font-medium';

const sessionStyles: Record<SessionType, string> = {
  LECTURE:  'bg-[--muted] text-[--foreground]',
  LAB:      'bg-[--muted] text-[--foreground]',
  SECTION:  'bg-[--muted] text-[--foreground]',
  TUTORIAL: 'bg-[--muted] text-[--foreground]',
  SEMINAR:  'bg-[--muted] text-[--foreground]',
};

const statusStyles: Record<SemesterStatus, string> = {
  DRAFT:     'bg-[--muted] text-[--muted-foreground]',
  PUBLISHED: 'bg-[--muted] text-[--foreground]',
  CLOSED:    'bg-[--muted] text-[--muted-foreground]',
};

interface BadgeProps extends React.HTMLAttributes<HTMLSpanElement> {
  children: React.ReactNode;
}

export function Badge({ children, className, ...props }: BadgeProps) {
  return (
    <span className={cn(baseBadge, className)} {...props}>
      {children}
    </span>
  );
}

export function SessionBadge({ type }: { type: SessionType | null | undefined }) {
  if (!type) return <Badge className="bg-[--muted] text-[--muted-foreground]">—</Badge>;
  return <Badge className={sessionStyles[type]}>{type}</Badge>;
}

export function StatusBadge({ status }: { status: SemesterStatus }) {
  return <Badge className={statusStyles[status]}>{status}</Badge>;
}

export function YearBadge({ year }: { year: YearLevel }) {
  return <Badge className="bg-[--muted] text-[--foreground]">{year}</Badge>;
}

export function RoomTypeBadge({ type }: { type: string | null | undefined }) {
  const label = type?.replace('_', ' ') ?? '—';
  return <Badge className="bg-[--muted] text-[--muted-foreground]">{label}</Badge>;
}
