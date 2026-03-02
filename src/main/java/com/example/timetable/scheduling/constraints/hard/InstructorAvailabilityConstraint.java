package com.example.timetable.scheduling.constraints.hard;

import com.example.timetable.entity.InstructorAvailability;
import com.example.timetable.scheduling.algorithm.Chromosome;
import com.example.timetable.scheduling.algorithm.Gene;
import com.example.timetable.scheduling.constraints.HardConstraint;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class InstructorAvailabilityConstraint implements HardConstraint {

    private final Set<String> unavailableCache = new HashSet<>();

    public void preload(Set<InstructorAvailability> unavailable) {
        unavailableCache.clear();
        for (InstructorAvailability ua : unavailable) {
            unavailableCache.add(key(
                    ua.getInstructor().getId(),
                    ua.getTimeSlot().getId()));
        }
    }

    @Override
    public String getName() {
        return "Instructor Availability Constraint";
    }

    @Override
    public int violations(Chromosome chromosome) {

        int violations = 0;

        for (Gene gene : chromosome.getGenes()) {

            String key = key(
                    gene.getSection().getInstructor().getId(),
                    gene.getTimeSlot().getId());

            if (unavailableCache.contains(key)) {
                violations++;
            }
        }

        return violations;
    }

    private String key(Long instructorId, Long slotId) {
        return instructorId + "_" + slotId;
    }
}