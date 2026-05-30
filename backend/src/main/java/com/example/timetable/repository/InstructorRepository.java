package com.example.timetable.repository;

import com.example.timetable.entity.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InstructorRepository
        extends JpaRepository<Instructor, Long> {

    // Find instructor by linked user id
    Optional<Instructor> findByUserId(Long userId);

    // Check if instructor already linked to a user
    boolean existsByUserId(Long userId);

    // Get all instructors in a department
    List<Instructor> findByDepartmentId(Long departmentId);

    // Find instructor by linked user email
    Optional<Instructor> findByUserEmail(String email);
}
