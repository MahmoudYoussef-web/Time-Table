package com.example.timetable.service.impl;

import com.example.timetable.entity.Semester;
import com.example.timetable.repository.SemesterRepository;
import com.example.timetable.service.SemesterService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class SemesterServiceImpl implements SemesterService {

    private final SemesterRepository semesterRepository;

    @Override
    public List<Semester> findAll() {
        return semesterRepository.findAll();
    }

    @Override
    public Semester findById(Long id) {
        return semesterRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Semester not found with id: " + id));
    }

    @Override
    public Semester save(Semester semester) {
        return semesterRepository.save(semester);
    }
}