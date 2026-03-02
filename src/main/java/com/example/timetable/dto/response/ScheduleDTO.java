package com.example.timetable.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class ScheduleDTO {

    private Long id;
    private double fitnessScore;
    private int hardViolations;
    private int softViolations;
    private String status;
    private LocalDateTime createdAt;
    private List<ScheduleEntryDTO> entries;

    public ScheduleDTO(
            Long id,
            double fitnessScore,
            int hardViolations,
            int softViolations,
            String status,
            LocalDateTime createdAt,
            List<ScheduleEntryDTO> entries
    ) {
        this.id = id;
        this.fitnessScore = fitnessScore;
        this.hardViolations = hardViolations;
        this.softViolations = softViolations;
        this.status = status;
        this.createdAt = createdAt;
        this.entries = entries;
    }

    public Long getId() {
        return id;
    }

    public double getFitnessScore() {
        return fitnessScore;
    }

    public int getHardViolations() {
        return hardViolations;
    }

    public int getSoftViolations() {
        return softViolations;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<ScheduleEntryDTO> getEntries() {
        return entries;
    }
}