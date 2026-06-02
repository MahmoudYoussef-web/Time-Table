import { useEffect, useState } from 'react';
import { Plus, Clock } from 'lucide-react';
import { toast } from 'sonner';
import { TimeSlot } from '../types';
import * as timeslotsApi from '../api/timeslots';
import { Table } from '../components/ui/Table';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import { ConfirmModal } from '../components/ui/ConfirmModal';
import { EmptyState } from '../components/ui/EmptyState';
import { TimeslotForm } from '../components/forms/TimeslotForm';
import { formatDay } from '../lib/utils';

export function TimeSlotsPage() {
  const [timeslots, setTimeslots] = useState<TimeSlot[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<TimeSlot | null>(null);
  const [loading, setLoading] = useState(true);
  const [deleteTargetId, setDeleteTargetId] = useState<number | null>(null);
  const [deleting, setDeleting] = useState(false);

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

  const handleDeleteConfirm = async () => {
    if (!deleteTargetId) return;
    setDeleting(true);
    try {
      await timeslotsApi.deleteTimeSlot(deleteTargetId);
      toast.success('Time slot deleted');
      fetchData();
    } catch { toast.error('Failed to delete time slot'); }
    finally { setDeleting(false); setDeleteTargetId(null); }
  };

  if (loading) return <div className="h-32 bg-[--muted] rounded animate-pulse" />;

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h1 className="display-lg">Time Slots</h1>
        <Button onClick={openCreate}><Plus size={18} /> Add Time Slot</Button>
      </div>
      {timeslots.length === 0 ? (
        <EmptyState
          icon={<Clock size={48} />}
          title="No time slots yet"
          description="Add your first time slot to get started"
          action={<Button onClick={openCreate}>Add Time Slot</Button>}
        />
      ) : (
        <Table
          columns={[
            { key: 'id', header: 'ID' },
            { key: 'day', header: 'Day', render: (t: TimeSlot) => formatDay(t.day) },
            { key: 'startTime', header: 'Start Time' },
            { key: 'endTime', header: 'End Time' },
          ]}
          data={timeslots}
          onEdit={openEdit}
          onDelete={(t) => setDeleteTargetId(t.id)}
          loading={loading}
        />
      )}
      <Modal isOpen={modalOpen} onClose={closeModal} title={editing ? 'Edit Time Slot' : 'Add Time Slot'}>
        <TimeslotForm
          defaultValues={editing ? { day: editing.day, startTime: editing.startTime, endTime: editing.endTime } : undefined}
          onSubmit={handleSubmit}
          onCancel={closeModal}
        />
      </Modal>
      <ConfirmModal
        isOpen={deleteTargetId !== null}
        title="Delete Time Slot"
        message="Are you sure? This action cannot be undone."
        onConfirm={handleDeleteConfirm}
        onCancel={() => setDeleteTargetId(null)}
        loading={deleting}
      />
    </div>
  );
}
