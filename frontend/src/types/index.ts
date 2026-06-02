export interface LoginRequest    { email: string; password: string; }
export interface RegisterRequest { email: string; password: string; fullName: string; }
export interface AuthResponse {
  success: boolean;
  message: string;
  token: string;
  user: { id: number; email: string };
}

export type YearLevel      = 'FIRST' | 'SECOND' | 'THIRD' | 'FOURTH';
export type SessionType    = 'LECTURE' | 'LAB' | 'TUTORIAL' | 'SEMINAR' | 'SECTION';
export type SemesterStatus = 'DRAFT' | 'PUBLISHED' | 'CLOSED';
export type RoomType       = 'LECTURE_HALL' | 'LAB' | 'SEMINAR_ROOM';
export type JobStatus      = 'RUNNING' | 'COMPLETED' | 'FAILED';
export type DayOfWeek      = 'SATURDAY' | 'SUNDAY' | 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY';

export interface Department {
  id: number;
  code: string;
  name: string;
}

export interface Course {
  id: number;
  code: string;
  name: string;
  creditHours: number;
  departmentName: string;
}

export interface Room {
  id: number;
  building: string;
  roomNumber: string;
  capacity: number;
  roomType: RoomType;
}

export interface Instructor {
  id: number;
  name: string;
  email: string;
  departmentName: string;
}

export interface Semester {
  id: number;
  name: string;
  startDate: string;
  endDate: string;
  status: SemesterStatus;
}

export interface TimeSlot {
  id: number;
  day: DayOfWeek;
  startTime: string;
  endTime: string;
}

export interface Section {
  id: number;
  name: string;
  courseCode: string;
  courseName: string;
  instructorName: string;
  capacity: number;
  yearLevel: YearLevel;
  sessionType: SessionType;
}

export interface CourseRequest {
  code: string;
  name: string;
  creditHours: number;
  departmentId: number;
}

export interface RoomRequest {
  building: string;
  roomNumber: string;
  capacity: number;
  roomType: RoomType;
}

export interface InstructorRequest {
  name: string;
  email: string;
  password: string;
  departmentId: number;
}

export interface SectionRequest {
  name: string;
  courseId: number;
  instructorId: number;
  semesterId: number;
  capacity: number;
  yearLevel: YearLevel;
  sessionType: SessionType;
}

export interface SemesterRequest {
  name: string;
  startDate: string;
  endDate: string;
  status: SemesterStatus;
}

export interface TimeSlotRequest {
  day: DayOfWeek;
  startTime: string;
  endTime: string;
}

export interface DepartmentRequest {
  code: string;
  name: string;
}

export interface ScheduleEntryDTO {
  sectionId: number;
  sectionName: string;
  courseCode: string;
  courseName: string;
  instructorName: string;
  roomNumber: string;
  departmentName: string;
  dayOfWeek: DayOfWeek;
  startTime: string;
  endTime: string;
  yearLevel: YearLevel;
  sessionType: SessionType;
  hardViolations: number;
  softViolations: number;
}

export interface SlotDTO {
  startTime: string;
  endTime: string;
  entry: ScheduleEntryDTO | null;
}

export interface DayScheduleDTO {
  day: DayOfWeek;
  slots: SlotDTO[];
}

export interface WeeklyScheduleDTO {
  days: DayScheduleDTO[];
}

export interface ConstraintViolation {
  constraintName: string;
  sectionId: number;
  message: string;
}

export interface ScheduleGenerationJob {
  id: string;
  status: JobStatus;
  scheduleId: number;
}

export interface Student {
  id: number;
  fullName: string;
  email: string;
  academicYear: string;
  level: number;
  departmentName: string;
}

export interface StudentRequest {
  userId: number;
  academicYear: string;
  level: number;
  departmentId: number;
}

export interface Enrollment {
  id: number;
  studentName: string;
  studentEmail: string;
  sectionName: string;
  courseName: string;
  status: string;
  grade: string;
}

export interface EnrollmentRequest {
  studentId: number;
  sectionId: number;
  status: string;
}

export interface RoomUtilizationDTO {
  roomLabel: string;
  capacity: number;
  entriesCount: number;
  utilizationPercent: number;
}

export interface InstructorWorkloadDTO {
  instructorName: string;
  sectionCount: number;
  estimatedHours: number;
}

export interface AnalyticsResponse {
  totalSchedules: number;
  totalInstructors: number;
  totalRooms: number;
  totalCourses: number;
  averageFitnessScore: number;
  totalHardViolations: number;
  roomUtilization: RoomUtilizationDTO[];
  instructorWorkload: InstructorWorkloadDTO[];
}

export interface ScheduleSummary {
  id: number;
  semesterName: string;
  status: string;
  fitnessScore: number;
  hardViolations: number;
  softViolations: number;
  createdAt: string;
}

export interface ScheduleDTO {
  id: number;
  fitnessScore: number;
  hardViolations: number;
  softViolations: number;
  status: string;
  createdAt: string;
  entries: ScheduleEntryDTO[];
  unscheduledSectionIds: number[];
  semesterName: string;
  startDate: string;
  endDate: string;
}
