import { create } from 'zustand';

interface User { id: number; email: string; }

interface AuthState {
  token: string | null;
  user: User | null;
  role: 'ADMIN' | 'SCHEDULER' | 'INSTRUCTOR' | null;
  isAuthenticated: boolean;
  login: (token: string, user: User) => void;
  logout: () => void;
  initialize: () => void;
}

function decodeRole(token: string): 'ADMIN' | 'SCHEDULER' | 'INSTRUCTOR' | null {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.role || null;
  } catch {
    return null;
  }
}

export const useAuthStore = create<AuthState>((set) => ({
  token: null,
  user: null,
  role: null,
  isAuthenticated: false,

  login: (token, user) => {
    const role = decodeRole(token);
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(user));
    if (role) localStorage.setItem('role', role);
    set({ token, user, role, isAuthenticated: true });
  },

  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('role');
    set({ token: null, user: null, role: null, isAuthenticated: false });
    window.location.href = '/auth';
  },

  initialize: () => {
    const token = localStorage.getItem('token');
    const user  = localStorage.getItem('user');
    const role  = localStorage.getItem('role') as AuthState['role'];
    if (token && user) {
      set({ token, user: JSON.parse(user), role, isAuthenticated: true });
    }
  },
}));
