package com.example.timetable.scheduling.algorithm.config;

import com.example.timetable.scheduling.algorithm.mutation.MutationStrategy;
import com.example.timetable.scheduling.algorithm.mutation.RandomMutation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Configuration
public class MutationConfig {

    @Bean
    public MutationStrategy mutationStrategy(GAProperties gaProperties) {
        return new RandomMutation(
                new Random(gaProperties.getRandomSeed())
        );
    }
}
