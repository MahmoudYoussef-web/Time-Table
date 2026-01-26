package com.example.timetable.scheduling.algorithm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ga")
public class GAProperties {

    private int populationSize;
    private int maxGenerations;
    private double crossoverRate;
    private double mutationRate;
    private int elitismCount;
    private double earlyStopThreshold;
    private long randomSeed;
}
