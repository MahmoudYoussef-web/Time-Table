package com.example.timetable.service;

import com.example.timetable.model.Instructor;
import com.example.timetable.repository.InstructorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class InstructorService {

    private final InstructorRepository instructorRepository;

    public List<Instructor> findAll() {
        return instructorRepository.findAll();
    }

    public Instructor save(Instructor instructor) {
        return instructorRepository.save(instructor);
    }

    public Instructor findById(Long id) {
        return instructorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Instructor not found with id: " + id));
    }

    public void deleteById(Long id) {
        if (!instructorRepository.existsById(id)) {
            throw new NoSuchElementException("Instructor not found with id: " + id);
        }
        instructorRepository.deleteById(id);
    }
}
