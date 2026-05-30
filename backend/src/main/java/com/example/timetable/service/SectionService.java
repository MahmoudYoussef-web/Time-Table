package com.example.timetable.service;

import com.example.timetable.entity.Section;

import java.util.List;

public interface SectionService {

    List<Section> findAll();

    Section findById(Long id);

    Section save(Section section);

    void deleteById(Long id);
}
