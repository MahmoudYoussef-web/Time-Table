package com.example.timetable.service.impl;

import com.example.timetable.entity.Section;
import com.example.timetable.repository.SectionRepository;
import com.example.timetable.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;

    @Override
    public List<Section> findAll() {
        return sectionRepository.findAll();
    }

    @Override
    public Section findById(Long id) {
        return sectionRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Section not found with id: " + id));
    }

    @Override
    public Section save(Section section) {
        return sectionRepository.save(section);
    }

    @Override
    public void deleteById(Long id) {

        if (!sectionRepository.existsById(id)) {
            throw new NoSuchElementException("Section not found with id: " + id);
        }

        sectionRepository.deleteById(id);
    }
}
