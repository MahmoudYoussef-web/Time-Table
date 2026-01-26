package com.example.timetable.config;

import com.example.timetable.model.*;
import com.example.timetable.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Configuration
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final CourseRepository courseRepository;
    private final RoomRepository roomRepository;
    private final InstructorRepository instructorRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ClassSectionRepository classSectionRepository;

    @Override
    public void run(String... args) {

        if (courseRepository.count() > 0) {
            return; // prevent duplicate seeding
        }

        /* ===== Courses ===== */
        Course c1 = courseRepository.save(
                new Course(null, "CS101", "Programming", 3));
        Course c2 = courseRepository.save(
                new Course(null, "CS102", "Data Structures", 3));
        Course c3 = courseRepository.save(
                new Course(null, "CS201", "Databases", 3));

        /* ===== Instructors ===== */
        Instructor i1 = instructorRepository.save(
                new Instructor(null, "Dr. Ahmed", "ahmed@uni.edu"));
        Instructor i2 = instructorRepository.save(
                new Instructor(null, "Dr. Sara", "sara@uni.edu"));

        /* ===== Rooms ===== */
        Room r1 = roomRepository.save(
                new Room(null, "A101", 40));
        Room r2 = roomRepository.save(
                new Room(null, "B202", 60));

        /* ===== TimeSlots ===== */
        TimeSlot t1 = timeSlotRepository.save(
                new TimeSlot(
                        null,
                        DayOfWeek.MONDAY,
                        LocalTime.of(9, 0),
                        LocalTime.of(11, 0)
                )
        );

        TimeSlot t2 = timeSlotRepository.save(
                new TimeSlot(
                        null,
                        DayOfWeek.MONDAY,
                        LocalTime.of(11, 0),
                        LocalTime.of(13, 0)
                )
        );

        TimeSlot t3 = timeSlotRepository.save(
                new TimeSlot(
                        null,
                        DayOfWeek.WEDNESDAY,
                        LocalTime.of(9, 0),
                        LocalTime.of(11, 0)
                )
        );

        /* ===== Class Sections ===== */
        classSectionRepository.save(
                new ClassSection(null, c1, i1, 35));
        classSectionRepository.save(
                new ClassSection(null, c2, i2, 40));
        classSectionRepository.save(
                new ClassSection(null, c3, i1, 30));

        System.out.println("✅ Seed data loaded successfully");
    }
}
