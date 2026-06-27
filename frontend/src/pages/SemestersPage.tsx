import { useEffect, useState } from 'react';
import { Plus, CalendarDays } from 'lucide-react';
import { toast } from 'sonner';
import { Semester } from '../types';
import * as semestersApi from '../api/semesters';
import { Table } from '../components/ui/Table';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import { ConfirmModal } from '../components/ui/ConfirmModal';
import { EmptyState } from '../components/ui/EmptyState';
import { StatusBadge } from '../components/ui/Badge';
import { SemesterForm } from '../components/forms/SemesterForm';
import { formatDate } from '../lib/utils';

export function SemestersPage() {
  const [semesters, setSemesters] = useState<Semester[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Semester | null>(null);
  const [loading, setLoading] = useState(true);
  const [deleteTargetId, setDeleteTargetId] = useState<number | null>(null);
  const [deleting, setDeleting] = useState(false);

  const fetchData = async () => {
    try { setSemesters(await semestersApi.getSemesters()); }
    catch (err: any) {
      const msg = err?.response?.data?.message || err?.message || 'Failed to load semesters';
      console.error('Load semesters error:', err?.response?.data || err);
      toast.error(msg);
    }
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
    } catch (err: any) {
      const msg = err?.response?.data?.message || err?.message || 'Failed to save semester';
      console.error('Save semester error:', err?.response?.data || err);
      toast.error(msg);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deleteTargetId) return;
    setDeleting(true);
    try {
      await semestersApi.deleteSemester(deleteTargetId);
      toast.success('Semester deleted');
      fetchData();
    } catch (err: any) {
      const msg = err?.response?.data?.message || err?.message || 'Failed to delete semester';
      console.error('Delete semester error:', err?.response?.data || err);
      toast.error(msg);
    } finally {
      setDeleting(false);
      setDeleteTargetId(null);
    }
  };

  if (loading) return <div className="h-32 bg-[--muted] rounded animate-pulse" />;

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h1 className="display-lg">Semesters</h1>
        <Button onClick={openCreate}><Plus size={18} /> Add Semester</Button>
      </div>
      {semesters.length === 0 ? (
        <EmptyState
          icon={<CalendarDays size={48} />}
          title="No semesters yet"
          description="Add your first semester to get started"
          action={<Button onClick={openCreate}>Add Semester</Button>}
        />
      ) : (
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
          onDelete={(s) => setDeleteTargetId(s.id)}
          loading={loading}
        />
      )}
      <Modal isOpen={modalOpen} onClose={closeModal} title={editing ? 'Edit Semester' : 'Add Semester'}>
        <SemesterForm
          defaultValues={editing ? { name: editing.name, startDate: editing.startDate, endDate: editing.endDate, status: editing.status } : undefined}
          onSubmit={handleSubmit}
          onCancel={closeModal}
        />
      </Modal>

      <ConfirmModal
        isOpen={deleteTargetId !== null}
        title="Delete Semester"
        message="Are you sure? This action cannot be undone."
        onConfirm={handleDeleteConfirm}
        onCancel={() => setDeleteTargetId(null)}
        loading={deleting}
      />
    </div>
  );
}
