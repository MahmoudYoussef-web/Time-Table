package com.example.timetable.scheduling.algorithm.config;

import com.example.timetable.scheduling.algorithm.crossover.CrossoverStrategy;
import com.example.timetable.scheduling.algorithm.crossover.SinglePointCrossover;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Configuration
public class CrossoverConfig {

    @Bean
    public CrossoverStrategy crossoverStrategy(GAProperties gaProperties) {
        return new SinglePointCrossover(
                new Random(gaProperties.getRandomSeed())
        );
    }
}
