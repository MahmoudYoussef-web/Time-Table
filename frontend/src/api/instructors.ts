import client from './client';
import { Instructor, InstructorRequest } from '../types';

export const getInstructors    = ()                                => client.get<Instructor[]>('/instructors').then(r => r.data);
export const getInstructor     = (id: number)                     => client.get<Instructor>(`/instructors/${id}`).then(r => r.data);
export const createInstructor  = (data: InstructorRequest)        => client.post<Instructor>('/instructors', data).then(r => r.data);
export const updateInstructor  = (id: number, data: InstructorRequest) => client.put<Instructor>(`/instructors/${id}`, data).then(r => r.data);
export const deleteInstructor  = (id: number)                     => client.delete(`/instructors/${id}`);
