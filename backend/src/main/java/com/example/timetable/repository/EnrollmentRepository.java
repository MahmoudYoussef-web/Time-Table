package com.example.timetable.repository;

import com.example.timetable.entity.Enrollment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository
        extends JpaRepository<Enrollment, Long> {

    // Get enrollments for student
    List<Enrollment> findByStudentId(Long studentId);

    // Get enrollments for section
    List<Enrollment> findBySectionId(Long sectionId);

    // Prevent duplicate enrollment
    boolean existsByStudentIdAndSectionId(
            Long studentId,
            Long sectionId
    );

    // Batch fetch enrollments for multiple sections (eagerly loads student)
    @EntityGraph(attributePaths = {"student"})
    List<Enrollment> findBySectionIdIn(List<Long> sectionIds);
}
