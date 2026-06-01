import { useEffect, useState } from 'react';
import { Plus } from 'lucide-react';
import toast from 'react-hot-toast';
import { TimeSlot } from '../types';
import * as timeslotsApi from '../api/timeslots';
import { Table } from '../components/ui/Table';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import { TimeslotForm } from '../components/forms/TimeslotForm';
import { formatDay } from '../lib/utils';

export function TimeSlotsPage() {
  const [timeslots, setTimeslots] = useState<TimeSlot[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<TimeSlot | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchData = async () => {
    try { setTimeslots(await timeslotsApi.getTimeSlots()); }
    catch { toast.error('Failed to load time slots'); }
    finally { setLoading(false); }
  };

  useEffect(() => { fetchData(); }, []);

  const openCreate = () => { setEditing(null); setModalOpen(true); };
  const openEdit = (t: TimeSlot) => { setEditing(t); setModalOpen(true); };
  const closeModal = () => { setModalOpen(false); setEditing(null); };

  const handleSubmit = async (data: Parameters<typeof timeslotsApi.createTimeSlot>[0]) => {
    try {
      if (editing) {
        await timeslotsApi.updateTimeSlot(editing.id, data);
        toast.success('Time slot updated');
      } else {
        await timeslotsApi.createTimeSlot(data);
        toast.success('Time slot created');
      }
      closeModal();
      fetchData();
    } catch { toast.error('Failed to save time slot'); }
  };

  const handleDelete = async (t: TimeSlot) => {
    if (!window.confirm('Are you sure you want to delete this time slot? This cannot be undone.')) return;
    try {
      await timeslotsApi.deleteTimeSlot(t.id);
      toast.success('Time slot deleted');
      fetchData();
    } catch { toast.error('Failed to delete time slot'); }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h1 className="headline-lg">Time Slots</h1>
        <Button onClick={openCreate}><Plus size={18} /> Add Time Slot</Button>
      </div>
      <Table
        columns={[
          { key: 'id', header: 'ID' },
          { key: 'day', header: 'Day', render: (t: TimeSlot) => formatDay(t.day) },
          { key: 'startTime', header: 'Start Time' },
          { key: 'endTime', header: 'End Time' },
        ]}
        data={timeslots}
        onEdit={openEdit}
        onDelete={handleDelete}
        loading={loading}
      />
      <Modal isOpen={modalOpen} onClose={closeModal} title={editing ? 'Edit Time Slot' : 'Add Time Slot'}>
        <TimeslotForm
          defaultValues={editing ? { day: editing.day, startTime: editing.startTime, endTime: editing.endTime } : undefined}
          onSubmit={handleSubmit}
          onCancel={closeModal}
        />
      </Modal>
    </div>
  );
}
