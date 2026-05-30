import { useEffect, useState } from 'react';
import { Plus } from 'lucide-react';
import toast from 'react-hot-toast';
import { Semester } from '../types';
import * as semestersApi from '../api/semesters';
import { Table } from '../components/ui/Table';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import { StatusBadge } from '../components/ui/Badge';
import { SemesterForm } from '../components/forms/SemesterForm';
import { formatDate } from '../lib/utils';

export function SemestersPage() {
  const [semesters, setSemesters] = useState<Semester[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Semester | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchData = async () => {
    try { setSemesters(await semestersApi.getSemesters()); }
    catch { toast.error('Failed to load semesters'); }
    finally { setLoading(false); }
  };

  useEffect(() => { fetchData(); }, []);

  const openCreate = () => { setEditing(null); setModalOpen(true); };
  const openEdit = (s: Semester) => { setEditing(s); setModalOpen(true); };
  const closeModal = () => { setModalOpen(false); setEditing(null); };

  const handleSubmit = async (data: Parameters<typeof semestersApi.createSemester>[0]) => {
    try {
      if (editing) {
        await semestersApi.updateSemester(editing.id, data);
        toast.success('Semester updated');
      } else {
        await semestersApi.createSemester(data);
        toast.success('Semester created');
      }
      closeModal();
      fetchData();
    } catch { toast.error('Failed to save semester'); }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h1 className="headline-lg">Semesters</h1>
        <Button onClick={openCreate}><Plus size={18} /> Add Semester</Button>
      </div>
      <Table
        columns={[
          { key: 'id', header: 'ID' },
          { key: 'name', header: 'Name' },
          { key: 'startDate', header: 'Start Date', render: (s: Semester) => formatDate(s.startDate) },
          { key: 'endDate', header: 'End Date', render: (s: Semester) => formatDate(s.endDate) },
          { key: 'status', header: 'Status', render: (s: Semester) => <StatusBadge status={s.status} /> },
        ]}
        data={semesters}
        onEdit={openEdit}
        loading={loading}
      />
      <Modal isOpen={modalOpen} onClose={closeModal} title={editing ? 'Edit Semester' : 'Add Semester'}>
        <SemesterForm
          defaultValues={editing ? { name: editing.name, startDate: editing.startDate, endDate: editing.endDate, status: editing.status } : undefined}
          onSubmit={handleSubmit}
          onCancel={closeModal}
        />
      </Modal>
    </div>
  );
}
