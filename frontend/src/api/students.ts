import client from './client';
import { Student, StudentRequest } from '../types';

export const getStudents    = ()                          => client.get<Student[]>('/students').then(r => r.data);
export const getStudent     = (id: number)                => client.get<Student>(`/students/${id}`).then(r => r.data);
export const createStudent  = (data: StudentRequest)      => client.post<Student>('/students', data).then(r => r.data);
export const updateStudent  = (id: number, data: StudentRequest) => client.put<Student>(`/students/${id}`, data).then(r => r.data);
export const deleteStudent  = (id: number)                => client.delete(`/students/${id}`);
