import { useEffect, useState } from 'react';
import { Plus } from 'lucide-react';
import toast from 'react-hot-toast';
import { Course, CourseRequest } from '../types';
import { getCourses, createCourse, updateCourse, deleteCourse } from '../api/courses';
import * as departmentsApi from '../api/departments';
import { Table } from '../components/ui/Table';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import { CourseForm } from '../components/forms/CourseForm';

export function CoursesPage() {
  const [courses, setCourses] = useState<Course[]>([]);
  const [departments, setDepartments] = useState<{ id: number; name: string }[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingCourse, setEditingCourse] = useState<Course | null>(null);

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

  const handleDelete = async (c: Course) => {
    if (!window.confirm('Delete this course? This cannot be undone.')) return;
    try {
      await deleteCourse(c.id);
      toast.success('Course deleted');
      fetchData();
    } catch {
      toast.error('Failed to delete course');
    }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h1 className="headline-lg">Courses</h1>
        <Button onClick={openAdd}><Plus size={18} /> Add Course</Button>
      </div>
      <Table
        columns={[
          { key: 'code', header: 'Code' },
          { key: 'name', header: 'Name' },
          { key: 'creditHours', header: 'Credits' },
          { key: 'departmentName', header: 'Department' },
        ]}
        data={courses}
        onEdit={openEdit}
        onDelete={handleDelete}
        loading={loading}
      />
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
    </div>
  );
}
