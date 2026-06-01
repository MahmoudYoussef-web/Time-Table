package com.example.timetable.scheduling.algorithm.selection;

import com.example.timetable.scheduling.algorithm.Chromosome;

import java.util.List;

public interface SelectionStrategy {

    Chromosome select(List<Chromosome> population);
}
