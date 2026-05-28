package com.example.timetable.scheduling.constraints.soft;

import com.example.timetable.scheduling.algorithm.Chromosome;
import com.example.timetable.scheduling.algorithm.Gene;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

@Component
public class InstructorBackToBackConstraint implements SoftConstraint {

    @Override
    public String name() {
        return "Instructor Back-To-Back Lectures";
    }

    @Override
    public double weight() {
        return 2.0;
    }

    @Override
    public int violations(Chromosome chromosome) {

        int violations = 0;
        List<Gene> genes = chromosome.getGenes();

        for (int i = 0; i < genes.size(); i++) {
            Gene g1 = genes.get(i);

            for (int j = i + 1; j < genes.size(); j++) {
                Gene g2 = genes.get(j);

                if (!g1.getSection().getInstructor()
                        .equals(g2.getSection().getInstructor())) {
                    continue;
                }

                if (!g1.getTimeSlot().getDay()
                        .equals(g2.getTimeSlot().getDay())) {
                    continue;
                }

                LocalTime end1 = g1.getTimeSlot().getEndTime();
                LocalTime start1 = g1.getTimeSlot().getStartTime();
                LocalTime end2 = g2.getTimeSlot().getEndTime();
                LocalTime start2 = g2.getTimeSlot().getStartTime();

                if (end1.equals(start2) || end2.equals(start1)) {
                    violations++;
                }
            }
        }

        return violations;
    }
}
