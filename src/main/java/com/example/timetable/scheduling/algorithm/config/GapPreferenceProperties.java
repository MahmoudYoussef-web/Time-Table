package com.example.timetable.scheduling.algorithm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ga.gap")
public class GapPreferenceProperties {

    private long minMinutes;
}
