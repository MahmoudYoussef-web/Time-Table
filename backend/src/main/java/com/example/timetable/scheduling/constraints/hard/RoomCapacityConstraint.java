package com.example.timetable.scheduling.constraints.hard;

import com.example.timetable.entity.Room;
import com.example.timetable.scheduling.algorithm.Chromosome;
import com.example.timetable.scheduling.algorithm.Gene;
import com.example.timetable.scheduling.constraints.HardConstraint;
import org.springframework.stereotype.Component;

@Component
public class RoomCapacityConstraint implements HardConstraint {

    @Override
    public String getName() {
        return "Room Capacity Constraint";
    }

    @Override
    public int violations(Chromosome chromosome) {

        int violations = 0;

        for (Gene gene : chromosome.getGenes()) {

            Room room = gene.getRoom();
            int sectionCapacity = gene.getSection().getCapacity();

            if (room.getCapacity() < sectionCapacity) {
                violations++;
            }
        }

        return violations;
    }
}
