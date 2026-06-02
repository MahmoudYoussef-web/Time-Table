import { Navigate } from 'react-router-dom';
import { toast } from 'sonner';
import { useAuthStore } from '../store/authStore';

interface RoleGuardProps {
  allowedRoles: string[];
  children: React.ReactNode;
}

export function RoleGuard({ allowedRoles, children }: RoleGuardProps) {
  const role = useAuthStore(s => s.role);
  if (!role || !allowedRoles.includes(role)) {
    toast.error('Access denied');
    return <Navigate to="/dashboard" replace />;
  }
  return <>{children}</>;
}
