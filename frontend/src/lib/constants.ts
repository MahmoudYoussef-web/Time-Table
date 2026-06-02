export const YEAR_LEVELS = ['FIRST', 'SECOND', 'THIRD', 'FOURTH'] as const;
export const SESSION_TYPES = ['LECTURE', 'LAB', 'SECTION', 'TUTORIAL', 'SEMINAR'] as const;
export const ROOM_TYPES = ['LECTURE_HALL', 'LAB', 'SEMINAR_ROOM', 'TUTORIAL_ROOM'] as const;
export const DAYS_OF_WEEK = ['SATURDAY', 'SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY'] as const;
export const SEMESTER_STATUSES = ['DRAFT', 'ACTIVE', 'PUBLISHED', 'CLOSED'] as const;
export const SCHEDULE_STATUSES = ['DRAFT', 'VALIDATED', 'LOCKED', 'PUBLISHED'] as const;
export const ENROLLMENT_STATUSES = ['ACTIVE', 'DROPPED', 'COMPLETED'] as const;

export const DEPT_COLORS: Record<string, string> = {
  'Computer Science': '#3B82F6',
  'Information Systems': '#10B981',
  'Information Technology': '#F59E0B',
  'Artificial Intelligence': '#8B5CF6',
  default: '#6B7280',
};

export function getDeptColor(deptName: string): string {
  return DEPT_COLORS[deptName] ?? DEPT_COLORS.default;
}
