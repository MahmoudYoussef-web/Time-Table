package com.example.timetable.repository;

import com.example.timetable.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentRepository
        extends JpaRepository<Department, Long> {

    Optional<Department> findByCode(String code);

    boolean existsByCode(String code);

    // Get department by name
    Optional<Department> findByName(String name);
}
