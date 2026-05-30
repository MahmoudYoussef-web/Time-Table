import client from './client';
import { Room, RoomRequest } from '../types';

export const getRooms    = ()                       => client.get<Room[]>('/rooms').then(r => r.data);
export const getRoom     = (id: number)            => client.get<Room>(`/rooms/${id}`).then(r => r.data);
export const createRoom  = (data: RoomRequest)     => client.post<Room>('/rooms', data).then(r => r.data);
export const updateRoom  = (id: number, data: RoomRequest) => client.put<Room>(`/rooms/${id}`, data).then(r => r.data);
export const deleteRoom  = (id: number)            => client.delete(`/rooms/${id}`);
