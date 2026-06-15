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

        /* ===== Users (Instructors) — CS ===== */

        User u_cs1 = findOrCreateUser("Prof. Dr. Ahmed Mahmoud", "ahmed.mahmoud@uni.edu", "123456", UserRole.INSTRUCTOR);
        User u_cs2 = findOrCreateUser("Prof. Dr. Sara Ali", "sara.ali@uni.edu", "123456", UserRole.INSTRUCTOR);
        User u_cs3 = findOrCreateUser("Dr. Khaled Nabil", "khaled.nabil@uni.edu", "123456", UserRole.INSTRUCTOR);
        User u_cs4 = findOrCreateUser("Dr. Mohamed Adel", "mohamed.adel@uni.edu", "123456", UserRole.INSTRUCTOR);
        User u_cs5 = findOrCreateUser("T.A. Omar Hassan", "omar.hassan@uni.edu", "123456", UserRole.INSTRUCTOR);
        User u_cs6 = findOrCreateUser("T.A. Lina Ahmed", "lina.ahmed@uni.edu", "123456", UserRole.INSTRUCTOR);

        /* ===== Users (Instructors) — IS ===== */

        User u_is1 = findOrCreateUser("Prof. Dr. Noha Khaled", "noha.khaled@uni.edu", "123456", UserRole.INSTRUCTOR);
        User u_is2 = findOrCreateUser("Dr. Karim Mostafa", "karim.mostafa@uni.edu", "123456", UserRole.INSTRUCTOR);
        User u_is3 = findOrCreateUser("Dr. Lamia Tarek", "lamia.tarek@uni.edu", "123456", UserRole.INSTRUCTOR);
        User u_is4 = findOrCreateUser("T.A. Mariam Ali", "mariam.ali@uni.edu", "123456", UserRole.INSTRUCTOR);
        User u_is5 = findOrCreateUser("T.A. Salma Ibrahim", "salma.ibrahim@uni.edu", "123456", UserRole.INSTRUCTOR);

        /* ===== Users (Instructors) — IT ===== */

        User u_it1 = findOrCreateUser("Prof. Dr. Hany Soliman", "hany.soliman@uni.edu", "123456", UserRole.INSTRUCTOR);
        User u_it2 = findOrCreateUser("Dr. Yara Gamal", "yara.gamal@uni.edu", "123456", UserRole.INSTRUCTOR);
        User u_it3 = findOrCreateUser("Dr. Mostafa Samir", "mostafa.samir@uni.edu", "123456", UserRole.INSTRUCTOR);
        User u_it4 = findOrCreateUser("T.A. Nour Mohamed", "nour.mohamed@uni.edu", "123456", UserRole.INSTRUCTOR);
        User u_it5 = findOrCreateUser("T.A. Youssef Tarek", "youssef.tarek@uni.edu", "123456", UserRole.INSTRUCTOR);

        /* ===== Users (Instructors) — AI ===== */

        User u_ai1 = findOrCreateUser("Prof. Dr. Youssef Ibrahim", "youssef.ibrahim@uni.edu", "123456", UserRole.INSTRUCTOR);
        User u_ai2 = findOrCreateUser("Dr. Heba Mostafa", "heba.mostafa@uni.edu", "123456", UserRole.INSTRUCTOR);
        User u_ai3 = findOrCreateUser("Dr. Amr Hassan", "amr.hassan@uni.edu", "123456", UserRole.INSTRUCTOR);
        User u_ai4 = findOrCreateUser("T.A. Mahmoud Samir", "mahmoud.samir@uni.edu", "123456", UserRole.INSTRUCTOR);
        User u_ai5 = findOrCreateUser("T.A. Menna Youssef", "menna.youssef@uni.edu", "123456", UserRole.INSTRUCTOR);

        /* ===== Instructors ===== */

        // CS
        Instructor i_cs1 = findOrCreateInstructor(u_cs1, cs, "Algorithms & Data Structures");
        Instructor i_cs2 = findOrCreateInstructor(u_cs2, cs, "Programming & Software Engineering");
        Instructor i_cs3 = findOrCreateInstructor(u_cs3, cs, "Operating Systems & Networks");
        Instructor i_cs4 = findOrCreateInstructor(u_cs4, cs, "Database & AI");
        Instructor i_cs5 = findOrCreateInstructor(u_cs5, cs, "Teaching Assistant");
        Instructor i_cs6 = findOrCreateInstructor(u_cs6, cs, "Teaching Assistant");
        // IS
        Instructor i_is1 = findOrCreateInstructor(u_is1, is, "Information Systems & Strategy");
        Instructor i_is2 = findOrCreateInstructor(u_is2, is, "Database & Business Analytics");
        Instructor i_is3 = findOrCreateInstructor(u_is3, is, "Enterprise Architecture");
        Instructor i_is4 = findOrCreateInstructor(u_is4, is, "Teaching Assistant");
        Instructor i_is5 = findOrCreateInstructor(u_is5, is, "Teaching Assistant");
        // IT
        Instructor i_it1 = findOrCreateInstructor(u_it1, it, "Networks & Security");
        Instructor i_it2 = findOrCreateInstructor(u_it2, it, "Web & Mobile Development");
        Instructor i_it3 = findOrCreateInstructor(u_it3, it, "Cloud Computing & DevOps");
        Instructor i_it4 = findOrCreateInstructor(u_it4, it, "Teaching Assistant");
        Instructor i_it5 = findOrCreateInstructor(u_it5, it, "Teaching Assistant");
        // AI
        Instructor i_ai1 = findOrCreateInstructor(u_ai1, ai, "Machine Learning & Deep Learning");
        Instructor i_ai2 = findOrCreateInstructor(u_ai2, ai, "NLP & Data Science");
        Instructor i_ai3 = findOrCreateInstructor(u_ai3, ai, "Computer Vision & Robotics");
        Instructor i_ai4 = findOrCreateInstructor(u_ai4, ai, "Teaching Assistant");
        Instructor i_ai5 = findOrCreateInstructor(u_ai5, ai, "Teaching Assistant");

        /* ===== Courses ===== */

        // CS — 8 courses (2 per year)
        Course cs101 = findOrCreateCourse("CS101", "Programming Fundamentals", 3, cs);
        Course cs102 = findOrCreateCourse("CS102", "Object-Oriented Programming", 3, cs);
        Course cs201 = findOrCreateCourse("CS201", "Data Structures & Algorithms", 3, cs);
        Course cs202 = findOrCreateCourse("CS202", "Database Systems", 3, cs);
        Course cs301 = findOrCreateCourse("CS301", "Operating Systems", 3, cs);
        Course cs302 = findOrCreateCourse("CS302", "Computer Networks", 3, cs);
        Course cs401 = findOrCreateCourse("CS401", "Software Engineering", 3, cs);
        Course cs402 = findOrCreateCourse("CS402", "Artificial Intelligence", 3, cs);

        // IS — 8 courses (2 per year)
        Course is101 = findOrCreateCourse("IS101", "Introduction to Information Systems", 3, is);
        Course is102 = findOrCreateCourse("IS102", "Programming for IS", 3, is);
        Course is201 = findOrCreateCourse("IS201", "Database Management Systems", 3, is);
        Course is202 = findOrCreateCourse("IS202", "Systems Analysis & Design", 3, is);
        Course is301 = findOrCreateCourse("IS301", "Enterprise Architecture", 3, is);
        Course is302 = findOrCreateCourse("IS302", "E-Business & E-Commerce", 3, is);
        Course is401 = findOrCreateCourse("IS401", "ERP Systems", 3, is);
        Course is402 = findOrCreateCourse("IS402", "Business Intelligence", 3, is);

        // IT — 8 courses (2 per year)
        Course it101 = findOrCreateCourse("IT101", "Introduction to Information Technology", 3, it);
        Course it102 = findOrCreateCourse("IT102", "Web Development Fundamentals", 3, it);
        Course it201 = findOrCreateCourse("IT201", "Computer Networks", 3, it);
        Course it202 = findOrCreateCourse("IT202", "Cybersecurity Fundamentals", 3, it);
        Course it301 = findOrCreateCourse("IT301", "Cloud Computing", 3, it);
        Course it302 = findOrCreateCourse("IT302", "Mobile Application Development", 3, it);
        Course it401 = findOrCreateCourse("IT401", "IT Project Management", 3, it);
        Course it402 = findOrCreateCourse("IT402", "Data Center Administration", 3, it);

        // AI — 8 courses (2 per year)
        Course ai101 = findOrCreateCourse("AI101", "Introduction to Artificial Intelligence", 3, ai);
        Course ai102 = findOrCreateCourse("AI102", "Programming for AI", 3, ai);
        Course ai201 = findOrCreateCourse("AI201", "Machine Learning Fundamentals", 3, ai);
        Course ai202 = findOrCreateCourse("AI202", "Data Structures for AI", 3, ai);
        Course ai301 = findOrCreateCourse("AI301", "Deep Learning", 3, ai);
        Course ai302 = findOrCreateCourse("AI302", "Natural Language Processing", 3, ai);
        Course ai401 = findOrCreateCourse("AI401", "Computer Vision", 3, ai);
        Course ai402 = findOrCreateCourse("AI402", "Robotics & Autonomous Systems", 3, ai);

        /* ===== Rooms ===== */

        Room r1 = findOrCreateRoom("Main Building", "A101", 200, RoomType.LECTURE_HALL);
        Room r2 = findOrCreateRoom("Main Building", "A102", 180, RoomType.LECTURE_HALL);
        Room r3 = findOrCreateRoom("Main Building", "B201", 150, RoomType.LECTURE_HALL);
        Room r4 = findOrCreateRoom("Main Building", "B202", 150, RoomType.LECTURE_HALL);
        Room r5 = findOrCreateRoom("Main Building", "C301", 120, RoomType.LECTURE_HALL);
        Room r6 = findOrCreateRoom("Main Building", "C302", 120, RoomType.LECTURE_HALL);
        Room r7 = findOrCreateRoom("Lab Center", "LAB1", 35, RoomType.LAB);
        Room r8 = findOrCreateRoom("Lab Center", "LAB2", 35, RoomType.LAB);
        Room r9 = findOrCreateRoom("Lab Center", "LAB3", 35, RoomType.LAB);
        Room r10 = findOrCreateRoom("Lab Center", "LAB4", 35, RoomType.LAB);
        Room r11 = findOrCreateRoom("Engineering Building", "SEM-A", 30, RoomType.SEMINAR_ROOM);
        Room r12 = findOrCreateRoom("Engineering Building", "SEM-B", 30, RoomType.SEMINAR_ROOM);

        /* ===== Time Slots ===== */

        LocalTime[][] times = {
                {LocalTime.of(8, 0), LocalTime.of(10, 0)},
                {LocalTime.of(10, 0), LocalTime.of(12, 0)},
                {LocalTime.of(13, 0), LocalTime.of(15, 0)},
                {LocalTime.of(15, 0), LocalTime.of(17, 0)},
        };

        TimeSlot[][] ts = new TimeSlot[6][4];
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
        Semester s4 = findOrCreateSemester("Spring 2026", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 6, 1), SemesterStatus.PUBLISHED);

        /* ===== Sections & Enrollments ===== */

        Semester[] semesters = {s1, s2, s3, s4};
        int studentCounter = 1;

        Course[][] coursesByDept = {
                {cs101, cs102, cs201, cs202, cs301, cs302, cs401, cs402},
                {is101, is102, is201, is202, is301, is302, is401, is402},
                {it101, it102, it201, it202, it301, it302, it401, it402},
                {ai101, ai102, ai201, ai202, ai301, ai302, ai401, ai402}
        };
        Instructor[][] instructorsByDept = {
                {i_cs1, i_cs2, i_cs3, i_cs4, i_cs5, i_cs6},
                {i_is1, i_is2, i_is3, i_is4, i_is5},
                {i_it1, i_it2, i_it3, i_it4, i_it5},
                {i_ai1, i_ai2, i_ai3, i_ai4, i_ai5}
        };
        Department[] depts = {cs, is, it, ai};
        YearLevel[] levels = {YearLevel.FIRST, YearLevel.FIRST, YearLevel.SECOND, YearLevel.SECOND,
                              YearLevel.THIRD, YearLevel.THIRD, YearLevel.FOURTH, YearLevel.FOURTH};

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
