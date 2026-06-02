import { useEffect, useState } from 'react';
import { Plus, ClipboardList, Search } from 'lucide-react';
import { toast } from 'sonner';
import { Enrollment } from '../types';
import * as enrollmentsApi from '../api/enrollments';
import * as studentsApi from '../api/students';
import * as sectionsApi from '../api/sections';
import { Table } from '../components/ui/Table';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import { ConfirmModal } from '../components/ui/ConfirmModal';
import { EmptyState } from '../components/ui/EmptyState';
import { EnrollmentForm } from '../components/forms/EnrollmentForm';
import { useTableFilter } from '../hooks/useTableFilter';

export function EnrollmentsPage() {
  const [enrollments, setEnrollments] = useState<Enrollment[]>([]);
  const [students, setStudents] = useState<{ id: number; fullName: string }[]>([]);
  const [sections, setSections] = useState<{ id: number; name: string }[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Enrollment | null>(null);
  const [deleteTargetId, setDeleteTargetId] = useState<number | null>(null);
  const [deleting, setDeleting] = useState(false);

  const fetchData = async () => {
    try {
      const [e, st, se] = await Promise.all([
        enrollmentsApi.getEnrollments(),
        studentsApi.getStudents(),
        sectionsApi.getSections(),
      ]);
      setEnrollments(e);
      setStudents(st.map(s => ({ id: s.id, fullName: s.fullName })));
      setSections(se.map(s => ({ id: s.id, name: s.name })));
    } catch {
      toast.error('Failed to load enrollments');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchData(); }, []);

  const openCreate = () => { setEditing(null); setModalOpen(true); };
  const openEdit = (e: Enrollment) => { setEditing(e); setModalOpen(true); };
  const closeModal = () => { setModalOpen(false); setEditing(null); };

  const handleSubmit = async (data: Parameters<typeof enrollmentsApi.createEnrollment>[0]) => {
    try {
      if (editing) {
        await enrollmentsApi.updateEnrollment(editing.id, data);
        toast.success('Enrollment updated');
      } else {
        await enrollmentsApi.createEnrollment(data);
        toast.success('Enrollment created');
      }
      closeModal();
      fetchData();
    } catch {
      toast.error('Failed to save enrollment');
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deleteTargetId) return;
    setDeleting(true);
    try {
      await enrollmentsApi.deleteEnrollment(deleteTargetId);
      toast.success('Enrollment deleted');
      fetchData();
    } catch {
      toast.error('Failed to delete enrollment');
    } finally {
      setDeleting(false);
      setDeleteTargetId(null);
    }
  };

  const { filtered, search, setSearch, filters, setFilters } = useTableFilter(
    enrollments,
    ['studentName', 'sectionName', 'courseName', 'status'],
  );

  if (loading) return <div className="h-32 bg-[--muted] rounded animate-pulse" />;

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h1 className="display-lg">Enrollments <span className="text-sm text-[--text-secondary] font-normal">({enrollments.length})</span></h1>
        <Button onClick={openCreate}><Plus size={18} /> Add Enrollment</Button>
      </div>
      <div className="flex gap-2 mb-4">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-[--text-muted]" />
          <input
            placeholder="Search by student, section, course..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            className="w-full pl-9 pr-3 py-2 text-sm border border-[--border] rounded-[--radius-sm] bg-[--surface] outline-none focus:border-[--primary]"
          />
        </div>
        <select
          value={filters.status ?? ''}
          onChange={e => setFilters(f => ({ ...f, status: e.target.value || undefined }))}
          className="text-sm border border-[--border] rounded-[--radius-sm] px-3 py-2 bg-[--surface] outline-none"
        >
          <option value="">All Status</option>
          <option value="ACTIVE">Active</option>
          <option value="DROPPED">Dropped</option>
          <option value="COMPLETED">Completed</option>
        </select>
      </div>
      {enrollments.length === 0 ? (
        <EmptyState
          icon={<ClipboardList size={48} />}
          title="No enrollments yet"
          description="Add your first enrollment to get started"
          action={<Button onClick={openCreate}>Add Enrollment</Button>}
        />
      ) : filtered.length === 0 ? (
        <EmptyState
          icon={<Search size={48} />}
          title="No results"
          description="Try a different search or filter"
        />
      ) : (
        <Table
          columns={[
            { key: 'id', header: 'ID' },
            { key: 'studentName', header: 'Student' },
            { key: 'sectionName', header: 'Section' },
            { key: 'courseName', header: 'Course' },
            { key: 'status', header: 'Status' },
            { key: 'grade', header: 'Grade' },
          ]}
          data={filtered}
          onEdit={openEdit}
          onDelete={(e) => setDeleteTargetId(e.id)}
          loading={loading}
        />
      )}
      <Modal isOpen={modalOpen} onClose={closeModal} title={editing ? 'Edit Enrollment' : 'Add Enrollment'}>
        <EnrollmentForm
          defaultValues={editing ? {
            studentId: students.find(s => s.fullName === editing.studentName)?.id || 0,
            sectionId: sections.find(s => s.name === editing.sectionName)?.id || 0,
            status: editing.status,
          } : undefined}
          onSubmit={handleSubmit}
          onCancel={closeModal}
          students={students}
          sections={sections}
        />
      </Modal>
      <ConfirmModal
        isOpen={deleteTargetId !== null}
        title="Delete Enrollment"
        message="Are you sure? This action cannot be undone."
        onConfirm={handleDeleteConfirm}
        onCancel={() => setDeleteTargetId(null)}
        loading={deleting}
      />
    </div>
  );
}
