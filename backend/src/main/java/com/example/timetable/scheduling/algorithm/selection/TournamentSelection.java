package com.example.timetable.scheduling.algorithm.selection;

import com.example.timetable.scheduling.algorithm.Chromosome;

import java.util.List;
import java.util.Random;

/**
 * Pure Tournament Selection Strategy
 */
public class TournamentSelection implements SelectionStrategy {

    private final Random random;
    private final int tournamentSize;

    public TournamentSelection(Random random, int tournamentSize) {
        if (tournamentSize < 2) {
            throw new IllegalArgumentException(
                    "Tournament size must be >= 2"
            );
        }
        this.random = random;
        this.tournamentSize = tournamentSize;
    }

    @Override
    public Chromosome select(List<Chromosome> population) {

        Chromosome best = null;

        for (int i = 0; i < tournamentSize; i++) {

            Chromosome candidate =
                    population.get(random.nextInt(population.size()));

            if (best == null ||
                    candidate.getFitness() > best.getFitness()) {
                best = candidate;
            }
        }
        return best;
    }
}
