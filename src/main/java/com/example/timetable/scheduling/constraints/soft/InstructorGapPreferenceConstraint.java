package com.example.timetable.scheduling.constraints.soft;

import com.example.timetable.scheduling.algorithm.Chromosome;
import com.example.timetable.scheduling.algorithm.Gene;
import com.example.timetable.scheduling.constraints.SoftConstraint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

/**
 * Penalizes short idle gaps between an instructor's lectures
 * on the same day.
 */
@Component
public class InstructorGapPreferenceConstraint implements SoftConstraint {

    private final long minGapMinutes;

    public InstructorGapPreferenceConstraint(
            @Value("${ga.constraints.instructor.min-gap-minutes:30}")
            long minGapMinutes) {
        this.minGapMinutes = minGapMinutes;
    }

    @Override
    public String getName() {
        return "Instructor Idle Gap Preference";
    }

    @Override
    public int violations(Chromosome chromosome) {

        int violations = 0;
        List<Gene> genes = chromosome.getGenes();

        for (int i = 0; i < genes.size(); i++) {
            Gene g1 = genes.get(i);

            for (int j = i + 1; j < genes.size(); j++) {
                Gene g2 = genes.get(j);

                if (!g1.getClassSection().getInstructor()
                        .equals(g2.getClassSection().getInstructor())) {
                    continue;
                }

                if (!g1.getTimeSlot().getDayOfWeek()
                        .equals(g2.getTimeSlot().getDayOfWeek())) {
                    continue;
                }

                LocalTime end1 = g1.getTimeSlot().getEndTime();
                LocalTime start1 = g1.getTimeSlot().getStartTime();
                LocalTime end2 = g2.getTimeSlot().getEndTime();
                LocalTime start2 = g2.getTimeSlot().getStartTime();

                LocalTime firstEnd;
                LocalTime secondStart;

                if (end1.isBefore(start2)) {
                    firstEnd = end1;
                    secondStart = start2;
                } else if (end2.isBefore(start1)) {
                    firstEnd = end2;
                    secondStart = start1;
                } else {
                    continue;
                }

                long gapMinutes =
                        Duration.between(firstEnd, secondStart).toMinutes();

                if (gapMinutes > 0 && gapMinutes < minGapMinutes) {
                    violations++;
                }
            }
        }

        return violations;
    }
}
