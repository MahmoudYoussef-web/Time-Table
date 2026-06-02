import axios from 'axios';
import { parseJwt } from '../lib/utils';

const rawUrl = import.meta.env.VITE_API_URL || '';
const baseURL = rawUrl
  ? rawUrl.replace(/\/?$/, '') + '/api'
  : '/api';

const client = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' },
});

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    const decoded = parseJwt(token);
    if (!decoded || decoded.exp * 1000 < Date.now()) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      localStorage.removeItem('role');
      window.location.href = '/auth';
      return Promise.reject(new Error('Token expired'));
    }
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

client.interceptors.response.use(
  (response) => {
    const ct = (response.headers?.['content-type'] || '') as string;
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
