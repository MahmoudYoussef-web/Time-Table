package com.example.timetable.scheduling.constraints.soft;


import com.example.timetable.scheduling.algorithm.Chromosome;

public interface SoftConstraint {

    String name();

    double weight();

    int violations(Chromosome chromosome);
}
