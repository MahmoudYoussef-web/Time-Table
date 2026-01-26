package com.example.timetable.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(
        name = "time_slots",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"day_of_week", "start_time", "end_time"})
        },
        indexes = {
                @Index(name = "idx_day", columnList = "day_of_week"),
                @Index(name = "idx_time_range", columnList = "start_time, end_time")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    @JsonFormat(pattern = "HH:mm")
    @Schema(type = "string", example = "09:00")
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    @JsonFormat(pattern = "HH:mm")
    @Schema(type = "string", example = "11:00")
    private LocalTime endTime;
}
