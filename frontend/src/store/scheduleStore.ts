import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface ScheduleState {
  lastScheduleId: number | null;
  setLastScheduleId: (id: number) => void;
  clearLastScheduleId: () => void;
}

export const useScheduleStore = create<ScheduleState>()(
  persist(
    (set) => ({
      lastScheduleId: null,
      setLastScheduleId: (id) => set({ lastScheduleId: id }),
      clearLastScheduleId: () => set({ lastScheduleId: null }),
    }),
    { name: 'schedule-store' }
  )
);
