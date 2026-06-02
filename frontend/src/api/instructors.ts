import client from './client';
import { Instructor, InstructorRequest, TimeSlot } from '../types';

export const getInstructors    = ()                                => client.get<Instructor[]>('/instructors').then(r => r.data);
export const getInstructor     = (id: number)                     => client.get<Instructor>(`/instructors/${id}`).then(r => r.data);
export const createInstructor  = (data: InstructorRequest)        => client.post<Instructor>('/instructors', data).then(r => r.data);
export const updateInstructor  = (id: number, data: InstructorRequest) => client.put<Instructor>(`/instructors/${id}`, data).then(r => r.data);
export const deleteInstructor  = (id: number)                     => client.delete(`/instructors/${id}`);

export const getUnavailableSlots = (instructorId: number): Promise<TimeSlot[]> =>
  client.get(`/instructors/${instructorId}/unavailable-slots`).then(r => r.data);

export const addUnavailableSlot = (instructorId: number, slotId: number): Promise<void> =>
  client.post(`/instructors/${instructorId}/unavailable-slots/${slotId}`).then(r => r.data);

export const removeUnavailableSlot = (instructorId: number, slotId: number): Promise<void> =>
  client.delete(`/instructors/${instructorId}/unavailable-slots/${slotId}`).then(r => r.data);
