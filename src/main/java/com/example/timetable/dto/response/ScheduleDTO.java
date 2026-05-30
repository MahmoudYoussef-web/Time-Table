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
    private List<Long> unscheduledSectionIds;
    private String semesterName;
    private String startDate;
    private String endDate;

    public List<Long> getUnscheduledSectionIds() { return unscheduledSectionIds; }

    public void setUnscheduledSectionIds(List<Long> unscheduledSectionIds) { this.unscheduledSectionIds = unscheduledSectionIds; }

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

    public String getSemesterName() { return semesterName; }

    public void setSemesterName(String semesterName) { this.semesterName = semesterName; }

    public String getStartDate() { return startDate; }

    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }

    public void setEndDate(String endDate) { this.endDate = endDate; }
}