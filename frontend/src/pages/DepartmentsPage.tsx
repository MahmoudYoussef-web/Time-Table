import { useEffect, useState } from 'react';
import { Plus } from 'lucide-react';
import toast from 'react-hot-toast';
import { Department } from '../types';
import * as departmentsApi from '../api/departments';
import { Table } from '../components/ui/Table';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import { DepartmentForm } from '../components/forms/DepartmentForm';

export function DepartmentsPage() {
  const [departments, setDepartments] = useState<Department[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Department | null>(null);
  const [loading, setLoading] = useState(true);

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

  const handleDelete = async (d: Department) => {
    if (!window.confirm('Are you sure you want to delete this department? This cannot be undone.')) return;
    try {
      await departmentsApi.deleteDepartment(d.id);
      toast.success('Department deleted');
      fetchData();
    } catch { toast.error('Failed to delete department'); }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h1 className="headline-lg">Departments</h1>
        <Button onClick={openCreate}><Plus size={18} /> Add Department</Button>
      </div>
      <Table
        columns={[
          { key: 'id', header: 'ID' },
          { key: 'code', header: 'Code' },
          { key: 'name', header: 'Name' },
        ]}
        data={departments}
        onEdit={openEdit}
        onDelete={handleDelete}
        loading={loading}
      />
      <Modal isOpen={modalOpen} onClose={closeModal} title={editing ? 'Edit Department' : 'Add Department'}>
        <DepartmentForm
          defaultValues={editing ? { code: editing.code, name: editing.name } : undefined}
          onSubmit={handleSubmit}
          onCancel={closeModal}
        />
      </Modal>
    </div>
  );
}
