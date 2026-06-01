package com.example.timetable.repository;

import com.example.timetable.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SectionRepository
        extends JpaRepository<Section, Long> {

    // Get all sections taught by an instructor
    List<Section> findByInstructorId(Long instructorId);

    // Get all sections for a course
    List<Section> findByCourseId(Long courseId);

    // Get all sections in a department
    List<Section> findByCourseDepartmentId(Long departmentId);

    List<Section> findBySemesterId(Long semesterId);

    Optional<Section> findByNameAndCourseIdAndSemesterId(String name, Long courseId, Long semesterId);
}
