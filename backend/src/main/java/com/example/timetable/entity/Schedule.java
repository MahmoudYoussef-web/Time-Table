package com.example.timetable.entity;


import com.example.timetable.entity.enums.ScheduleStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "schedules",
        indexes = {
                @Index(name = "idx_schedule_semester", columnList = "semester_id"),
                @Index(name = "idx_schedule_status", columnList = "status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"entries", "semester"})
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private double fitnessScore;

    @Column(nullable = false)
    private int hardViolations;

    @Column(nullable = false)
    private int softViolations;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScheduleStatus status = ScheduleStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @OneToMany(mappedBy = "schedule",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<ScheduleEntry> entries = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "schedule_unscheduled_sections",
            joinColumns = @JoinColumn(name = "schedule_id"))
    @Column(name = "section_id")
    private List<Long> unscheduledSectionIds = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (status == null) {
            this.status = ScheduleStatus.DRAFT;
        }
    }
}
