package com.example.timetable.service.impl;

import com.example.timetable.dto.request.SemesterRequest;
import com.example.timetable.entity.Semester;
import com.example.timetable.entity.enums.SemesterStatus;
import com.example.timetable.repository.SemesterRepository;
import com.example.timetable.service.SemesterService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
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

    @Override
    @Transactional
    public Semester update(Long id, SemesterRequest request) {
        Semester existing = findById(id);
        existing.setName(request.name());
        existing.setStartDate(request.startDate());
        existing.setEndDate(request.endDate());
        if (request.status() != null) {
            SemesterStatus current = existing.getStatus();
            SemesterStatus next = request.status();
            boolean valid = (current == SemesterStatus.DRAFT && next == SemesterStatus.PUBLISHED)
                    || (current == SemesterStatus.PUBLISHED && next == SemesterStatus.CLOSED);
            if (!valid && current != next) {
                throw new IllegalStateException(
                        "Invalid status transition: " + current + " → " + next
                );
            }
            existing.setStatus(next);
        }
        return existing;
    }
}
