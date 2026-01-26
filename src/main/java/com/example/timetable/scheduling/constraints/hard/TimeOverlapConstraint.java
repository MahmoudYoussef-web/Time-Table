package com.example.timetable.scheduling.constraints.hard;

import com.example.timetable.scheduling.algorithm.Chromosome;
import com.example.timetable.scheduling.algorithm.Gene;
import com.example.timetable.scheduling.constraints.HardConstraint;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

/**
 * Prevents overlapping time intervals for the same room,
 * instructor, or class section on the same day.
 */
@Component
public class TimeOverlapConstraint implements HardConstraint {

    @Override
    public String getName() {
        return "Time Overlap Conflict";
    }

    @Override
    public int violations(Chromosome chromosome) {

        int violations = 0;
        List<Gene> genes = chromosome.getGenes();

        for (int i = 0; i < genes.size(); i++) {
            Gene g1 = genes.get(i);

            for (int j = i + 1; j < genes.size(); j++) {
                Gene g2 = genes.get(j);

                if (!g1.getTimeSlot().getDayOfWeek()
                        .equals(g2.getTimeSlot().getDayOfWeek())) {
                    continue;
                }

                LocalTime s1 = g1.getTimeSlot().getStartTime();
                LocalTime e1 = g1.getTimeSlot().getEndTime();
                LocalTime s2 = g2.getTimeSlot().getStartTime();
                LocalTime e2 = g2.getTimeSlot().getEndTime();

                boolean overlap = s1.isBefore(e2) && s2.isBefore(e1);
                if (!overlap) continue;

                if (g1.getRoom().equals(g2.getRoom())
                        || g1.getClassSection().equals(g2.getClassSection())
                        || g1.getClassSection().getInstructor()
                        .equals(g2.getClassSection().getInstructor())) {
                    violations++;
                }
            }
        }

        return violations;
    }
}
