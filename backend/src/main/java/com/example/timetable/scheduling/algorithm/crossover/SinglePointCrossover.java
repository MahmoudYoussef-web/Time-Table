package com.example.timetable.scheduling.algorithm.crossover;

import com.example.timetable.scheduling.algorithm.Chromosome;
import com.example.timetable.scheduling.algorithm.Gene;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Single-point crossover strategy for Genetic Algorithm.
 */
public class SinglePointCrossover implements CrossoverStrategy {

    private final Random random;

    public SinglePointCrossover(Random random) {
        this.random = random;
    }

    @Override
    public Chromosome crossover(Chromosome parent1, Chromosome parent2) {

        int size = parent1.getGenes().size();

        if (size == 0 || size != parent2.getGenes().size()) {
            throw new IllegalArgumentException(
                    "Parents must have non-empty gene lists of equal size"
            );
        }

        int midpoint = random.nextInt(size);

        List<Gene> offspringGenes = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            Gene sourceGene =
                    (i < midpoint)
                            ? parent1.getGenes().get(i)
                            : parent2.getGenes().get(i);

            offspringGenes.add(sourceGene.copy());
        }

        return new Chromosome(offspringGenes);
    }
}
