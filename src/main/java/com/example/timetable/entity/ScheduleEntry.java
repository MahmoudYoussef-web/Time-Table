package com.example.timetable.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "schedule_entries",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"room_id", "time_slot_id"}),
                @UniqueConstraint(columnNames = {"instructor_id", "time_slot_id"}),
                @UniqueConstraint(columnNames = {"section_id", "time_slot_id"})
        }
)
@Getter
@Setter
public class ScheduleEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Section for this schedule entry
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    // Instructor teaching this class
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private Instructor instructor;

    // Room where class takes place
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    // Time slot of this class
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id", nullable = false)
    private TimeSlot timeSlot;

    // Parent schedule
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    // Class type
    @Column(nullable = false, length = 30)
    private String type;

}
