package com.example.timetable.scheduling.algorithm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = "ga")
@Component
public class SelectionProperties {

    private int tournamentSize;
}
