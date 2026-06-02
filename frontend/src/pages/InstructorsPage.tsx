import { useEffect, useState } from 'react';
import { Plus, Search } from 'lucide-react';
import { toast } from 'sonner';
import { Instructor } from '../types';
import * as instructorsApi from '../api/instructors';
import * as departmentsApi from '../api/departments';
import { Table } from '../components/ui/Table';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import { ConfirmModal } from '../components/ui/ConfirmModal';
import { InstructorForm } from '../components/forms/InstructorForm';
import { useTableFilter } from '../hooks/useTableFilter';

export function InstructorsPage() {
  const [instructors, setInstructors] = useState<Instructor[]>([]);
  const [departments, setDepartments] = useState<{ id: number; name: string }[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Instructor | null>(null);
  const [loading, setLoading] = useState(true);
  const [deleteTargetId, setDeleteTargetId] = useState<number | null>(null);
  const [deleting, setDeleting] = useState(false);

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

  const handleDeleteConfirm = async () => {
    if (!deleteTargetId) return;
    setDeleting(true);
    try {
      await instructorsApi.deleteInstructor(deleteTargetId);
      toast.success('Instructor deleted');
      fetchData();
    } catch { toast.error('Failed to delete instructor'); }
    finally { setDeleting(false); setDeleteTargetId(null); }
  };

  const { filtered, search, setSearch, filters, setFilters } = useTableFilter(
    instructors,
    ['name', 'email', 'departmentName'],
  );

  if (loading) return <div className="h-32 bg-[--muted] rounded animate-pulse" />;

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h1 className="display-lg">Instructors <span className="text-sm text-[--text-secondary] font-normal">({instructors.length})</span></h1>
        <Button onClick={openCreate}><Plus size={18} /> Add Instructor</Button>
      </div>
      <div className="flex gap-2 mb-4">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-[--text-muted]" />
          <input
            placeholder="Search by name, email or department..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            className="w-full pl-9 pr-3 py-2 text-sm border border-[--border] rounded-[--radius-sm] bg-[--surface] outline-none focus:border-[--primary]"
          />
        </div>
        <select
          value={filters.departmentName ?? ''}
          onChange={e => setFilters(f => ({ ...f, departmentName: e.target.value || undefined }))}
          className="text-sm border border-[--border] rounded-[--radius-sm] px-3 py-2 bg-[--surface] outline-none"
        >
          <option value="">All Departments</option>
          {departments.map(d => <option key={d.id} value={d.name}>{d.name}</option>)}
        </select>
      </div>
      <Table
        columns={[
          { key: 'id', header: 'ID' },
          { key: 'name', header: 'Name' },
          { key: 'email', header: 'Email' },
          { key: 'departmentName', header: 'Department' },
        ]}
        data={filtered}
        onEdit={openEdit}
        onDelete={(i) => setDeleteTargetId(i.id)}
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
      <ConfirmModal
        isOpen={deleteTargetId !== null}
        title="Delete Instructor"
        message="Are you sure? This action cannot be undone."
        onConfirm={handleDeleteConfirm}
        onCancel={() => setDeleteTargetId(null)}
        loading={deleting}
      />
    </div>
  );
}
