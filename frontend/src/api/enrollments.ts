import client from './client';
import { Enrollment, EnrollmentRequest } from '../types';

export const getEnrollments    = ()                             => client.get<Enrollment[]>('/enrollments').then(r => r.data);
export const getEnrollment     = (id: number)                   => client.get<Enrollment>(`/enrollments/${id}`).then(r => r.data);
export const createEnrollment  = (data: EnrollmentRequest)      => client.post<Enrollment>('/enrollments', data).then(r => r.data);
export const updateEnrollment  = (id: number, data: EnrollmentRequest) => client.put<Enrollment>(`/enrollments/${id}`, data).then(r => r.data);
export const deleteEnrollment  = (id: number)                   => client.delete(`/enrollments/${id}`);
