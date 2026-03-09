package com.example.timetable.config;

import com.example.timetable.entity.*;
import com.example.timetable.entity.enums.UserRole;
import com.example.timetable.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

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

    @Override
    public void run(String... args) {

        if (courseRepository.count() > 0) {
            return;
        }

        /* ===== Department ===== */

        Department cs = new Department();
        cs.setCode("CS");
        cs.setName("Computer Science");
        departmentRepository.save(cs);

        /* ===== Users (Instructors) ===== */

        User u1 = new User();
        u1.setFullName("Dr. Ahmed");
        u1.setEmail("ahmed@uni.edu");
        u1.setPassword(passwordEncoder.encode("123456"));
        u1.setRole(UserRole.INSTRUCTOR);
        u1.setEnabled(true);
        userRepository.save(u1);

        User u2 = new User();
        u2.setFullName("Dr. Sara");
        u2.setEmail("sara@uni.edu");
        u2.setPassword(passwordEncoder.encode("123456"));
        u2.setRole(UserRole.INSTRUCTOR);
        u2.setEnabled(true);
        userRepository.save(u2);

        /* ===== Instructors ===== */

        Instructor i1 = new Instructor();
        i1.setUser(u1);
        i1.setDepartment(cs);
        instructorRepository.save(i1);

        Instructor i2 = new Instructor();
        i2.setUser(u2);
        i2.setDepartment(cs);
        instructorRepository.save(i2);

        /* ===== Courses ===== */

        Course c1 = new Course();
        c1.setCode("CS101");
        c1.setName("Programming");
        c1.setCreditHours(3);
        c1.setDepartment(cs);
        courseRepository.save(c1);

        Course c2 = new Course();
        c2.setCode("CS102");
        c2.setName("Data Structures");
        c2.setCreditHours(3);
        c2.setDepartment(cs);
        courseRepository.save(c2);

        /* ===== Rooms ===== */

        Room r1 = new Room();
        r1.setBuilding("Main");
        r1.setRoomNumber("A101");
        r1.setCapacity(40);
        roomRepository.save(r1);

        Room r2 = new Room();
        r2.setBuilding("Main");
        r2.setRoomNumber("B202");
        r2.setCapacity(60);
        roomRepository.save(r2);

        /* ===== Time Slots ===== */

        TimeSlot t1 = new TimeSlot();
        t1.setDay(DayOfWeek.MONDAY);
        t1.setStartTime(LocalTime.of(9, 0));
        t1.setEndTime(LocalTime.of(11, 0));
        timeSlotRepository.save(t1);

        TimeSlot t2 = new TimeSlot();
        t2.setDay(DayOfWeek.MONDAY);
        t2.setStartTime(LocalTime.of(11, 0));
        t2.setEndTime(LocalTime.of(13, 0));
        timeSlotRepository.save(t2);
        /* ===== Semester ===== */

        Semester semester = new Semester();
        semester.setName("Spring 2026");
        semester.setStartDate(LocalDate.of(2026, 2, 1));
        semester.setEndDate(LocalDate.of(2026, 6, 1));
        semesterRepository.save(semester);

        /* ===== Sections ===== */

        Section s1 = new Section();
        s1.setName("A");
        s1.setCourse(c1);
        s1.setInstructor(i1);
        s1.setCapacity(35);
        s1.setSemester(semester);
        sectionRepository.save(s1);

        Section s2 = new Section();
        s2.setName("B");
        s2.setCourse(c2);
        s2.setInstructor(i2);
        s2.setCapacity(40);
        s2.setSemester(semester);
        sectionRepository.save(s2);

        System.out.println("✅ Seed data loaded successfully");
    }
}