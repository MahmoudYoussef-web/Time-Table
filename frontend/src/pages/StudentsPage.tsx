import { useEffect, useState } from 'react';
import { Plus, GraduationCap, Search } from 'lucide-react';
import { toast } from 'sonner';
import { Student } from '../types';
import * as studentsApi from '../api/students';
import * as departmentsApi from '../api/departments';
import { Table } from '../components/ui/Table';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import { ConfirmModal } from '../components/ui/ConfirmModal';
import { EmptyState } from '../components/ui/EmptyState';
import { StudentForm } from '../components/forms/StudentForm';
import { useTableFilter } from '../hooks/useTableFilter';

export function StudentsPage() {
  const [students, setStudents] = useState<Student[]>([]);
  const [departments, setDepartments] = useState<{ id: number; name: string }[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Student | null>(null);
  const [deleteTargetId, setDeleteTargetId] = useState<number | null>(null);
  const [deleting, setDeleting] = useState(false);

  const fetchData = async () => {
    try {
      const [s, d] = await Promise.all([studentsApi.getStudents(), departmentsApi.getDepartments()]);
      setStudents(s);
      setDepartments(d.map(d => ({ id: d.id, name: d.name })));
    } catch {
      toast.error('Failed to load students');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchData(); }, []);

  const openCreate = () => { setEditing(null); setModalOpen(true); };
  const openEdit = (s: Student) => { setEditing(s); setModalOpen(true); };
  const closeModal = () => { setModalOpen(false); setEditing(null); };

  const handleSubmit = async (data: Parameters<typeof studentsApi.createStudent>[0]) => {
    try {
      if (editing) {
        await studentsApi.updateStudent(editing.id, data);
        toast.success('Student updated');
      } else {
        await studentsApi.createStudent(data);
        toast.success('Student created');
      }
      closeModal();
      fetchData();
    } catch {
      toast.error('Failed to save student');
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deleteTargetId) return;
    setDeleting(true);
    try {
      await studentsApi.deleteStudent(deleteTargetId);
      toast.success('Student deleted');
      fetchData();
    } catch {
      toast.error('Failed to delete student');
    } finally {
      setDeleting(false);
      setDeleteTargetId(null);
    }
  };

  const { filtered, search, setSearch, filters, setFilters } = useTableFilter(
    students as unknown as Record<string, unknown>[],
    ['fullName', 'email', 'departmentName'],
  );

  if (loading) return <div className="h-32 bg-[--muted] rounded animate-pulse" />;

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h1 className="display-lg">Students <span className="text-sm text-[--text-secondary] font-normal">({students.length})</span></h1>
        <Button onClick={openCreate}><Plus size={18} /> Add Student</Button>
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
        <select
          value={filters.academicYear ?? ''}
          onChange={e => setFilters(f => ({ ...f, academicYear: e.target.value || undefined }))}
          className="text-sm border border-[--border] rounded-[--radius-sm] px-3 py-2 bg-[--surface] outline-none"
        >
          <option value="">All Years</option>
          {[1,2,3,4,5].map(y => <option key={y} value={y}>Year {y}</option>)}
        </select>
      </div>
      {students.length === 0 ? (
        <EmptyState
          icon={<GraduationCap size={48} />}
          title="No students yet"
          description="Add your first student to get started"
          action={<Button onClick={openCreate}>Add Student</Button>}
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
            { key: 'fullName', header: 'Name' },
            { key: 'email', header: 'Email' },
            { key: 'academicYear', header: 'Academic Year' },
            { key: 'level', header: 'Level' },
            { key: 'departmentName', header: 'Department' },
          ]}
          data={filtered}
          onEdit={openEdit}
          onDelete={(s) => setDeleteTargetId(s.id)}
          loading={loading}
        />
      )}
      <Modal isOpen={modalOpen} onClose={closeModal} title={editing ? 'Edit Student' : 'Add Student'}>
        <StudentForm
          defaultValues={editing ? {
            userId: 0,
            academicYear: editing.academicYear,
            level: editing.level,
            departmentId: departments.find(d => d.name === editing.departmentName)?.id || 0,
          } : undefined}
          onSubmit={handleSubmit}
          onCancel={closeModal}
          departments={departments}
        />
      </Modal>
      <ConfirmModal
        isOpen={deleteTargetId !== null}
        title="Delete Student"
        message="Are you sure? This action cannot be undone."
        onConfirm={handleDeleteConfirm}
        onCancel={() => setDeleteTargetId(null)}
        loading={deleting}
      />
    </div>
  );
}
