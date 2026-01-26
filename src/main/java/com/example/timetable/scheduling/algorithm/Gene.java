package com.example.timetable.scheduling.algorithm;

import com.example.timetable.model.ClassSection;
import com.example.timetable.model.Room;
import com.example.timetable.model.TimeSlot;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Gene {

    private ClassSection classSection;
    private Room room;
    private TimeSlot timeSlot;

    public Gene copy() {
        return new Gene(classSection, room, timeSlot);
    }
}
