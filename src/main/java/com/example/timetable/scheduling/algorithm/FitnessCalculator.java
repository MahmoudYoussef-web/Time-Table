package com.example.timetable.scheduling.algorithm;

import com.example.timetable.scheduling.algorithm.config.FitnessProperties;
import com.example.timetable.scheduling.constraints.HardConstraint;
import com.example.timetable.scheduling.constraints.soft.SoftConstraint;
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

            int hardViolations = 0;

            for (HardConstraint constraint : hardConstraints) {
                hardViolations += constraint.violations(chromosome);
            }

            int softViolations = 0;
            double softPenalty = 0;

            for (SoftConstraint constraint : softConstraints) {

                int violations = constraint.violations(chromosome);

                softViolations += violations;
                softPenalty += violations * constraint.weight();
            }

            double penalty =
                    (hardViolations * hardWeight)
                            + (softPenalty * softWeight);

            double fitness = Math.exp(-penalty);

            chromosome.setFitness(fitness);
            chromosome.setHardViolations(hardViolations);
            chromosome.setSoftViolations(softViolations);
        }
    }
}