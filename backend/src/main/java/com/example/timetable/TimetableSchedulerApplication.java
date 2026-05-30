package com.example.timetable;

import com.example.timetable.scheduling.algorithm.config.GAProperties;
import com.example.timetable.scheduling.algorithm.config.SelectionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        GAProperties.class,
        SelectionProperties.class
})
public class TimetableSchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimetableSchedulerApplication.class, args);
    }
}
