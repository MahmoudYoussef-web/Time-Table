package com.example.timetable.service;

import com.example.timetable.entity.Semester;

import java.util.List;

public interface SemesterService {

    List<Semester> findAll();

    Semester findById(Long id);

    Semester save(Semester semester);
}