import { useState, useEffect } from 'react';
import { toast } from 'sonner';
import { Modal } from '../ui/Modal';
import { Spinner } from '../ui/Spinner';
import { TimeSlot, Instructor } from '../../types';
import { getUnavailableSlots, addUnavailableSlot, removeUnavailableSlot } from '../../api/instructors';
import * as timeslotsApi from '../../api/timeslots';
import { formatDay } from '../../lib/utils';

const DAYS = ['SATURDAY', 'SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY'];

interface InstructorAvailabilityModalProps {
  instructor: Instructor;
  isOpen: boolean;
  onClose: () => void;
}

export function InstructorAvailabilityModal({ instructor, isOpen, onClose }: InstructorAvailabilityModalProps) {
  const [allSlots, setAllSlots] = useState<TimeSlot[]>([]);
  const [unavailableIds, setUnavailableIds] = useState<Set<number>>(new Set());
  const [loading, setLoading] = useState(true);
  const [toggling, setToggling] = useState<number | null>(null);

  useEffect(() => {
    if (!isOpen) return;
    setLoading(true);
    Promise.all([
      timeslotsApi.getTimeSlots(),
      getUnavailableSlots(instructor.id),
    ]).then(([slots, unavailable]) => {
      setAllSlots(slots);
      setUnavailableIds(new Set(unavailable.map(s => s.id)));
    }).catch((err: any) => {
      const msg = err?.response?.data?.message || err?.message || 'Failed to load availability data';
      console.error('Load availability error:', err?.response?.data || err);
      toast.error(msg);
    }).finally(() => setLoading(false));
  }, [isOpen, instructor.id]);

  const handleToggle = async (slotId: number) => {
    setToggling(slotId);
    try {
      if (unavailableIds.has(slotId)) {
        await removeUnavailableSlot(instructor.id, slotId);
        setUnavailableIds(prev => { const next = new Set(prev); next.delete(slotId); return next; });
        toast.success('Slot made available');
      } else {
        await addUnavailableSlot(instructor.id, slotId);
        setUnavailableIds(prev => new Set(prev).add(slotId));
        toast.success('Slot marked unavailable');
      }
    } catch (err: any) {
      const msg = err?.response?.data?.message || err?.message || 'Failed to update availability';
      console.error('Update availability error:', err?.response?.data || err);
      toast.error(msg);
    } finally {
      setToggling(null);
    }
  };

  const groupedByDay = DAYS.map(day => ({
    day,
    label: formatDay(day),
    slots: allSlots.filter(s => s.day === day).sort((a, b) => a.startTime.localeCompare(b.startTime)),
  }));

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={`Availability — ${instructor.name}`} size="lg">
      {loading ? (
        <div className="flex items-center justify-center py-12">
          <Spinner size="lg" />
        </div>
      ) : (
        <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
          {groupedByDay.map(group => (
            <div key={group.day}>
              <p className="label-sm font-semibold mb-2 text-[--muted-foreground]">{group.label}</p>
              <div className="space-y-1.5">
                {group.slots.map(slot => {
                  const isUnavailable = unavailableIds.has(slot.id);
                  const isToggling = toggling === slot.id;
                  return (
                    <button
                      key={slot.id}
                      onClick={() => handleToggle(slot.id)}
                      disabled={isToggling}
                      className={`w-full px-2 py-1.5 rounded text-xs font-medium transition-colors cursor-pointer flex items-center justify-between ${
                        isUnavailable
                          ? 'bg-red-100 text-red-700 border border-red-300'
                          : 'bg-green-100 text-green-700 border border-green-300'
                      }`}
                    >
                      <span>{slot.startTime} - {slot.endTime}</span>
                      {isToggling ? <Spinner size="sm" /> : <span>{isUnavailable ? '✕' : '✓'}</span>}
                    </button>
                  );
                })}
              </div>
            </div>
          ))}
        </div>
      )}
    </Modal>
  );
}
