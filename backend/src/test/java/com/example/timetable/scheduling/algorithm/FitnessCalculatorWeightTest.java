package com.example.timetable.scheduling.algorithm;

import com.example.timetable.scheduling.algorithm.config.FitnessProperties;
import com.example.timetable.scheduling.constraints.HardConstraint;
import com.example.timetable.scheduling.constraints.soft.SoftConstraint;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class FitnessCalculatorWeightTest {

    @Test
    void weightHardPenalizesViolationsExponentially() {
        FitnessProperties props = new FitnessProperties();
        props.setWeightHard(10);
        props.setWeightSoft(1);

        List<HardConstraint> hardConstraints = List.of(
                new HardConstraint() {
                    @Override
                    public String getName() { return "TEST_HARD"; }

                    @Override
                    public int violations(Chromosome c) {
                        return c.getHardViolations();
                    }
                }
        );

        List<SoftConstraint> softConstraints = List.of();

        FitnessCalculator calculator = new FitnessCalculator(props, hardConstraints, softConstraints);

        Chromosome clean = new Chromosome(List.of());
        Chromosome dirty = new Chromosome(List.of());
        dirty.setHardViolations(1);

        calculator.calculateFitness(List.of(clean, dirty));

        assertThat(clean.getFitness()).isCloseTo(1.0, within(0.0001));
        assertThat(dirty.getFitness()).isCloseTo(Math.exp(-10), within(0.0001));
    }

    @Test
    void weightSoftPenalizesSoftViolations() {
        FitnessProperties props = new FitnessProperties();
        props.setWeightHard(10);
        props.setWeightSoft(2);

        List<HardConstraint> hardConstraints = List.of();

        List<SoftConstraint> softConstraints = List.of(
                new SoftConstraint() {
                    @Override
                    public String name() { return "TEST_SOFT"; }

                    @Override
                    public double weight() { return 2.0; }

                    @Override
                    public int violations(Chromosome c) {
                        return c.getSoftViolations();
                    }
                }
        );

        FitnessCalculator calculator = new FitnessCalculator(props, hardConstraints, softConstraints);

        Chromosome clean = new Chromosome(List.of());
        Chromosome dirty = new Chromosome(List.of());
        dirty.setSoftViolations(1);

        calculator.calculateFitness(List.of(clean, dirty));

        double expectedPenalty = 1 * 2 * 2;
        assertThat(clean.getFitness()).isCloseTo(1.0, within(0.0001));
        assertThat(dirty.getFitness()).isCloseTo(Math.exp(-expectedPenalty), within(0.0001));
    }

    @Test
    void zeroWeightsResultInUniformFitness() {
        FitnessProperties props = new FitnessProperties();
        props.setWeightHard(0);
        props.setWeightSoft(0);

        List<HardConstraint> hardConstraints = List.of(
                new HardConstraint() {
                    @Override
                    public String getName() { return "TEST_HARD"; }

                    @Override
                    public int violations(Chromosome c) {
                        return c.getHardViolations();
                    }
                }
        );

        List<SoftConstraint> softConstraints = List.of(
                new SoftConstraint() {
                    @Override
                    public String name() { return "TEST_SOFT"; }

                    @Override
                    public double weight() { return 1.0; }

                    @Override
                    public int violations(Chromosome c) {
                        return c.getSoftViolations();
                    }
                }
        );

        FitnessCalculator calculator = new FitnessCalculator(props, hardConstraints, softConstraints);

        Chromosome c1 = new Chromosome(List.of());
        c1.setHardViolations(0);
        Chromosome c2 = new Chromosome(List.of());
        c2.setHardViolations(100);

        calculator.calculateFitness(List.of(c1, c2));

        assertThat(c1.getFitness()).isCloseTo(1.0, within(0.0001));
        assertThat(c2.getFitness()).isCloseTo(1.0, within(0.0001));
    }
}
