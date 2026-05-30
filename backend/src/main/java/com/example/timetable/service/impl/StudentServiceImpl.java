package com.example.timetable.service.impl;

import com.example.timetable.entity.Student;
import com.example.timetable.repository.StudentRepository;
import com.example.timetable.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    @Override
    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    @Override
    public Student findById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Student not found with id: " + id));
    }

    @Override
    public Student save(Student student) {
        return studentRepository.save(student);
    }

    @Override
    public void deleteById(Long id) {

        if (!studentRepository.existsById(id)) {
            throw new NoSuchElementException("Student not found with id: " + id);
        }

        studentRepository.deleteById(id);
    }
}
