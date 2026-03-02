package com.example.timetable.scheduling.constraints.hard;

import com.example.timetable.scheduling.algorithm.Chromosome;
import com.example.timetable.scheduling.algorithm.Gene;
import com.example.timetable.scheduling.constraints.ConstraintViolation;
import com.example.timetable.scheduling.constraints.HardConstraint;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RoomConflictConstraint implements HardConstraint {

    @Override
    public String getName() {
        return "ROOM_CONFLICT";
    }

    @Override
    public int violations(Chromosome chromosome) {
        return explain(chromosome).size();
    }

    @Override
    public List<ConstraintViolation> explain(Chromosome chromosome) {

        List<ConstraintViolation> violations = new ArrayList<>();
        List<Gene> genes = chromosome.getGenes();

        for (int i = 0; i < genes.size(); i++) {
            Gene g1 = genes.get(i);

            for (int j = i + 1; j < genes.size(); j++) {
                Gene g2 = genes.get(j);

                if (g1.getTimeSlot().equals(g2.getTimeSlot())
                        && g1.getRoom().equals(g2.getRoom())) {

                    violations.add(
                            new ConstraintViolation(
                                    getName(),
                                    g1.getSection().getId(),
                                    "Room "
                                            + g1.getRoom().getRoomNumber()
                                            + " double booked at "
                                            + g1.getTimeSlot().getDay()
                            )
                    );
                }
            }
        }

        return violations;
    }
}