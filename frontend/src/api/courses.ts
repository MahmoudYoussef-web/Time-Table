import client from './client';
import { Course, CourseRequest } from '../types';

export const getCourses    = ()                           => client.get<Course[]>('/courses').then(r => r.data);
export const getCourse     = (id: number)                => client.get<Course>(`/courses/${id}`).then(r => r.data);
export const createCourse  = (data: CourseRequest)       => client.post<Course>('/courses', data).then(r => r.data);
export const updateCourse  = (id: number, data: CourseRequest) => client.put<Course>(`/courses/${id}`, data).then(r => r.data);
export const deleteCourse  = (id: number)                => client.delete(`/courses/${id}`);
