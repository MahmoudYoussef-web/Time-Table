package com.example.timetable.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "schedule_history",
        indexes = {
                @Index(name = "idx_schedule_history_schedule", columnList = "schedule_id"),
                @Index(name = "idx_schedule_history_created", columnList = "created_at"),
                @Index(name = "idx_schedule_history_user", columnList = "created_by")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "schedule")
public class ScheduleHistory {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // Related schedule
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    // User who generated the schedule
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    // Generation timestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Optional notes (why regenerated)
    @Column(length = 255)
    private String note;

    // Auto set creation time
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
