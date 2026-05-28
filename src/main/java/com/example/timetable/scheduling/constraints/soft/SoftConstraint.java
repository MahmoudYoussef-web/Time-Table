package com.example.timetable.scheduling.constraints.soft;

import com.example.timetable.scheduling.algorithm.Chromosome;

public interface SoftConstraint extends com.example.timetable.scheduling.constraints.SoftConstraint {

    String name();

    double weight();

    int violations(Chromosome chromosome);

    @Override
    default String getName() {
        return name();
    }
}
