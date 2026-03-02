package com.example.timetable.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.timetable.entity.enums.ScheduleStatus;

@Entity
@Table(name = "schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "entries")
public class Schedule {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // Creation time
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Fitness score from GA
    @Column(nullable = false)
    private double fitnessScore;

    // Hard constraint violations
    @Column(nullable = false)
    private int hardViolations;

    // Soft constraint violations
    @Column(nullable = false)
    private int softViolations;

    // 🔥 NEW: Schedule Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleStatus status = ScheduleStatus.DRAFT;

    // 🔥 NEW: Link to Semester
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    // Schedule entries
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ScheduleEntry> entries = new ArrayList<>();

    // Auto timestamp
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
