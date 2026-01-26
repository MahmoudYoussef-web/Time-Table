package com.example.timetable.repository;

import com.example.timetable.model.ClassSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassSectionRepository extends JpaRepository<ClassSection, Long> {

    List<ClassSection> findByInstructorId(Long instructorId);

    List<ClassSection> findByCourseId(Long courseId);
}
