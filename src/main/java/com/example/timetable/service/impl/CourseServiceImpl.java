package com.example.timetable.service.impl;

import com.example.timetable.entity.Course;
import com.example.timetable.repository.CourseRepository;
import com.example.timetable.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    @Override
    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    @Override
    public Course findById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Course not found with id: " + id));
    }

    @Override
    public Course save(Course course) {
        courseRepository.findByCode(course.getCode()).ifPresent(existing -> {
            if (course.getId() == null || !existing.getId().equals(course.getId())) {
                throw new IllegalArgumentException("Course code already exists");
            }
        });

        return courseRepository.save(course);
    }

    @Override
    public void deleteById(Long id) {

        if (!courseRepository.existsById(id)) {
            throw new NoSuchElementException("Course not found with id: " + id);
        }

        courseRepository.deleteById(id);
    }

}
