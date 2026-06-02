import { useEffect, useState } from 'react';
import { Plus, Building2 } from 'lucide-react';
import { toast } from 'sonner';
import { Department } from '../types';
import * as departmentsApi from '../api/departments';
import { Table } from '../components/ui/Table';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import { ConfirmModal } from '../components/ui/ConfirmModal';
import { EmptyState } from '../components/ui/EmptyState';
import { DepartmentForm } from '../components/forms/DepartmentForm';

export function DepartmentsPage() {
  const [departments, setDepartments] = useState<Department[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Department | null>(null);
  const [loading, setLoading] = useState(true);
  const [deleteTargetId, setDeleteTargetId] = useState<number | null>(null);
  const [deleting, setDeleting] = useState(false);

  const fetchData = async () => {
    try {
      const d = await departmentsApi.getDepartments();
      setDepartments(d);
    } catch { toast.error('Failed to load departments'); }
    finally { setLoading(false); }
  };

  useEffect(() => { fetchData(); }, []);

  const openCreate = () => { setEditing(null); setModalOpen(true); };
  const openEdit = (d: Department) => { setEditing(d); setModalOpen(true); };
  const closeModal = () => { setModalOpen(false); setEditing(null); };

  const handleSubmit = async (data: Parameters<typeof departmentsApi.createDepartment>[0]) => {
    try {
      if (editing) {
        await departmentsApi.updateDepartment(editing.id, data);
        toast.success('Department updated');
      } else {
        await departmentsApi.createDepartment(data);
        toast.success('Department created');
      }
      closeModal();
      fetchData();
    } catch { toast.error('Failed to save department'); }
  };

  const handleDeleteConfirm = async () => {
    if (!deleteTargetId) return;
    setDeleting(true);
    try {
      await departmentsApi.deleteDepartment(deleteTargetId);
      toast.success('Department deleted');
      fetchData();
    } catch { toast.error('Failed to delete department'); }
    finally { setDeleting(false); setDeleteTargetId(null); }
  };

  if (loading) return <div className="h-32 bg-[--muted] rounded animate-pulse" />;

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h1 className="display-lg">Departments</h1>
        <Button onClick={openCreate}><Plus size={18} /> Add Department</Button>
      </div>
      {departments.length === 0 ? (
        <EmptyState
          icon={<Building2 size={48} />}
          title="No departments yet"
          description="Add your first department to get started"
          action={<Button onClick={openCreate}>Add Department</Button>}
        />
      ) : (
        <Table
          columns={[
            { key: 'id', header: 'ID' },
            { key: 'code', header: 'Code' },
            { key: 'name', header: 'Name' },
          ]}
          data={departments}
          onEdit={openEdit}
          onDelete={(d) => setDeleteTargetId(d.id)}
          loading={loading}
        />
      )}
      <Modal isOpen={modalOpen} onClose={closeModal} title={editing ? 'Edit Department' : 'Add Department'}>
        <DepartmentForm
          defaultValues={editing ? { code: editing.code, name: editing.name } : undefined}
          onSubmit={handleSubmit}
          onCancel={closeModal}
        />
      </Modal>
      <ConfirmModal
        isOpen={deleteTargetId !== null}
        title="Delete Department"
        message="Are you sure? This action cannot be undone."
        onConfirm={handleDeleteConfirm}
        onCancel={() => setDeleteTargetId(null)}
        loading={deleting}
      />
    </div>
  );
}
