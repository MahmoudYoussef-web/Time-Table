package com.example.timetable.service;

import com.example.timetable.entity.Enrollment;

import java.util.List;

public interface EnrollmentService {

    List<Enrollment> findAll();

    Enrollment findById(Long id);

    Enrollment save(Enrollment enrollment);

    void deleteById(Long id);
}
