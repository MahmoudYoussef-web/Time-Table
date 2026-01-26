package com.example.timetable.scheduling.algorithm.crossover;

import com.example.timetable.scheduling.algorithm.Chromosome;

public interface CrossoverStrategy {

    Chromosome crossover(Chromosome parent1, Chromosome parent2);
}
