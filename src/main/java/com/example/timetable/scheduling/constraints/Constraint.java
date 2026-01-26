package com.example.timetable.scheduling.constraints;

import com.example.timetable.scheduling.algorithm.Chromosome;

public interface Constraint {

    String getName();

    ConstraintType getType();

    /**
     * @return number of violations (>= 0)
     */
    int violations(Chromosome chromosome);
}
