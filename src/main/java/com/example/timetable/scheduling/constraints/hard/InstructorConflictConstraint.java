package com.example.timetable.scheduling.constraints.hard;

import com.example.timetable.scheduling.algorithm.Chromosome;
import com.example.timetable.scheduling.algorithm.Gene;
import com.example.timetable.scheduling.constraints.HardConstraint;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Ensures that an instructor is not assigned to more than one class
 * in the same time slot.
 */
@Component
public class InstructorConflictConstraint implements HardConstraint {

    @Override
    public String getName() {
        return "Instructor Time Conflict";
    }

    @Override
    public int violations(Chromosome chromosome) {

        int violations = 0;
        List<Gene> genes = chromosome.getGenes();

        for (int i = 0; i < genes.size(); i++) {
            Gene g1 = genes.get(i);

            for (int j = i + 1; j < genes.size(); j++) {
                Gene g2 = genes.get(j);

                if (g1.getTimeSlot().equals(g2.getTimeSlot())
                        && g1.getClassSection().getInstructor()
                        .equals(g2.getClassSection().getInstructor())) {
                    violations++;
                }
            }
        }

        return violations;
    }
}
