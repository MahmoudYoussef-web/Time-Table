package com.example.timetable.controller.academic;

import com.example.timetable.entity.Section;
import com.example.timetable.service.SectionService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    @GetMapping
    public ResponseEntity<List<Section>> getAll() {
        return ResponseEntity.ok(
                sectionService.findAll()
        );
    }

    @PostMapping
    public ResponseEntity<Section> create(
            @RequestBody Section section
    ) {
        return ResponseEntity.ok(
                sectionService.save(section)
        );
    }
}