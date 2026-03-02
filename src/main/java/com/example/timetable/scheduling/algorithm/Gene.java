package com.example.timetable.scheduling.algorithm;

import com.example.timetable.entity.Section;
import com.example.timetable.entity.Room;
import com.example.timetable.entity.TimeSlot;
import lombok.Data;

@Data
public class Gene {

    private Section section;
    private Room room;
    private TimeSlot timeSlot;

    // 🔒 Prevent mutation if true
    private boolean locked;

    public Gene(Section section, Room room, TimeSlot timeSlot) {
        this.section = section;
        this.room = room;
        this.timeSlot = timeSlot;
        this.locked = false;
    }

    public Gene copy() {
        Gene copy = new Gene(section, room, timeSlot);
        copy.setLocked(this.locked);
        return copy;
    }
}