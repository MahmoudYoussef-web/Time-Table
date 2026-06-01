package com.example.timetable.service;

import com.example.timetable.entity.Course;

import java.util.List;

public interface CourseService {

    List<Course> findAll();

    Course findById(Long id);

    Course save(Course course);

    void deleteById(Long id);
}
