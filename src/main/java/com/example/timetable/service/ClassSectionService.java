package com.example.timetable.service;

import com.example.timetable.model.ClassSection;
import com.example.timetable.repository.ClassSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class ClassSectionService {

    private final ClassSectionRepository repository;

    public List<ClassSection> findAll() {
        return repository.findAll();
    }

    public ClassSection save(ClassSection section) {
        return repository.save(section);
    }

    public ClassSection findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("ClassSection not found with id: " + id));
    }

    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("ClassSection not found with id: " + id);
        }
        repository.deleteById(id);
    }
}
