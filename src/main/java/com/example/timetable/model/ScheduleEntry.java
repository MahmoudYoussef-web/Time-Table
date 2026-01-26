package com.example.timetable.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "schedule_entries",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"room_id", "time_slot_id"}),
                @UniqueConstraint(columnNames = {"class_section_id", "time_slot_id"})
        },
        indexes = {
                @Index(name = "idx_schedule", columnList = "schedule_id"),
                @Index(name = "idx_room", columnList = "room_id"),
                @Index(name = "idx_timeslot", columnList = "time_slot_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"schedule", "classSection", "room", "timeSlot"})
public class ScheduleEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_section_id", nullable = false)
    private ClassSection classSection;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "time_slot_id", nullable = false)
    private TimeSlot timeSlot;
}
