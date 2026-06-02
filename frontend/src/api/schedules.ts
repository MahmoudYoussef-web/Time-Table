import client from './client';
import { ScheduleGenerationJob, ConstraintViolation } from '../types';
import type { ScheduleSummary } from '../types';

export const getSchedules = () =>
  client.get<ScheduleSummary[]>('/schedules').then(r => r.data);

export const getSchedule = (id: number) =>
  client.get<import('../types').ScheduleDTO>(`/schedules/${id}`).then(r => r.data);

export const deleteSchedule = (id: number) =>
  client.delete(`/schedules/${id}`);

export const generateSchedule = (semesterId: number): Promise<string> =>
  client.post<string>(`/schedules/generate/${semesterId}`, null, {
    headers: { Accept: 'text/plain' }
  }).then(r => r.data);

export const validateSchedule = (id: number) =>
  client.patch(`/schedules/${id}/validate`);

export const lockSchedule = (id: number) =>
  client.patch(`/schedules/${id}/lock`);

export const getJobStatus = (jobId: string) =>
  client.get<ScheduleGenerationJob>(`/schedules/jobs/${jobId}`).then(r => r.data);

export const getConflicts = (scheduleId: number) =>
  client.get<ConstraintViolation[]>(`/schedules/${scheduleId}/conflicts`).then(r => r.data);

const triggerDownload = (blob: Blob, filename: string) => {
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  a.click();
  a.remove();
  window.URL.revokeObjectURL(url);
};

export const downloadPdf = async (
  scheduleId: number,
  theme: 'NAVY' | 'BLACK' = 'NAVY',
  year?: string
) => {
  const params = new URLSearchParams({ scheduleId: String(scheduleId), theme });
  if (year) params.set('year', year);
  const response = await client.get(`/export/pdf?${params}`, { responseType: 'blob' });
  const filename = `schedule-${scheduleId}${year ? '-' + year.toLowerCase() : ''}.pdf`;
  triggerDownload(response.data, filename);
};

export const downloadExcel = async (
  scheduleId: number,
  theme: 'NAVY' | 'BLACK' = 'NAVY'
) => {
  const params = new URLSearchParams({ scheduleId: String(scheduleId), theme });
  const response = await client.get(`/export/excel?${params}`, { responseType: 'blob' });
  const filename = `schedule-${scheduleId}.xlsx`;
  triggerDownload(response.data, filename);
};

export const downloadPng = async (
  scheduleId: number,
  theme: 'NAVY' | 'BLACK' = 'NAVY',
  year?: string
) => {
  const params = new URLSearchParams({ scheduleId: String(scheduleId), theme });
  if (year) params.set('year', year);
  const response = await client.get(`/export/png?${params}`, { responseType: 'blob' });
  const filename = `schedule-${scheduleId}${year ? '-' + year.toLowerCase() : ''}.png`;
  triggerDownload(response.data, filename);
};
