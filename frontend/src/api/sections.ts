import client from './client';
import { Section, SectionRequest } from '../types';

export const getSections    = ()                            => client.get<Section[]>('/sections').then(r => r.data);
export const getSection     = (id: number)                 => client.get<Section>(`/sections/${id}`).then(r => r.data);
export const createSection  = (data: SectionRequest)       => client.post<Section>('/sections', data).then(r => r.data);
export const updateSection  = (id: number, data: SectionRequest) => client.put<Section>(`/sections/${id}`, data).then(r => r.data);
export const deleteSection  = (id: number)                 => client.delete(`/sections/${id}`);
