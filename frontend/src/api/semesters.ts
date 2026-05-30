import client from './client';
import { Semester, SemesterRequest } from '../types';

export const getSemesters    = ()                              => client.get<Semester[]>('/semesters').then(r => r.data);
export const getSemester     = (id: number)                   => client.get<Semester>(`/semesters/${id}`).then(r => r.data);
export const createSemester  = (data: SemesterRequest)        => client.post<Semester>('/semesters', data).then(r => r.data);
export const updateSemester  = (id: number, data: SemesterRequest) => client.put<Semester>(`/semesters/${id}`, data).then(r => r.data);
