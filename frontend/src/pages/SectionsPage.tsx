import { useEffect, useState } from 'react';
import { Plus, LayoutList, Search } from 'lucide-react';
import { toast } from 'sonner';
import { Section } from '../types';
import * as sectionsApi from '../api/sections';
import * as coursesApi from '../api/courses';
import * as instructorsApi from '../api/instructors';
import * as semestersApi from '../api/semesters';
import { Table } from '../components/ui/Table';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import { ConfirmModal } from '../components/ui/ConfirmModal';
import { EmptyState } from '../components/ui/EmptyState';
import { YearBadge, SessionBadge } from '../components/ui/Badge';
import { SectionForm } from '../components/forms/SectionForm';
import { useTableFilter } from '../hooks/useTableFilter';
import { YEAR_LEVELS, SESSION_TYPES } from '../lib/constants';

export function SectionsPage() {
  const [sections, setSections] = useState<Section[]>([]);
  const [courses, setCourses] = useState<{ id: number; code: string; name: string }[]>([]);
  const [instructors, setInstructors] = useState<{ id: number; name: string }[]>([]);
  const [semesters, setSemesters] = useState<{ id: number; name: string }[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Section | null>(null);
  const [loading, setLoading] = useState(true);
  const [deleteTargetId, setDeleteTargetId] = useState<number | null>(null);
  const [deleting, setDeleting] = useState(false);

  const fetchData = async () => {
    try {
      const [s, c, i, sem] = await Promise.all([
        sectionsApi.getSections(),
        coursesApi.getCourses(),
        instructorsApi.getInstructors(),
        semestersApi.getSemesters(),
      ]);
      setSections(s);
      setCourses(c.map(c => ({ id: c.id, code: c.code, name: c.name })));
      setInstructors(i.map(i => ({ id: i.id, name: i.name })));
      setSemesters(sem.map(s => ({ id: s.id, name: s.name })));
    } catch { toast.error('Failed to load sections'); }
    finally { setLoading(false); }
  };

  useEffect(() => { fetchData(); }, []);

  const openCreate = () => { setEditing(null); setModalOpen(true); };
  const openEdit = (s: Section) => { setEditing(s); setModalOpen(true); };
  const closeModal = () => { setModalOpen(false); setEditing(null); };

  const handleSubmit = async (data: Parameters<typeof sectionsApi.createSection>[0]) => {
    try {
      if (editing) {
        await sectionsApi.updateSection(editing.id, data);
        toast.success('Section updated');
      } else {
        await sectionsApi.createSection(data);
        toast.success('Section created');
      }
      closeModal();
      fetchData();
    } catch { toast.error('Failed to save section'); }
  };

  const handleDeleteConfirm = async () => {
    if (!deleteTargetId) return;
    setDeleting(true);
    try {
      await sectionsApi.deleteSection(deleteTargetId);
      toast.success('Section deleted');
      fetchData();
    } catch { toast.error('Failed to delete section'); }
    finally { setDeleting(false); setDeleteTargetId(null); }
  };

  const { filtered, search, setSearch, filters, setFilters } = useTableFilter(
    sections,
    ['name', 'courseCode', 'courseName', 'instructorName'],
  );

  if (loading) return <div className="h-32 bg-[--muted] rounded animate-pulse" />;

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h1 className="display-lg">Sections <span className="text-sm text-[--text-secondary] font-normal">({sections.length})</span></h1>
        <Button onClick={openCreate}><Plus size={18} /> Add Section</Button>
      </div>
      <div className="flex gap-2 mb-4">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-[--text-muted]" />
          <input
            placeholder="Search by name, course or instructor..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            className="w-full pl-9 pr-3 py-2 text-sm border border-[--border] rounded-[--radius-sm] bg-[--surface] outline-none focus:border-[--primary]"
          />
        </div>
        <select
          value={filters.yearLevel ?? ''}
          onChange={e => setFilters(f => ({ ...f, yearLevel: e.target.value || undefined }))}
          className="text-sm border border-[--border] rounded-[--radius-sm] px-3 py-2 bg-[--surface] outline-none"
        >
          <option value="">All Years</option>
          {YEAR_LEVELS.map(y => <option key={y} value={y}>{y}</option>)}
        </select>
        <select
          value={filters.sessionType ?? ''}
          onChange={e => setFilters(f => ({ ...f, sessionType: e.target.value || undefined }))}
          className="text-sm border border-[--border] rounded-[--radius-sm] px-3 py-2 bg-[--surface] outline-none"
        >
          <option value="">All Types</option>
          {SESSION_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
        </select>
      </div>
      {sections.length === 0 ? (
        <EmptyState
          icon={<LayoutList size={48} />}
          title="No sections yet"
          description="Add your first section to get started"
          action={<Button onClick={openCreate}>Add Section</Button>}
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
            { key: 'name', header: 'Name' },
            { key: 'courseCode', header: 'Course Code' },
            { key: 'courseName', header: 'Course Name' },
            { key: 'instructorName', header: 'Instructor' },
            { key: 'yearLevel', header: 'Year', render: (s: Section) => <YearBadge year={s.yearLevel} /> },
            { key: 'sessionType', header: 'Type', render: (s: Section) => <SessionBadge type={s.sessionType} /> },
          ]}
          data={filtered}
          onEdit={openEdit}
          onDelete={(s) => setDeleteTargetId(s.id)}
          loading={loading}
        />
      )}
      <Modal isOpen={modalOpen} onClose={closeModal} title={editing ? 'Edit Section' : 'Add Section'}>
        <SectionForm
          defaultValues={editing ? {
            name: editing.name,
            capacity: editing.capacity,
            yearLevel: editing.yearLevel,
            sessionType: editing.sessionType,
            courseId: courses.find(c => c.code === editing.courseCode)?.id || 0,
            instructorId: instructors.find(i => i.name === editing.instructorName)?.id || 0,
            semesterId: 0,
          } : undefined}
          onSubmit={handleSubmit}
          onCancel={closeModal}
          courses={courses}
          instructors={instructors}
          semesters={semesters}
        />
      </Modal>
      <ConfirmModal
        isOpen={deleteTargetId !== null}
        title="Delete Section"
        message="Are you sure? This action cannot be undone."
        onConfirm={handleDeleteConfirm}
        onCancel={() => setDeleteTargetId(null)}
        loading={deleting}
      />
    </div>
  );
}
