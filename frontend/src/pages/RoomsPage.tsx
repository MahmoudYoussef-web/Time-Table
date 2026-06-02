import { useEffect, useState } from 'react';
import { Plus, Search, DoorOpen } from 'lucide-react';
import { toast } from 'sonner';
import { motion } from 'framer-motion';
import { Room, RoomType, RoomRequest } from '../types';
import { getRooms, createRoom, updateRoom, deleteRoom } from '../api/rooms';
import { Modal } from '../components/ui/Modal';
import { Button } from '../components/ui/Button';
import { RoomForm } from '../components/forms/RoomForm';
import { ConfirmModal } from '../components/ui/ConfirmModal';
import { EmptyState } from '../components/ui/EmptyState';
import { useTableFilter } from '../hooks/useTableFilter';
import { ROOM_TYPES } from '../lib/constants';

const typeLabels: Record<RoomType, string> = {
  LECTURE_HALL: 'Lecture Hall',
  LAB: 'Lab',
  SEMINAR_ROOM: 'Seminar',
};

function TypeBadge({ type }: { type: RoomType }) {
  return (
    <span className="inline-block bg-[--muted] px-2 py-0.5 rounded text-xs font-medium">
      {typeLabels[type]}
    </span>
  );
}

export function RoomsPage() {
  const [rooms, setRooms] = useState<Room[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingRoom, setEditingRoom] = useState<Room | null>(null);
  const [saving, setSaving] = useState(false);
  const [deleteTargetId, setDeleteTargetId] = useState<number | null>(null);
  const [deleting, setDeleting] = useState(false);

  const fetchRooms = async () => {
    try {
      const data = await getRooms();
      setRooms(data);
    } catch {
      toast.error('Failed to load rooms');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchRooms(); }, []);

  const openAdd = () => {
    setEditingRoom(null);
    setModalOpen(true);
  };

  const openEdit = (r: Room) => {
    setEditingRoom(r);
    setModalOpen(true);
  };

  const closeModal = () => {
    setModalOpen(false);
    setEditingRoom(null);
  };

  const handleSubmit = async (data: RoomRequest) => {
    setSaving(true);
    try {
      if (editingRoom) {
        await updateRoom(editingRoom.id, data);
        toast.success('Room updated');
      } else {
        await createRoom(data);
        toast.success('Room created');
      }
      closeModal();
      fetchRooms();
    } catch {
      toast.error('Failed to save room');
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deleteTargetId) return;
    setDeleting(true);
    try {
      await deleteRoom(deleteTargetId);
      toast.success('Room deleted');
      fetchRooms();
    } catch {
      toast.error('Failed to delete room');
    } finally {
      setDeleting(false);
      setDeleteTargetId(null);
    }
  };

  const { filtered, search, setSearch, filters, setFilters } = useTableFilter(
    rooms as unknown as Record<string, unknown>[],
    ['building', 'roomNumber'],
  );

  const displayed = filters.type ? filtered.filter(r => r.roomType === filters.type) : filtered;

  if (loading) {
    return (
      <div className="bg-[--card] border border-[--border] rounded-[--radius-md] overflow-hidden">
        <table className="w-full">
          <thead>
            <tr className="bg-[--muted] label-sm text-[--muted-foreground]">
              <th className="text-left py-3 px-4 font-medium">Building</th>
              <th className="text-left py-3 px-4 font-medium">Room Number</th>
              <th className="text-left py-3 px-4 font-medium">Capacity</th>
              <th className="text-left py-3 px-4 font-medium">Type</th>
              <th className="text-right py-3 px-4 font-medium">Actions</th>
            </tr>
          </thead>
          <tbody>
            {[1,2,3,4,5].map(i => (
              <tr key={i} className="animate-pulse">
                <td className="py-3 px-4"><div className="h-4 bg-[--muted] rounded w-24" /></td>
                <td className="py-3 px-4"><div className="h-4 bg-[--muted] rounded w-20" /></td>
                <td className="py-3 px-4"><div className="h-4 bg-[--muted] rounded w-16" /></td>
                <td className="py-3 px-4"><div className="h-4 bg-[--muted] rounded w-28" /></td>
                <td className="py-3 px-4"><div className="h-4 bg-[--muted] rounded w-12 ml-auto" /></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    );
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h1 className="display-md">Rooms <span className="text-sm text-[--text-secondary] font-normal">({rooms.length})</span></h1>
        <button onClick={openAdd} className="inline-flex items-center gap-1.5 bg-[--primary] text-[--primary-foreground] px-4 h-9 text-sm font-medium rounded-[--radius-sm] hover:opacity-90 transition-opacity">
          <Plus size={16} /> Add Room
        </button>
      </div>

      <div className="flex gap-2 mb-4">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-[--text-muted]" />
          <input
            placeholder="Search by building or room number..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            className="w-full pl-9 pr-3 py-2 text-sm border border-[--border] rounded-[--radius-sm] bg-[--surface] outline-none focus:border-[--primary]"
          />
        </div>
        <select
          value={filters.type ?? ''}
          onChange={e => setFilters(f => ({ ...f, type: e.target.value || undefined }))}
          className="text-sm border border-[--border] rounded-[--radius-sm] px-3 py-2 bg-[--surface] outline-none"
        >
          <option value="">All Types</option>
          {ROOM_TYPES.map(t => <option key={t} value={t}>{typeLabels[t as RoomType]}</option>)}
        </select>
      </div>

      {rooms.length === 0 ? (
        <EmptyState
          icon={<DoorOpen size={48} />}
          title="No rooms yet"
          description="Add your first room to get started"
          action={<Button onClick={openAdd}>Add Room</Button>}
        />
      ) : displayed.length === 0 ? (
        <EmptyState
          icon={<Search size={48} />}
          title="No results"
          description="Try a different search or filter"
        />
      ) : (
        <div className="bg-[--card] border border-[--border] rounded-[--radius-md] overflow-hidden">
          <table className="w-full">
            <thead>
              <tr className="bg-[--muted] label-sm text-[--muted-foreground]">
                <th className="text-left py-3 px-4 font-medium">Building</th>
                <th className="text-left py-3 px-4 font-medium">Room Number</th>
                <th className="text-left py-3 px-4 font-medium">Capacity</th>
                <th className="text-left py-3 px-4 font-medium">Type</th>
                <th className="text-right py-3 px-4 font-medium">Actions</th>
              </tr>
            </thead>
            <tbody>
              {displayed.map((r) => (
                <tr key={r.id} className="border-t border-[--border] body-md hover:bg-[--muted]/30 transition-colors">
                  <td className="py-3 px-4">{r.building}</td>
                  <td className="py-3 px-4">{r.roomNumber}</td>
                  <td className="py-3 px-4">{r.capacity}</td>
                  <td className="py-3 px-4"><TypeBadge type={r.roomType} /></td>
                  <td className="py-3 px-4 text-right">
                    <div className="inline-flex items-center gap-1">
                      <button onClick={() => openEdit(r)} className="p-1.5 rounded-[--radius-sm] hover:bg-[--muted] transition-colors" title="Edit">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17 3a2.85 2.85 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"/></svg>
                      </button>
                      <button onClick={() => setDeleteTargetId(r.id)} className="p-1.5 rounded-[--radius-sm] hover:bg-[--muted] transition-colors text-red-500" title="Delete">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M3 6h18"/><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/></svg>
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {modalOpen && (
        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="fixed inset-0 bg-black/20 z-50 flex items-center justify-center" onClick={closeModal}>
          <motion.div initial={{ scale: 0.95, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} className="bg-[--card] border border-[--border] rounded-[--radius-md] p-6 max-w-md w-full mx-4 shadow-lg" onClick={(e) => e.stopPropagation()}>
            <h2 className="display-md mb-4">{editingRoom ? 'Edit Room' : 'Add Room'}</h2>
            <RoomForm
              onSubmit={handleSubmit}
              defaultValues={editingRoom ? {
                building: editingRoom.building,
                roomNumber: editingRoom.roomNumber,
                capacity: editingRoom.capacity,
                roomType: editingRoom.roomType,
              } : undefined}
              loading={saving}
            />
            <div className="flex items-center justify-end gap-3 mt-4">
              <button onClick={closeModal} className="px-4 h-9 text-sm font-medium rounded-[--radius-sm] border border-[--border] hover:bg-[--muted] transition-colors">Cancel</button>
            </div>
          </motion.div>
        </motion.div>
      )}

      <ConfirmModal
        isOpen={deleteTargetId !== null}
        title="Delete Room"
        message="Are you sure? This action cannot be undone."
        onConfirm={handleDeleteConfirm}
        onCancel={() => setDeleteTargetId(null)}
        loading={deleting}
      />
    </div>
  );
}
