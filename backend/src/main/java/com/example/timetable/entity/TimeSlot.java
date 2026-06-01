package com.example.timetable.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(
        name = "time_slots",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"day", "start_time", "end_time"}
                )
        }
)
@Getter
@Setter
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Day of the week
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DayOfWeek day;

    // Start time of the slot
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    // End time of the slot
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "slot_type", length = 20)
    private String slotType;

    // Schedule entries using this slot
    @OneToMany(mappedBy = "timeSlot")
    @JsonIgnore
    private List<ScheduleEntry> scheduleEntries;

    public String getSlotType() {
        return slotType;
    }
}
