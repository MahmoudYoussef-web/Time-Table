package com.example.timetable.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "rooms",
        indexes = {
                @Index(name = "idx_room_number", columnList = "room_number")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "room_number", nullable = false, unique = true, length = 50)
    private String roomNumber;

    @Column(nullable = false)
    private int capacity;
}
