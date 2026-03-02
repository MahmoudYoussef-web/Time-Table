package com.example.timetable.entity;

import com.example.timetable.entity.enums.JobStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "schedule_generation_jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleGenerationJob {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    private Long scheduleId;
}