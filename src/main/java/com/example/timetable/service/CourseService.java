package com.example.timetable.service;

import com.example.timetable.model.Course;
import com.example.timetable.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;

    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    public Course save(Course course) {
        return courseRepository.save(course);
    }

    public Course findById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Course not found with id: " + id));
    }

    public void deleteById(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new NoSuchElementException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
    }
}
