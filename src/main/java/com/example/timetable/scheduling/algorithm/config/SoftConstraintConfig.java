package com.example.timetable.scheduling.algorithm.config;

import com.example.timetable.scheduling.constraints.SoftConstraint;
import com.example.timetable.scheduling.constraints.soft.InstructorBackToBackConstraint;
import com.example.timetable.scheduling.constraints.soft.InstructorGapPreferenceConstraint;
import com.example.timetable.scheduling.constraints.soft.SameCourseSameDayConstraint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SoftConstraintConfig {

    @Bean
    public List<SoftConstraint> softConstraints(
            GapPreferenceProperties gapProps
    ) {
        return List.of(
                new InstructorBackToBackConstraint(),
                new InstructorGapPreferenceConstraint(gapProps.getMinMinutes()),
                new SameCourseSameDayConstraint()
        );
    }
}
