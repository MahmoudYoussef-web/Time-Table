import { useEffect, useState } from 'react';
import { Plus } from 'lucide-react';
import toast from 'react-hot-toast';
import { Section } from '../types';
import * as sectionsApi from '../api/sections';
import * as coursesApi from '../api/courses';
import * as instructorsApi from '../api/instructors';
import * as semestersApi from '../api/semesters';
import { Table } from '../components/ui/Table';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import { YearBadge, SessionBadge } from '../components/ui/Badge';
import { SectionForm } from '../components/forms/SectionForm';

export function SectionsPage() {
  const [sections, setSections] = useState<Section[]>([]);
  const [courses, setCourses] = useState<{ id: number; code: string; name: string }[]>([]);
  const [instructors, setInstructors] = useState<{ id: number; name: string }[]>([]);
  const [semesters, setSemesters] = useState<{ id: number; name: string }[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Section | null>(null);
  const [loading, setLoading] = useState(true);

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

  const handleDelete = async (s: Section) => {
    if (!window.confirm('Are you sure you want to delete this section? This cannot be undone.')) return;
    try {
      await sectionsApi.deleteSection(s.id);
      toast.success('Section deleted');
      fetchData();
    } catch { toast.error('Failed to delete section'); }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h1 className="headline-lg">Sections</h1>
        <Button onClick={openCreate}><Plus size={18} /> Add Section</Button>
      </div>
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
        data={sections}
        onEdit={openEdit}
        onDelete={handleDelete}
        loading={loading}
      />
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
    </div>
  );
}
