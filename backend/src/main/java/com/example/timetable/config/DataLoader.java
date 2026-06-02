package com.example.timetable.config;

import com.example.timetable.entity.*;
import com.example.timetable.entity.enums.*;
import com.example.timetable.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SemesterRepository semesterRepository;
    private final StudentRepository studentRepository;

    @Override
    public void run(String... args) {
        if (courseRepository.count() > 0 && sectionRepository.count() > 0) {
            return;
        }

        /* ===== Departments ===== */

        Department cs = findOrCreateDept("CS", "Computer Science");
        Department is = findOrCreateDept("IS", "Information Systems");
        Department it = findOrCreateDept("IT", "Information Technology");
        Department ai = findOrCreateDept("AI", "Artificial Intelligence");

        /* ===== Users (Instructors) ===== */

        User u1 = findOrCreateUser("Dr. Ahmed Hassan", "ahmed@uni.edu", "123456", UserRole.INSTRUCTOR);
        User u2 = findOrCreateUser("Dr. Sara Ali", "sara@uni.edu", "123456", UserRole.INSTRUCTOR);
        User u3 = findOrCreateUser("Dr. Mohamed Youssef", "mohamed@uni.edu", "123456", UserRole.INSTRUCTOR);
        User u4 = findOrCreateUser("Dr. Noha Khaled", "noha@uni.edu", "123456", UserRole.INSTRUCTOR);
        User u5 = findOrCreateUser("Dr. Karim Mostafa", "karim@uni.edu", "123456", UserRole.INSTRUCTOR);
        User u6 = findOrCreateUser("Dr. Lamia Tarek", "lamia@uni.edu", "123456", UserRole.INSTRUCTOR);
        User u7 = findOrCreateUser("Dr. Hany Soliman", "hany@uni.edu", "123456", UserRole.INSTRUCTOR);
        User u8 = findOrCreateUser("Dr. Yara Gamal", "yara@uni.edu", "123456", UserRole.INSTRUCTOR);
        User u9 = findOrCreateUser("Dr. Mostafa Adel", "mostafa@uni.edu", "123456", UserRole.INSTRUCTOR);

        /* ===== Instructors ===== */

        Instructor i1 = findOrCreateInstructor(u1, cs, "Algorithms");
        Instructor i2 = findOrCreateInstructor(u2, cs, "Programming");
        Instructor i3 = findOrCreateInstructor(u3, is, "Databases");
        Instructor i4 = findOrCreateInstructor(u4, is, "Systems Analysis");
        Instructor i5 = findOrCreateInstructor(u5, it, "Networks");
        Instructor i6 = findOrCreateInstructor(u6, it, "Web Development");
        Instructor i7 = findOrCreateInstructor(u7, ai, "Machine Learning");
        Instructor i8 = findOrCreateInstructor(u8, ai, "Data Science");
        Instructor i9 = findOrCreateInstructor(u9, cs, "Software Engineering");

        /* ===== Courses ===== */

        // CS
        Course cs101 = findOrCreateCourse("CS101", "Programming Fundamentals", 3, cs);
        Course cs102 = findOrCreateCourse("CS102", "Data Structures", 3, cs);
        Course cs201 = findOrCreateCourse("CS201", "Operating Systems", 3, cs);
        Course cs301 = findOrCreateCourse("CS301", "Software Engineering", 3, cs);

        // IS
        Course is101 = findOrCreateCourse("IS101", "Database Systems", 3, is);
        Course is102 = findOrCreateCourse("IS102", "Systems Analysis & Design", 3, is);
        Course is201 = findOrCreateCourse("IS201", "Enterprise Architecture", 3, is);

        // IT
        Course it101 = findOrCreateCourse("IT101", "Computer Networks", 3, it);
        Course it102 = findOrCreateCourse("IT102", "Web Development", 3, it);
        Course it201 = findOrCreateCourse("IT201", "Cybersecurity Fundamentals", 3, it);

        // AI
        Course ai101 = findOrCreateCourse("AI101", "Machine Learning", 3, ai);
        Course ai102 = findOrCreateCourse("AI102", "Deep Learning", 3, ai);
        Course ai201 = findOrCreateCourse("AI201", "Natural Language Processing", 3, ai);
        Course ai202 = findOrCreateCourse("AI202", "Computer Vision", 3, ai);

        /* ===== Rooms ===== */

        Room r1 = findOrCreateRoom("Main", "A101", 40, RoomType.LECTURE_HALL);
        Room r2 = findOrCreateRoom("Main", "A102", 35, RoomType.LECTURE_HALL);
        Room r3 = findOrCreateRoom("Main", "B201", 60, RoomType.LECTURE_HALL);
        Room r4 = findOrCreateRoom("Main", "B202", 50, RoomType.LECTURE_HALL);
        Room r5 = findOrCreateRoom("Science", "Lab1", 30, RoomType.LAB);
        Room r6 = findOrCreateRoom("Science", "Lab2", 30, RoomType.LAB);
        Room r7 = findOrCreateRoom("Science", "Lab3", 25, RoomType.LAB);
        Room r8 = findOrCreateRoom("Engineering", "SemA", 20, RoomType.SEMINAR_ROOM);
        Room r9 = findOrCreateRoom("Engineering", "SemB", 20, RoomType.SEMINAR_ROOM);

        /* ===== Time Slots ===== */

        LocalTime[][] times = {
                {LocalTime.of(8, 0), LocalTime.of(9, 30)},
                {LocalTime.of(9, 45), LocalTime.of(11, 15)},
                {LocalTime.of(11, 30), LocalTime.of(13, 0)},
                {LocalTime.of(13, 30), LocalTime.of(15, 0)},
                {LocalTime.of(15, 15), LocalTime.of(16, 45)},
        };

        TimeSlot[][] ts = new TimeSlot[6][5];
        DayOfWeek[] days = {DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY};
        for (int d = 0; d < days.length; d++) {
            for (int p = 0; p < times.length; p++) {
                ts[d][p] = findOrCreateTimeSlot(days[d], times[p][0], times[p][1]);
            }
        }

        /* ===== Semesters ===== */

        Semester s1 = findOrCreateSemester("Fall 2024", LocalDate.of(2024, 9, 1), LocalDate.of(2025, 1, 15), SemesterStatus.PUBLISHED);
        Semester s2 = findOrCreateSemester("Spring 2025", LocalDate.of(2025, 2, 1), LocalDate.of(2025, 6, 1), SemesterStatus.PUBLISHED);
        Semester s3 = findOrCreateSemester("Fall 2025", LocalDate.of(2025, 9, 1), LocalDate.of(2026, 1, 15), SemesterStatus.PUBLISHED);
        Semester s4 = findOrCreateSemester("Spring 2026", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 6, 1), SemesterStatus.DRAFT);

        /* ===== Sections & Enrollments ===== */

        // Helper: createSection + enrollStudents
        Semester[] semesters = {s1, s2, s3, s4};

        // Student counter for enrolling
        int studentCounter = 1;
        int[][] studentsPerSemester = {{4,4,3,4}, {4,3,4,4,3}, {4,4,4}, {4,4,4,4}};

        Course[][] coursesByDept = {
                {cs101, cs102, cs201, cs301},   // CS department
                {is101, is102, is201},           // IS department
                {it101, it102, it201},           // IT department
                {ai101, ai102, ai201, ai202}     // AI department
        };
        Instructor[][] instructorsByDept = {
                {i1, i2, i9},
                {i3, i4},
                {i5, i6},
                {i7, i8}
        };
        Department[] depts = {cs, is, it, ai};
        YearLevel[] levels = {YearLevel.FIRST, YearLevel.SECOND, YearLevel.THIRD, YearLevel.FOURTH};

        for (int semIdx = 0; semIdx < semesters.length; semIdx++) {
            Semester sem = semesters[semIdx];
            for (int deptIdx = 0; deptIdx < depts.length; deptIdx++) {
                for (int cIdx = 0; cIdx < coursesByDept[deptIdx].length; cIdx++) {
                    Course course = coursesByDept[deptIdx][cIdx];
                    Instructor instructor = instructorsByDept[deptIdx][cIdx % instructorsByDept[deptIdx].length];
                    YearLevel level = levels[cIdx];

                    // Create 2 sections per course per semester (A, B)
                    for (char secName = 'A'; secName <= 'B'; secName++) {
                        String name = String.valueOf(secName);
                        if (sectionRepository.findByNameAndCourseIdAndSemesterId(name, course.getId(), sem.getId()).isPresent()) {
                            continue;
                        }
                        Section section = new Section();
                        section.setName(name);
                        section.setCourse(course);
                        section.setInstructor(instructor);
                        section.setCapacity(25);
                        section.setSemester(sem);
                        section.setSessionType(SessionType.LECTURE);
                        section.setYearLevel(level);
                        section = sectionRepository.save(section);

                        // Enroll students
                        int numStudents = 15 + (secName - 'A') * 5;
                        for (int st = 0; st < numStudents; st++) {
                            String studentEmail = "student" + studentCounter + "@uni.edu";
                            Optional<User> existingUser = userRepository.findByEmail(studentEmail);
                            User studentUser;
                            if (existingUser.isEmpty()) {
                                studentUser = new User();
                                studentUser.setFullName("Student " + studentCounter);
                                studentUser.setEmail(studentEmail);
                                studentUser.setPassword(passwordEncoder.encode("123456"));
                                studentUser.setRole(UserRole.STUDENT);
                                studentUser.setEnabled(true);
                                studentUser = userRepository.save(studentUser);

                                Student student = new Student();
                                student.setUser(studentUser);
                                student.setAcademicYear(sem.getName().contains("2024") ? "2024/2025" : "2025/2026");
                                student.setLevel(level.ordinal() + 1);
                                student.setDepartment(depts[deptIdx]);
                                studentRepository.save(student);
                            }
                            studentCounter++;
                        }
                    }
                }
            }
        }

        System.out.println("=== Seed data loaded successfully ===");
    }

    private Department findOrCreateDept(String code, String name) {
        return departmentRepository.findByCode(code)
                .orElseGet(() -> {
                    Department d = new Department();
                    d.setCode(code);
                    d.setName(name);
                    return departmentRepository.save(d);
                });
    }

    private User findOrCreateUser(String fullName, String email, String password, UserRole role) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User u = new User();
                    u.setFullName(fullName);
                    u.setEmail(email);
                    u.setPassword(passwordEncoder.encode(password));
                    u.setRole(role);
                    u.setEnabled(true);
                    return userRepository.save(u);
                });
    }

    private Instructor findOrCreateInstructor(User user, Department dept, String specialization) {
        return instructorRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Instructor i = new Instructor();
                    i.setUser(user);
                    i.setDepartment(dept);
                    i.setSpecialization(specialization);
                    return instructorRepository.save(i);
                });
    }

    private Course findOrCreateCourse(String code, String name, int credits, Department dept) {
        return courseRepository.findByCode(code)
                .orElseGet(() -> {
                    Course c = new Course();
                    c.setCode(code);
                    c.setName(name);
                    c.setCreditHours(credits);
                    c.setDepartment(dept);
                    return courseRepository.save(c);
                });
    }

    private Room findOrCreateRoom(String building, String roomNumber, int capacity, RoomType type) {
        return roomRepository.findByBuildingAndRoomNumber(building, roomNumber)
                .orElseGet(() -> {
                    Room r = new Room();
                    r.setBuilding(building);
                    r.setRoomNumber(roomNumber);
                    r.setCapacity(capacity);
                    r.setRoomType(type);
                    return roomRepository.save(r);
                });
    }

    private TimeSlot findOrCreateTimeSlot(DayOfWeek day, LocalTime start, LocalTime end) {
        return timeSlotRepository.findByDayAndStartTimeAndEndTime(day, start, end)
                .orElseGet(() -> {
                    TimeSlot t = new TimeSlot();
                    t.setDay(day);
                    t.setStartTime(start);
                    t.setEndTime(end);
                    return timeSlotRepository.save(t);
                });
    }

    private Semester findOrCreateSemester(String name, LocalDate start, LocalDate end, SemesterStatus status) {
        return semesterRepository.findByName(name)
                .orElseGet(() -> {
                    Semester s = new Semester();
                    s.setName(name);
                    s.setStartDate(start);
                    s.setEndDate(end);
                    s.setStatus(status);
                    return semesterRepository.save(s);
                });
    }
}
