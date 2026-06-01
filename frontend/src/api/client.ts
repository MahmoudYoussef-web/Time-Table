import axios from 'axios';
import { parseJwt } from '../lib/utils';

const client = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

function isTokenExpired(token: string): boolean {
  const payload = parseJwt(token);
  if (!payload || typeof payload.exp !== 'number') return true;
  return payload.exp * 1000 <= Date.now();
}

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    if (isTokenExpired(token)) {
      localStorage.removeItem('token');
      window.location.href = '/auth';
      return Promise.reject(new Error('Token expired'));
    }
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

client.interceptors.response.use(
  (response) => {
    const ct = response.headers?.['content-type'] || '';
    if (ct.includes('text/plain') && typeof response.data === 'string') {
      return response;
    }
    return response;
  },
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/auth';
    }
    return Promise.reject(err);
  }
);

export default client;
