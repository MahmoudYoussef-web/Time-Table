import { useAuthStore } from '../store/authStore';
import { login as loginApi } from '../api/auth';

export function useAuth() {
  const { token, user, isAuthenticated, login, logout, initialize } = useAuthStore();

  const loginUser = async (email: string, password: string) => {
    const response = await loginApi({ email, password });
    if (response.success) {
      login(response.token, response.user);
      return { success: true };
    }
    return { success: false, message: response.message };
  };

  return { token, user, isAuthenticated, loginUser, logout, initialize };
}
