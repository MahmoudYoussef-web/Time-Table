import { AlertCircle } from 'lucide-react';
import { Button } from './Button';

interface ErrorStateProps {
  message?: string;
  onRetry?: () => void;
}

export function ErrorState({ message = 'Something went wrong', onRetry }: ErrorStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-16 gap-4">
      <AlertCircle className="h-12 w-12 text-[--destructive] opacity-70" />
      <p className="text-[--text-secondary] text-sm">{message}</p>
      {onRetry && (
        <Button variant="ghost" onClick={onRetry}>
          Try Again
        </Button>
      )}
    </div>
  );
}
