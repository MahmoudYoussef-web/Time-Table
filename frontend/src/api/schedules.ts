import client from './client';
import { ScheduleGenerationJob, ConstraintViolation } from '../types';

export const generateSchedule = (semesterId: number): Promise<string> =>
  client.post<string>(`/schedules/generate/${semesterId}`, null, {
    headers: { Accept: 'text/plain' }
  }).then(r => r.data);

export const getJobStatus = (jobId: string) =>
  client.get<ScheduleGenerationJob>(`/schedules/jobs/${jobId}`).then(r => r.data);

export const getConflicts = (scheduleId: number) =>
  client.get<ConstraintViolation[]>(`/schedules/${scheduleId}/conflicts`).then(r => r.data);

export const downloadPdf = async (scheduleId: number) => {
  const response = await client.get(`/schedules/${scheduleId}/pdf`, { responseType: 'blob' });
  const url  = window.URL.createObjectURL(new Blob([response.data]));
  const link = document.createElement('a');
  link.href  = url;
  link.setAttribute('download', `schedule-${scheduleId}.pdf`);
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
};

export const downloadExcel = async (scheduleId: number) => {
  const response = await client.get(`/schedules/${scheduleId}/excel`, { responseType: 'blob' });
  const url  = window.URL.createObjectURL(new Blob([response.data]));
  const link = document.createElement('a');
  link.href  = url;
  link.setAttribute('download', `schedule-${scheduleId}.xlsx`);
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
};
