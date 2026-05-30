import client from './client';
import { Department, DepartmentRequest } from '../types';

export const getDepartments    = ()                              => client.get<Department[]>('/departments').then(r => r.data);
export const getDepartment     = (id: number)                   => client.get<Department>(`/departments/${id}`).then(r => r.data);
export const createDepartment  = (data: DepartmentRequest)      => client.post<Department>('/departments', data).then(r => r.data);
export const updateDepartment  = (id: number, data: DepartmentRequest) => client.put<Department>(`/departments/${id}`, data).then(r => r.data);
export const deleteDepartment  = (id: number)                   => client.delete(`/departments/${id}`);
