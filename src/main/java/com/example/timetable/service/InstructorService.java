package com.example.timetable.service;

import com.example.timetable.entity.Instructor;

import java.util.List;

public interface InstructorService {

    List<Instructor> findAll();

    Instructor findById(Long id);

    Instructor findByEmail(String email); // ✅ ADD

    Instructor save(Instructor instructor);

    void deleteById(Long id);
}
