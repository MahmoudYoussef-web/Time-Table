import { useEffect, useState } from 'react';
import { Plus } from 'lucide-react';
import toast from 'react-hot-toast';
import { Instructor } from '../types';
import * as instructorsApi from '../api/instructors';
import * as departmentsApi from '../api/departments';
import { Table } from '../components/ui/Table';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import { InstructorForm } from '../components/forms/InstructorForm';

export function InstructorsPage() {
  const [instructors, setInstructors] = useState<Instructor[]>([]);
  const [departments, setDepartments] = useState<{ id: number; name: string }[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Instructor | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchData = async () => {
    try {
      const [i, d] = await Promise.all([instructorsApi.getInstructors(), departmentsApi.getDepartments()]);
      setInstructors(i);
      setDepartments(d.map(d => ({ id: d.id, name: d.name })));
    } catch { toast.error('Failed to load instructors'); }
    finally { setLoading(false); }
  };

  useEffect(() => { fetchData(); }, []);

  const openCreate = () => { setEditing(null); setModalOpen(true); };
  const openEdit = (i: Instructor) => { setEditing(i); setModalOpen(true); };
  const closeModal = () => { setModalOpen(false); setEditing(null); };

  const handleSubmit = async (data: Parameters<typeof instructorsApi.createInstructor>[0]) => {
    try {
      if (editing) {
        await instructorsApi.updateInstructor(editing.id, data);
        toast.success('Instructor updated');
      } else {
        await instructorsApi.createInstructor(data);
        toast.success('Instructor created');
      }
      closeModal();
      fetchData();
    } catch { toast.error('Failed to save instructor'); }
  };

  const handleDelete = async (i: Instructor) => {
    if (!window.confirm('Are you sure you want to delete this instructor? This cannot be undone.')) return;
    try {
      await instructorsApi.deleteInstructor(i.id);
      toast.success('Instructor deleted');
      fetchData();
    } catch { toast.error('Failed to delete instructor'); }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h1 className="headline-lg">Instructors</h1>
        <Button onClick={openCreate}><Plus size={18} /> Add Instructor</Button>
      </div>
      <Table
        columns={[
          { key: 'id', header: 'ID' },
          { key: 'name', header: 'Name' },
          { key: 'email', header: 'Email' },
          { key: 'departmentName', header: 'Department' },
        ]}
        data={instructors}
        onEdit={openEdit}
        onDelete={handleDelete}
        loading={loading}
      />
      <Modal isOpen={modalOpen} onClose={closeModal} title={editing ? 'Edit Instructor' : 'Add Instructor'}>
        <InstructorForm
          defaultValues={editing ? { name: editing.name, email: editing.email, departmentId: departments.find(d => d.name === editing.departmentName)?.id || 0, password: '' } : undefined}
          onSubmit={handleSubmit}
          onCancel={closeModal}
          departments={departments}
        />
      </Modal>
    </div>
  );
}
