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

    @Override
    public void run(String... args) {
        if (courseRepository.count() > 0 && sectionRepository.count() > 0) {
            return;
        }

        /* ===== Department (find-or-create) ===== */

        Department cs = departmentRepository.findByCode("CS")
                .orElseGet(() -> {
                    Department d = new Department();
                    d.setCode("CS");
                    d.setName("Computer Science");
                    return departmentRepository.save(d);
                });

        /* ===== Users (Instructors) ===== */

        User u1 = userRepository.findByEmail("ahmed@uni.edu")
                .orElseGet(() -> {
                    User u = new User();
                    u.setFullName("Dr. Ahmed");
                    u.setEmail("ahmed@uni.edu");
                    u.setPassword(passwordEncoder.encode("123456"));
                    u.setRole(UserRole.INSTRUCTOR);
                    u.setEnabled(true);
                    return userRepository.save(u);
                });

        User u2 = userRepository.findByEmail("sara@uni.edu")
                .orElseGet(() -> {
                    User u = new User();
                    u.setFullName("Dr. Sara");
                    u.setEmail("sara@uni.edu");
                    u.setPassword(passwordEncoder.encode("123456"));
                    u.setRole(UserRole.INSTRUCTOR);
                    u.setEnabled(true);
                    return userRepository.save(u);
                });

        /* ===== Instructors (find-or-create by user) ===== */

        Instructor i1 = instructorRepository.findByUserId(u1.getId())
                .orElseGet(() -> {
                    Instructor i = new Instructor();
                    i.setUser(u1);
                    i.setDepartment(cs);
                    return instructorRepository.save(i);
                });

        Instructor i2 = instructorRepository.findByUserId(u2.getId())
                .orElseGet(() -> {
                    Instructor i = new Instructor();
                    i.setUser(u2);
                    i.setDepartment(cs);
                    return instructorRepository.save(i);
                });

        /* ===== Courses ===== */

        Course c1 = courseRepository.findByCode("CS101")
                .orElseGet(() -> {
                    Course c = new Course();
                    c.setCode("CS101");
                    c.setName("Programming");
                    c.setCreditHours(3);
                    c.setDepartment(cs);
                    return courseRepository.save(c);
                });

        Course c2 = courseRepository.findByCode("CS102")
                .orElseGet(() -> {
                    Course c = new Course();
                    c.setCode("CS102");
                    c.setName("Data Structures");
                    c.setCreditHours(3);
                    c.setDepartment(cs);
                    return courseRepository.save(c);
                });

        /* ===== Rooms ===== */

        Room r1 = roomRepository.findByBuildingAndRoomNumber("Main", "A101")
                .orElseGet(() -> {
                    Room r = new Room();
                    r.setBuilding("Main");
                    r.setRoomNumber("A101");
                    r.setCapacity(40);
                    return roomRepository.save(r);
                });

        Room r2 = roomRepository.findByBuildingAndRoomNumber("Main", "B202")
                .orElseGet(() -> {
                    Room r = new Room();
                    r.setBuilding("Main");
                    r.setRoomNumber("B202");
                    r.setCapacity(60);
                    return roomRepository.save(r);
                });

        /* ===== Time Slots ===== */

        TimeSlot t1 = timeSlotRepository
                .findByDayAndStartTimeAndEndTime(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0))
                .orElseGet(() -> {
                    TimeSlot t = new TimeSlot();
                    t.setDay(DayOfWeek.MONDAY);
                    t.setStartTime(LocalTime.of(9, 0));
                    t.setEndTime(LocalTime.of(11, 0));
                    return timeSlotRepository.save(t);
                });

        TimeSlot t2 = timeSlotRepository
                .findByDayAndStartTimeAndEndTime(DayOfWeek.MONDAY, LocalTime.of(11, 0), LocalTime.of(13, 0))
                .orElseGet(() -> {
                    TimeSlot t = new TimeSlot();
                    t.setDay(DayOfWeek.MONDAY);
                    t.setStartTime(LocalTime.of(11, 0));
                    t.setEndTime(LocalTime.of(13, 0));
                    return timeSlotRepository.save(t);
                });

        /* ===== Semester ===== */

        Semester semester = semesterRepository.findByName("Spring 2026")
                .orElseGet(() -> {
                    Semester s = new Semester();
                    s.setName("Spring 2026");
                    s.setStartDate(LocalDate.of(2026, 2, 1));
                    s.setEndDate(LocalDate.of(2026, 6, 1));
                    return semesterRepository.save(s);
                });

        /* ===== Sections ===== */

        sectionRepository.findByNameAndCourseIdAndSemesterId("A", c1.getId(), semester.getId())
                .orElseGet(() -> {
                    Section s = new Section();
                    s.setName("A");
                    s.setCourse(c1);
                    s.setInstructor(i1);
                    s.setCapacity(35);
                    s.setSemester(semester);
                    return sectionRepository.save(s);
                });

        sectionRepository.findByNameAndCourseIdAndSemesterId("B", c2.getId(), semester.getId())
                .orElseGet(() -> {
                    Section s = new Section();
                    s.setName("B");
                    s.setCourse(c2);
                    s.setInstructor(i2);
                    s.setCapacity(40);
                    s.setSemester(semester);
                    return sectionRepository.save(s);
                });

        System.out.println("Seed data loaded successfully");
    }
}