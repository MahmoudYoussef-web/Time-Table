import { useEffect, useState } from 'react';
import { Plus, BookOpen, Search } from 'lucide-react';
import { toast } from 'sonner';
import { Course, CourseRequest } from '../types';
import { getCourses, createCourse, updateCourse, deleteCourse } from '../api/courses';
import * as departmentsApi from '../api/departments';
import { Table } from '../components/ui/Table';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import { ConfirmModal } from '../components/ui/ConfirmModal';
import { EmptyState } from '../components/ui/EmptyState';
import { CourseForm } from '../components/forms/CourseForm';
import { useTableFilter } from '../hooks/useTableFilter';

export function CoursesPage() {
  const [courses, setCourses] = useState<Course[]>([]);
  const [departments, setDepartments] = useState<{ id: number; name: string }[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingCourse, setEditingCourse] = useState<Course | null>(null);
  const [deleteTargetId, setDeleteTargetId] = useState<number | null>(null);
  const [deleting, setDeleting] = useState(false);

  const fetchData = async () => {
    try {
      const [c, d] = await Promise.all([getCourses(), departmentsApi.getDepartments()]);
      setCourses(c);
      setDepartments(d.map(d => ({ id: d.id, name: d.name })));
    } catch {
      toast.error('Failed to load courses');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchData(); }, []);

  const openAdd = () => { setEditingCourse(null); setModalOpen(true); };
  const openEdit = (c: Course) => { setEditingCourse(c); setModalOpen(true); };
  const closeModal = () => { setModalOpen(false); setEditingCourse(null); };

  const handleSubmit = async (data: CourseRequest) => {
    try {
      if (editingCourse) {
        await updateCourse(editingCourse.id, data);
        toast.success('Course updated');
      } else {
        await createCourse(data);
        toast.success('Course created');
      }
      closeModal();
      fetchData();
    } catch {
      toast.error('Failed to save course');
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deleteTargetId) return;
    setDeleting(true);
    try {
      await deleteCourse(deleteTargetId);
      toast.success('Course deleted');
      fetchData();
    } catch {
      toast.error('Failed to delete course');
    } finally {
      setDeleting(false);
      setDeleteTargetId(null);
    }
  };

  const { filtered, search, setSearch, filters, setFilters } = useTableFilter(
    courses,
    ['name', 'code'],
  );

  if (loading) return <div className="h-32 bg-[--muted] rounded animate-pulse" />;

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h1 className="display-lg">Courses <span className="text-sm text-[--text-secondary] font-normal">({courses.length})</span></h1>
        <Button onClick={openAdd}><Plus size={18} /> Add Course</Button>
      </div>
      <div className="flex gap-2 mb-4">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-[--text-muted]" />
          <input
            placeholder="Search by name or code..."
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
      {courses.length === 0 ? (
        <EmptyState
          icon={<BookOpen size={48} />}
          title="No courses yet"
          description="Add your first course to get started"
          action={<Button onClick={openAdd}>Add Course</Button>}
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
            { key: 'code', header: 'Code' },
            { key: 'name', header: 'Name' },
            { key: 'creditHours', header: 'Credits' },
            { key: 'departmentName', header: 'Department' },
          ]}
          data={filtered}
          onEdit={openEdit}
          onDelete={(c) => setDeleteTargetId(c.id)}
          loading={loading}
        />
      )}
      <Modal isOpen={modalOpen} onClose={closeModal} title={editingCourse ? 'Edit Course' : 'Add Course'}>
        <CourseForm
          defaultValues={editingCourse ? {
            code: editingCourse.code,
            name: editingCourse.name,
            creditHours: editingCourse.creditHours,
            departmentId: departments.find(d => d.name === editingCourse.departmentName)?.id || 0,
          } : undefined}
          onSubmit={handleSubmit}
          onCancel={closeModal}
          departments={departments}
        />
      </Modal>
      <ConfirmModal
        isOpen={deleteTargetId !== null}
        title="Delete Course"
        message="Are you sure? This action cannot be undone."
        onConfirm={handleDeleteConfirm}
        onCancel={() => setDeleteTargetId(null)}
        loading={deleting}
      />
    </div>
  );
}
