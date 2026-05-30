import { Search } from 'lucide-react';
import { useState } from 'react';

interface Student {
  id: string;
  name: string;
  department: string;
  semester: number;
  status: 'Active' | 'Inactive' | 'Graduated';
}

const mockStudents: Student[] = [
  { id: 'STU-2024-001', name: 'Emma Thompson', department: 'Computer Science', semester: 4, status: 'Active' },
  { id: 'STU-2024-002', name: 'James Rodriguez', department: 'Mathematics', semester: 2, status: 'Active' },
  { id: 'STU-2024-003', name: 'Sophia Chen', department: 'Physics', semester: 6, status: 'Active' },
  { id: 'STU-2024-004', name: 'Liam O\'Brien', department: 'Computer Science', semester: 4, status: 'Active' },
  { id: 'STU-2024-005', name: 'Olivia Patel', department: 'Chemistry', semester: 2, status: 'Active' },
  { id: 'STU-2024-006', name: 'Noah Kim', department: 'Biology', semester: 8, status: 'Graduated' },
  { id: 'STU-2024-007', name: 'Ava Martinez', department: 'Computer Science', semester: 6, status: 'Active' },
  { id: 'STU-2024-008', name: 'Ethan Johnson', department: 'Mathematics', semester: 4, status: 'Inactive' },
  { id: 'STU-2024-009', name: 'Isabella Lee', department: 'Physics', semester: 2, status: 'Active' },
  { id: 'STU-2024-010', name: 'Mason Brown', department: 'Chemistry', semester: 6, status: 'Active' },
];

const statusStyles: Record<string, string> = {
  Active: 'bg-[--muted] text-[--foreground]',
  Inactive: 'bg-[--muted] text-[--muted-foreground]',
  Graduated: 'bg-[--muted] text-[--foreground]',
};

export function StudentsPage() {
  const [search, setSearch] = useState('');

  const filtered = mockStudents.filter(
    (s) =>
      s.name.toLowerCase().includes(search.toLowerCase()) ||
      s.id.toLowerCase().includes(search.toLowerCase()) ||
      s.department.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h1 className="headline-md">Students</h1>
        <div className="relative">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-[--muted-foreground]" />
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search..."
            className="border border-[--border] rounded-[--radius-sm] bg-[--background] pl-9 pr-3 h-9 body-md focus:outline-none focus:ring-1 focus:ring-[--primary] w-60"
          />
        </div>
      </div>

      <div className="bg-[--card] border border-[--border] rounded-[--radius-md] overflow-hidden">
        <table className="w-full">
          <thead>
            <tr className="bg-[--muted] label-sm text-[--muted-foreground]">
              <th className="text-left py-3 px-4 font-medium">Student ID</th>
              <th className="text-left py-3 px-4 font-medium">Name</th>
              <th className="text-left py-3 px-4 font-medium">Department</th>
              <th className="text-left py-3 px-4 font-medium">Semester</th>
              <th className="text-left py-3 px-4 font-medium">Status</th>
            </tr>
          </thead>
          <tbody>
            {filtered.length === 0 ? (
              <tr>
                <td colSpan={5} className="py-8 text-center text-sm text-[--muted-foreground]">No students found</td>
              </tr>
            ) : (
              filtered.map((s) => (
                <tr key={s.id} className="border-t border-[--border] body-md hover:bg-[--muted]/30 transition-colors">
                  <td className="py-3 px-4 font-mono text-sm">{s.id}</td>
                  <td className="py-3 px-4">{s.name}</td>
                  <td className="py-3 px-4 text-[--muted-foreground]">{s.department}</td>
                  <td className="py-3 px-4">Semester {s.semester}</td>
                  <td className="py-3 px-4">
                    <span className={`inline-block px-2 py-0.5 rounded text-xs font-medium ${statusStyles[s.status]}`}>
                      {s.status}
                    </span>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
