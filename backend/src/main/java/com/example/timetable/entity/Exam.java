package com.example.timetable.entity;

import com.example.timetable.entity.enums.ExamType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
        name = "exams",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"room_id", "examDate", "startTime"})
        }
)
@Getter
@Setter
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Section this exam belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    // Room where exam takes place
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    // Exam type (Midterm, Final, etc.)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ExamType type;

    // Exam date
    @Column(nullable = false)
    private LocalDate examDate;

    // Exam start time
    @Column(nullable = false)
    private LocalTime startTime;

    // Exam end time
    @Column(nullable = false)
    private LocalTime endTime;
}
