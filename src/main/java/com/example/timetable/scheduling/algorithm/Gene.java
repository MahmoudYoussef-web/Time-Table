package com.example.timetable.scheduling.algorithm;

import com.example.timetable.entity.Section;
import com.example.timetable.entity.Room;
import com.example.timetable.entity.TimeSlot;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Gene {

    // Section assigned
    private Section section;

    // Assigned room
    private Room room;

    // Assigned time slot
    private TimeSlot timeSlot;

    public Gene copy() {
        return new Gene(section, room, timeSlot);
    }
}
