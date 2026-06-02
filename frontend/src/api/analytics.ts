import client from './client';
import type { AnalyticsResponse } from '../types';

export const getAnalytics = (): Promise<AnalyticsResponse> =>
  client.get('/analytics/dashboard').then(r => r.data);
