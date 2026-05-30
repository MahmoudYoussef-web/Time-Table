import client from './client';
import { TimeSlot, TimeSlotRequest } from '../types';

export const getTimeSlots    = ()                              => client.get<TimeSlot[]>('/timeslots').then(r => r.data);
export const getTimeSlot     = (id: number)                   => client.get<TimeSlot>(`/timeslots/${id}`).then(r => r.data);
export const createTimeSlot  = (data: TimeSlotRequest)        => client.post<TimeSlot>('/timeslots', data).then(r => r.data);
export const updateTimeSlot  = (id: number, data: TimeSlotRequest) => client.put<TimeSlot>(`/timeslots/${id}`, data).then(r => r.data);
export const deleteTimeSlot  = (id: number)                   => client.delete(`/timeslots/${id}`);
