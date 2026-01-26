package com.example.timetable.scheduling.constraints.hard;

import com.example.timetable.model.ClassSection;
import com.example.timetable.model.Room;
import com.example.timetable.scheduling.algorithm.Chromosome;
import com.example.timetable.scheduling.algorithm.Gene;
import com.example.timetable.scheduling.constraints.HardConstraint;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoomCapacityConstraint implements HardConstraint {

    @Override
    public String getName() {
        return "Room Capacity Constraint";
    }

    @Override
    public int violations(Chromosome chromosome) {

        int violations = 0;
        List<Gene> genes = chromosome.getGenes();

        for (Gene gene : genes) {
            Room room = gene.getRoom();
            ClassSection section = gene.getClassSection();

            if (room.getCapacity() < section.getNumberOfStudents()) {
                violations++;
            }
        }

        return violations;
    }
}
