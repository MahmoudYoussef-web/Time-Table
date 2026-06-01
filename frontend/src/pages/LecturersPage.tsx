import { useEffect, useState } from 'react';
import { Plus, Mail } from 'lucide-react';
import toast from 'react-hot-toast';
import { motion } from 'framer-motion';
import { Instructor } from '../types';
import * as instructorsApi from '../api/instructors';
import * as departmentsApi from '../api/departments';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import { InstructorForm } from '../components/forms/InstructorForm';

function AvatarFallback({ name }: { name: string }) {
  const initials = name
    .split(' ')
    .map((n) => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);
  return (
    <div className="w-10 h-10 rounded-full bg-[--muted] flex items-center justify-center text-sm font-medium shrink-0">
      {initials || '?'}
    </div>
  );
}

const container = { hidden: {}, show: { transition: { staggerChildren: 0.05 } } };
const item = { hidden: { opacity: 0, y: 12 }, show: { opacity: 1, y: 0 } };

export function LecturersPage() {
  const [lecturers, setLecturers] = useState<Instructor[]>([]);
  const [departments, setDepartments] = useState<{ id: number; name: string }[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Instructor | null>(null);

  const fetchData = async () => {
    try {
      const [l, d] = await Promise.all([instructorsApi.getInstructors(), departmentsApi.getDepartments()]);
      setLecturers(l);
      setDepartments(d.map(d => ({ id: d.id, name: d.name })));
    } catch {
      toast.error('Failed to load lecturers');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchData(); }, []);

  const openAdd = () => { setEditing(null); setModalOpen(true); };
  const openEdit = (l: Instructor) => { setEditing(l); setModalOpen(true); };
  const closeModal = () => { setModalOpen(false); setEditing(null); };

  const handleSubmit = async (data: Parameters<typeof instructorsApi.createInstructor>[0]) => {
    try {
      if (editing) {
        const { password, ...rest } = data;
        await instructorsApi.updateInstructor(editing.id, { ...rest, password: password || editing.email });
        toast.success('Lecturer updated');
      } else {
        await instructorsApi.createInstructor(data);
        toast.success('Lecturer created');
      }
      closeModal();
      fetchData();
    } catch {
      toast.error('Failed to save lecturer');
    }
  };

  const handleDelete = async (l: Instructor) => {
    if (!window.confirm('Delete this lecturer? This cannot be undone.')) return;
    try {
      await instructorsApi.deleteInstructor(l.id);
      toast.success('Lecturer deleted');
      fetchData();
    } catch {
      toast.error('Failed to delete lecturer');
    }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h1 className="headline-lg">Lecturers</h1>
        <Button onClick={openAdd}><Plus size={18} /> Add Lecturer</Button>
      </div>

      {loading ? (
        <div className="text-center py-12 text-sm text-[--muted-foreground]">Loading...</div>
      ) : lecturers.length === 0 ? (
        <div className="text-center py-12 text-sm text-[--muted-foreground]">No lecturers found</div>
      ) : (
        <motion.div variants={container} initial="hidden" animate="show" className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {lecturers.map((l) => (
            <motion.div key={l.id} variants={item} className="bg-[--card] border border-[--border] rounded-[--radius-md] p-5">
              <div className="flex items-start justify-between mb-3">
                <div className="flex items-center gap-3">
                  <AvatarFallback name={l.name} />
                  <div>
                    <p className="font-medium body-md">{l.name}</p>
                    <div className="flex items-center gap-1 text-[--muted-foreground] label-sm mt-0.5">
                      <Mail size={12} />
                      {l.email}
                    </div>
                  </div>
                </div>
                <div className="flex items-center gap-1">
                  <button onClick={() => openEdit(l)} className="p-1.5 rounded-[--radius-sm] hover:bg-[--muted] transition-colors" title="Edit">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17 3a2.85 2.85 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"/></svg>
                  </button>
                  <button onClick={() => handleDelete(l)} className="p-1.5 rounded-[--radius-sm] hover:bg-[--muted] transition-colors text-red-500" title="Delete">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M3 6h18"/><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/></svg>
                  </button>
                </div>
              </div>
              <p className="text-sm text-[--muted-foreground]">{l.departmentName}</p>
            </motion.div>
          ))}
        </motion.div>
      )}

      <Modal isOpen={modalOpen} onClose={closeModal} title={editing ? 'Edit Lecturer' : 'Add Lecturer'}>
        <InstructorForm
          defaultValues={editing ? {
            name: editing.name,
            email: editing.email,
            password: '',
            departmentId: departments.find(d => d.name === editing.departmentName)?.id || 0,
          } : undefined}
          onSubmit={handleSubmit}
          onCancel={closeModal}
          departments={departments}
        />
      </Modal>
    </div>
  );
}
