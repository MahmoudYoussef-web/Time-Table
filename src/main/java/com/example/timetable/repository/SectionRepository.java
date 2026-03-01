package com.example.timetable.repository;

import com.example.timetable.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionRepository
        extends JpaRepository<Section, Long> {

    // Get all sections taught by an instructor
    List<Section> findByInstructorId(Long instructorId);

    // Get all sections for a course
    List<Section> findByCourseId(Long courseId);

    // Get all sections in a department
    List<Section> findByCourseDepartmentId(Long departmentId);
}
