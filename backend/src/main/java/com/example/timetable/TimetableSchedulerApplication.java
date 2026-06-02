package com.example.timetable;

import com.example.timetable.scheduling.algorithm.config.GAProperties;
import com.example.timetable.scheduling.algorithm.config.SelectionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@EnableConfigurationProperties({
        GAProperties.class,
        SelectionProperties.class
})
@PropertySource(value = "file:./src/main/resources/application.properties", ignoreResourceNotFound = true)
public class TimetableSchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimetableSchedulerApplication.class, args);
    }
}
