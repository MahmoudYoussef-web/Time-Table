import client from './client';
import { WeeklyScheduleDTO } from '../types';

export const getWeeklySchedule = (id: number) =>
  client.get<WeeklyScheduleDTO>(`/weekly-schedules/${id}`).then(r => r.data);

export const getMySchedule = () =>
  client.get<WeeklyScheduleDTO>('/instructor/schedule/my').then(r => r.data);
