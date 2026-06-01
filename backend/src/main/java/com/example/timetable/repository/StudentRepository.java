package com.example.timetable.repository;

import com.example.timetable.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository
        extends JpaRepository<Student, Long> {

    // Find student by user email
    Optional<Student> findByUserEmail(String email);

    // Check duplicate user email
    boolean existsByUserEmail(String email);

    // Get students by department
    List<Student> findByDepartmentId(Long departmentId);
}
