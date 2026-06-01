import { useEffect, useState } from 'react';
import { Plus, Pencil, Trash2 } from 'lucide-react';
import toast from 'react-hot-toast';
import { motion } from 'framer-motion';
import { Room, RoomType, RoomRequest } from '../types';
import { getRooms, createRoom, updateRoom, deleteRoom } from '../api/rooms';

const typeLabels: Record<RoomType, string> = {
  LECTURE_HALL: 'Lecture Hall',
  LAB: 'Lab',
  SEMINAR_ROOM: 'Seminar',
};

const emptyForm: RoomRequest = { building: '', roomNumber: '', capacity: 30, roomType: 'LECTURE_HALL' };

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
  const [form, setForm] = useState<RoomRequest>(emptyForm);
  const [saving, setSaving] = useState(false);

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
    setForm(emptyForm);
    setModalOpen(true);
  };

  const openEdit = (r: Room) => {
    setEditingRoom(r);
    setForm({ building: r.building, roomNumber: r.roomNumber, capacity: r.capacity, roomType: r.roomType });
    setModalOpen(true);
  };

  const closeModal = () => {
    setModalOpen(false);
    setEditingRoom(null);
    setForm(emptyForm);
  };

  const handleSave = async () => {
    if (!form.building.trim() || !form.roomNumber.trim()) {
      toast.error('Building and room number are required');
      return;
    }
    setSaving(true);
    try {
      if (editingRoom) {
        await updateRoom(editingRoom.id, form);
        toast.success('Room updated');
      } else {
        await createRoom(form);
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

  const handleDelete = async (r: Room) => {
    try {
      await deleteRoom(r.id);
      toast.success('Room deleted');
      fetchRooms();
    } catch {
      toast.error('Failed to delete room');
    }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h1 className="headline-md">Rooms</h1>
        <button onClick={openAdd} className="inline-flex items-center gap-1.5 bg-[--primary] text-[--primary-foreground] px-4 h-9 text-sm font-medium rounded-[--radius-sm] hover:opacity-90 transition-opacity">
          <Plus size={16} /> Add Room
        </button>
      </div>

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
            {loading ? (
              <tr>
                <td colSpan={5} className="py-8 text-center text-sm text-[--muted-foreground]">Loading...</td>
              </tr>
            ) : rooms.length === 0 ? (
              <tr>
                <td colSpan={5} className="py-8 text-center text-sm text-[--muted-foreground]">No rooms found</td>
              </tr>
            ) : (
              rooms.map((r) => (
                <tr key={r.id} className="border-t border-[--border] body-md hover:bg-[--muted]/30 transition-colors">
                  <td className="py-3 px-4">{r.building}</td>
                  <td className="py-3 px-4">{r.roomNumber}</td>
                  <td className="py-3 px-4">{r.capacity}</td>
                  <td className="py-3 px-4"><TypeBadge type={r.roomType} /></td>
                  <td className="py-3 px-4 text-right">
                    <div className="inline-flex items-center gap-1">
                      <button onClick={() => openEdit(r)} className="p-1.5 rounded-[--radius-sm] hover:bg-[--muted] transition-colors" title="Edit">
                        <Pencil size={15} />
                      </button>
                      <button onClick={() => handleDelete(r)} className="p-1.5 rounded-[--radius-sm] hover:bg-[--muted] transition-colors text-red-500" title="Delete">
                        <Trash2 size={15} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {modalOpen && (
        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="fixed inset-0 bg-black/20 z-50 flex items-center justify-center" onClick={closeModal}>
          <motion.div initial={{ scale: 0.95, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} className="bg-[--card] border border-[--border] rounded-[--radius-md] p-6 max-w-md w-full mx-4 shadow-lg" onClick={(e) => e.stopPropagation()}>
            <h2 className="headline-md mb-4">{editingRoom ? 'Edit Room' : 'Add Room'}</h2>
            <div className="space-y-3">
              <div>
                <label className="label-sm text-[--muted-foreground] mb-1 block">Building</label>
                <input value={form.building} onChange={(e) => setForm({ ...form, building: e.target.value })} className="w-full h-10 px-3 border border-[--border] rounded-[--radius-sm] bg-[--background] body-md focus:outline-none focus:ring-1 focus:ring-[--primary]" placeholder="e.g. Science Hall" />
              </div>
              <div>
                <label className="label-sm text-[--muted-foreground] mb-1 block">Room Number</label>
                <input value={form.roomNumber} onChange={(e) => setForm({ ...form, roomNumber: e.target.value })} className="w-full h-10 px-3 border border-[--border] rounded-[--radius-sm] bg-[--background] body-md focus:outline-none focus:ring-1 focus:ring-[--primary]" placeholder="e.g. 201" />
              </div>
              <div>
                <label className="label-sm text-[--muted-foreground] mb-1 block">Capacity</label>
                <input type="number" min={1} max={999} value={form.capacity} onChange={(e) => setForm({ ...form, capacity: Number(e.target.value) })} className="w-full h-10 px-3 border border-[--border] rounded-[--radius-sm] bg-[--background] body-md focus:outline-none focus:ring-1 focus:ring-[--primary]" />
              </div>
              <div>
                <label className="label-sm text-[--muted-foreground] mb-1 block">Room Type</label>
                <select value={form.roomType} onChange={(e) => setForm({ ...form, roomType: e.target.value as RoomType })} className="w-full h-10 px-3 border border-[--border] rounded-[--radius-sm] bg-[--background] body-md focus:outline-none focus:ring-1 focus:ring-[--primary]">
                  <option value="LECTURE_HALL">Lecture Hall</option>
                  <option value="LAB">Lab</option>
                  <option value="SEMINAR_ROOM">Seminar</option>
                </select>
              </div>
            </div>
            <div className="flex items-center justify-end gap-3 mt-6">
              <button onClick={closeModal} className="px-4 h-9 text-sm font-medium rounded-[--radius-sm] border border-[--border] hover:bg-[--muted] transition-colors">Cancel</button>
              <button onClick={handleSave} disabled={saving} className="px-4 h-9 text-sm font-medium rounded-[--radius-sm] bg-[--primary] text-[--primary-foreground] hover:opacity-90 transition-opacity disabled:opacity-50">
                {saving ? 'Saving...' : 'Save'}
              </button>
            </div>
          </motion.div>
        </motion.div>
      )}
    </div>
  );
}
