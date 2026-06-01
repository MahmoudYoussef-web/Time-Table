package com.example.timetable.scheduling.constraints;

import com.example.timetable.scheduling.algorithm.Chromosome;

import java.util.List;

public interface Constraint {

    String getName();

    ConstraintType getType();

    int violations(Chromosome chromosome);

    default List<ConstraintViolation> explain(Chromosome chromosome) {
        return List.of();
    }
}