package com.example.timetable.scheduling.constraints;

import com.example.timetable.scheduling.algorithm.Chromosome;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HardConstraintValidator {

    private final List<HardConstraint> hardConstraints;

    public HardConstraintValidator(List<HardConstraint> hardConstraints) {
        this.hardConstraints = hardConstraints;
    }

    public int totalViolations(Chromosome chromosome) {
        return hardConstraints.stream()
                .mapToInt(c -> c.violations(chromosome))
                .sum();
    }

    public boolean isValid(Chromosome chromosome) {
        return totalViolations(chromosome) == 0;
    }
}
