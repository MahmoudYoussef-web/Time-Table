package com.example.timetable.scheduling.algorithm.config;

import com.example.timetable.scheduling.algorithm.selection.SelectionStrategy;
import com.example.timetable.scheduling.algorithm.selection.TournamentSelection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Configuration
public class SelectionConfig {

    @Bean
    public SelectionStrategy selectionStrategy(
            GAProperties gaProperties,
            SelectionProperties selectionProperties
    ) {
        return new TournamentSelection(
                new Random(gaProperties.getRandomSeed()),
                selectionProperties.getTournamentSize()
        );
    }
}
