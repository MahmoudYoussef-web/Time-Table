package com.example.timetable.scheduling.algorithm.config;

import com.example.timetable.scheduling.algorithm.FitnessCalculator;
import com.example.timetable.scheduling.algorithm.GeneticAlgorithm;
import com.example.timetable.scheduling.algorithm.crossover.CrossoverStrategy;
import com.example.timetable.scheduling.algorithm.mutation.MutationStrategy;
import com.example.timetable.scheduling.algorithm.selection.SelectionStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeneticAlgorithmConfig {

    @Bean
    public GeneticAlgorithm geneticAlgorithm(
            GAProperties props,
            FitnessCalculator fitnessCalculator,
            SelectionStrategy selectionStrategy,
            CrossoverStrategy crossoverStrategy,
            MutationStrategy mutationStrategy
    ) {
        return new GeneticAlgorithm(
                props.getPopulationSize(),
                props.getMaxGenerations(),
                props.getCrossoverRate(),
                props.getMutationRate(),
                props.getElitismCount(),
                props.getEarlyStopThreshold(),
                props.getRandomSeed(),
                fitnessCalculator,
                selectionStrategy,
                crossoverStrategy,
                mutationStrategy
        );
    }
}
