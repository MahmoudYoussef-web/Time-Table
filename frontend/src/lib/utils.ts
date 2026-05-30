import dayjs from 'dayjs';
import duration from 'dayjs/plugin/duration';
import { DayOfWeek, SessionType } from '../types';

dayjs.extend(duration);

export function cn(...classes: (string | undefined | null | false)[]): string {
  return classes.filter(Boolean).join(' ');
}

export function formatDate(iso: string): string {
  return dayjs(iso).format('MMM D, YYYY');
}

export function formatTime(time: string): string {
  return dayjs(`2000-01-01T${time}`).format('h:mm A');
}

export function formatDuration(seconds: number): string {
  return dayjs.duration(seconds, 'seconds').format('mm:ss');
}

export const DAY_ORDER: DayOfWeek[] = ['SATURDAY', 'SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY'];

export function dayLabel(day: DayOfWeek): string {
  return day.slice(0, 3);
}

export function formatDay(day: string): string {
  return day.charAt(0) + day.slice(1).toLowerCase();
}

export const SESSION_COLORS: Record<SessionType, { bg: string; border: string }> = {
  LECTURE:  { bg: '#DBEAFE', border: '#93c5fd' },
  LAB:      { bg: '#DCFCE7', border: '#86efac' },
  SECTION:  { bg: '#DCFCE7', border: '#86efac' },
  TUTORIAL: { bg: '#FEF3C7', border: '#fcd34d' },
  SEMINAR:  { bg: '#F3E8FF', border: '#d8b4fe' },
};

export function parseJwt(token: string): Record<string, unknown> | null {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonStr = atob(base64);
    return JSON.parse(jsonStr);
  } catch {
    return null;
  }
}

export function sessionLabel(sessionType: SessionType): string {
  return sessionType.charAt(0) + sessionType.slice(1).toLowerCase();
}
