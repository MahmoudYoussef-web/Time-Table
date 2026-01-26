package com.example.timetable.scheduling.algorithm;

import com.example.timetable.model.ClassSection;
import com.example.timetable.model.Room;
import com.example.timetable.model.TimeSlot;
import com.example.timetable.scheduling.algorithm.crossover.CrossoverStrategy;
import com.example.timetable.scheduling.algorithm.mutation.MutationStrategy;
import com.example.timetable.scheduling.algorithm.selection.SelectionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Pure Genetic Algorithm Engine (Framework-agnostic)
 */
public class GeneticAlgorithm {

    private static final Logger log =
            LoggerFactory.getLogger(GeneticAlgorithm.class);

    /* ===================== CONFIGURATION ===================== */

    private final int populationSize;
    private final int maxGenerations;
    private final double crossoverRate;
    private final double mutationRate;
    private final int elitismCount;
    private final double earlyStopThreshold;

    /* ===================== DEPENDENCIES ===================== */

    private final Random random;
    private final FitnessCalculator fitnessCalculator;
    private final SelectionStrategy selectionStrategy;
    private final CrossoverStrategy crossoverStrategy;
    private final MutationStrategy mutationStrategy;

    public GeneticAlgorithm(int populationSize,
                            int maxGenerations,
                            double crossoverRate,
                            double mutationRate,
                            int elitismCount,
                            double earlyStopThreshold,
                            long randomSeed,
                            FitnessCalculator fitnessCalculator,
                            SelectionStrategy selectionStrategy,
                            CrossoverStrategy crossoverStrategy,
                            MutationStrategy mutationStrategy) {

        this.populationSize = populationSize;
        this.maxGenerations = maxGenerations;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        this.elitismCount = elitismCount;
        this.earlyStopThreshold = earlyStopThreshold;
        this.random = new Random(randomSeed);

        this.fitnessCalculator = fitnessCalculator;
        this.selectionStrategy = selectionStrategy;
        this.crossoverStrategy = crossoverStrategy;
        this.mutationStrategy = mutationStrategy;
    }

    /* ===================== MAIN EVOLUTION ===================== */

    public Chromosome evolve(List<ClassSection> sections,
                             List<Room> rooms,
                             List<TimeSlot> slots) {

        validateInputs(sections, rooms, slots);

        List<Chromosome> population =
                initializePopulation(sections, rooms, slots);

        for (int generation = 0; generation < maxGenerations; generation++) {

            evaluatePopulation(population);
            population.sort(byFitnessDesc());

            Chromosome best = population.get(0);
            logGenerationStats(generation, best);

            if (shouldEarlyStop(best)) {
                break;
            }

            population = produceNextGeneration(population, rooms, slots);
        }

        evaluatePopulation(population);
        population.sort(byFitnessDesc());

        return population.get(0);
    }

    /* ===================== GENERATION STEPS ===================== */

    private List<Chromosome> produceNextGeneration(List<Chromosome> population,
                                                   List<Room> rooms,
                                                   List<TimeSlot> slots) {

        List<Chromosome> nextGeneration = new ArrayList<>(populationSize);

        applyElitism(population, nextGeneration);

        while (nextGeneration.size() < populationSize) {

            Chromosome parent1 = selectionStrategy.select(population);
            Chromosome parent2 = selectionStrategy.select(population);

            Chromosome offspring =
                    random.nextDouble() < crossoverRate
                            ? crossoverStrategy.crossover(parent1, parent2)
                            : parent1.copy();

            mutationStrategy.mutate(offspring, rooms, slots, mutationRate);
            nextGeneration.add(offspring);
        }

        return nextGeneration;
    }

    private void applyElitism(List<Chromosome> population,
                              List<Chromosome> nextGeneration) {

        int safeElitismCount = Math.min(elitismCount, population.size());
        for (int i = 0; i < safeElitismCount; i++) {
            nextGeneration.add(population.get(i).copy());
        }
    }

    /* ===================== FITNESS & LOGGING ===================== */

    private void evaluatePopulation(List<Chromosome> population) {
        fitnessCalculator.calculateFitness(population);
    }

    private boolean shouldEarlyStop(Chromosome best) {
        return best.getFitness() >= earlyStopThreshold;
    }

    private void logGenerationStats(int generation, Chromosome best) {
        log.info(
                "Generation {} | Fitness={} | Hard={} | Soft={}",
                generation,
                best.getFitness(),
                best.getHardViolations(),
                best.getSoftViolations()
        );
    }

    private Comparator<Chromosome> byFitnessDesc() {
        return Comparator.comparingDouble(Chromosome::getFitness).reversed();
    }

    /* ===================== INPUT VALIDATION ===================== */

    private void validateInputs(List<ClassSection> sections,
                                List<Room> rooms,
                                List<TimeSlot> slots) {

        if (sections == null || sections.isEmpty()) {
            throw new IllegalArgumentException("No class sections provided");
        }
        if (rooms == null || rooms.isEmpty()) {
            throw new IllegalArgumentException("No rooms provided");
        }
        if (slots == null || slots.isEmpty()) {
            throw new IllegalArgumentException("No time slots provided");
        }
    }

    /* ===================== INITIAL POPULATION ===================== */

    private List<Chromosome> initializePopulation(List<ClassSection> sections,
                                                  List<Room> rooms,
                                                  List<TimeSlot> slots) {

        List<Chromosome> population = new ArrayList<>(populationSize);

        for (int i = 0; i < populationSize; i++) {

            List<Gene> genes = new ArrayList<>(sections.size());

            for (ClassSection section : sections) {
                genes.add(new Gene(
                        section,
                        rooms.get(random.nextInt(rooms.size())),
                        slots.get(random.nextInt(slots.size()))
                ));
            }

            population.add(new Chromosome(genes));
        }

        return population;
    }
}
