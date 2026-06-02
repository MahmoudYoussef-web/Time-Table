interface EmptyStateProps {
  icon?: React.ReactNode;
  title: string;
  description?: string;
  action?: React.ReactNode;
}

export function EmptyState({ icon, title, description, action }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      {icon && <div className="mb-4 text-[--text-muted] opacity-40">{icon}</div>}
      <h3 className="text-lg font-semibold text-[--text-primary] mb-1">{title}</h3>
      {description && <p className="text-sm text-[--text-secondary] mb-4">{description}</p>}
      {action}
    </div>
  );
}
