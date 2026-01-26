package com.example.timetable.scheduling.algorithm;

import com.example.timetable.scheduling.algorithm.config.FitnessProperties;
import com.example.timetable.scheduling.constraints.HardConstraint;
import com.example.timetable.scheduling.constraints.SoftConstraint;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FitnessCalculator {

    private final double hardWeight;
    private final double softWeight;

    private final List<HardConstraint> hardConstraints;
    private final List<SoftConstraint> softConstraints;

    public FitnessCalculator(
            FitnessProperties properties,
            List<HardConstraint> hardConstraints,
            List<SoftConstraint> softConstraints
    ) {
        this.hardWeight = properties.getWeightHard();
        this.softWeight = properties.getWeightSoft();
        this.hardConstraints = hardConstraints;
        this.softConstraints = softConstraints;
    }

    public void calculateFitness(List<Chromosome> population) {

        for (Chromosome chromosome : population) {

            int hardViolations = hardConstraints.stream()
                    .mapToInt(c -> c.violations(chromosome))
                    .sum();

            int softViolations = softConstraints.stream()
                    .mapToInt(c -> c.violations(chromosome))
                    .sum();

            double penalty =
                    (hardViolations * hardWeight)
                            + (softViolations * softWeight);

            double fitness = Math.exp(-penalty);

            chromosome.setFitness(fitness);
            chromosome.setHardViolations(hardViolations);
            chromosome.setSoftViolations(softViolations);
        }
    }
}
