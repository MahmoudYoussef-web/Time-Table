package com.example.timetable.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(
        name = "rooms",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"building", "roomNumber"})
        }
)
@Getter
@Setter
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Building name
    @Column(nullable = false, length = 100)
    private String building;

    // Room number
    @Column(nullable = false, length = 50)
    private String roomNumber;

    // Maximum student capacity
    @Column(nullable = false)
    private int capacity;

    // Schedule entries in this room
    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private List<ScheduleEntry> scheduleEntries;

    // Exams in this room
    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private List<Exam> exams;
}
