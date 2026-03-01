package com.example.timetable.scheduling.algorithm;

import com.example.timetable.entity.Section;
import com.example.timetable.entity.Room;
import com.example.timetable.entity.TimeSlot;
import com.example.timetable.scheduling.algorithm.crossover.CrossoverStrategy;
import com.example.timetable.scheduling.algorithm.mutation.MutationStrategy;
import com.example.timetable.scheduling.algorithm.selection.SelectionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GeneticAlgorithm {

    private static final Logger log =
            LoggerFactory.getLogger(GeneticAlgorithm.class);

    private final int populationSize;
    private final int maxGenerations;
    private final double crossoverRate;
    private final double mutationRate;
    private final int elitismCount;
    private final double earlyStopThreshold;

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

    public Chromosome evolve(List<Section> sections,
                             List<Room> rooms,
                             List<TimeSlot> slots) {

        validateInputs(sections, rooms, slots);

        List<Chromosome> population =
                initializePopulation(sections, rooms, slots);

        for (int generation = 0; generation < maxGenerations; generation++) {

            fitnessCalculator.calculateFitness(population);
            population.sort(byFitnessDesc());

            Chromosome best = population.get(0);

            if (best.getFitness() >= earlyStopThreshold) {
                break;
            }

            population = produceNextGeneration(population, rooms, slots);
        }

        fitnessCalculator.calculateFitness(population);
        population.sort(byFitnessDesc());

        return population.get(0);
    }

    private List<Chromosome> produceNextGeneration(List<Chromosome> population,
                                                   List<Room> rooms,
                                                   List<TimeSlot> slots) {

        List<Chromosome> next = new ArrayList<>(populationSize);

        int eliteCount = Math.min(elitismCount, population.size());

        for (int i = 0; i < eliteCount; i++) {
            next.add(population.get(i).copy());
        }

        while (next.size() < populationSize) {

            Chromosome parent1 = selectionStrategy.select(population);
            Chromosome parent2 = selectionStrategy.select(population);

            Chromosome child =
                    random.nextDouble() < crossoverRate
                            ? crossoverStrategy.crossover(parent1, parent2)
                            : parent1.copy();

            mutationStrategy.mutate(child, rooms, slots, mutationRate);
            next.add(child);
        }

        return next;
    }

    private Comparator<Chromosome> byFitnessDesc() {
        return Comparator.comparingDouble(Chromosome::getFitness).reversed();
    }

    private void validateInputs(List<Section> sections,
                                List<Room> rooms,
                                List<TimeSlot> slots) {

        if (sections == null || sections.isEmpty())
            throw new IllegalArgumentException("No sections provided");

        if (rooms == null || rooms.isEmpty())
            throw new IllegalArgumentException("No rooms provided");

        if (slots == null || slots.isEmpty())
            throw new IllegalArgumentException("No time slots provided");
    }

    private List<Chromosome> initializePopulation(List<Section> sections,
                                                  List<Room> rooms,
                                                  List<TimeSlot> slots) {

        List<Chromosome> population = new ArrayList<>(populationSize);

        for (int i = 0; i < populationSize; i++) {

            List<Gene> genes = new ArrayList<>();

            for (Section section : sections) {
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
