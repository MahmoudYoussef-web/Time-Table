package com.example.timetable.scheduling.algorithm.mutation;

import com.example.timetable.model.Room;
import com.example.timetable.model.TimeSlot;
import com.example.timetable.scheduling.algorithm.Chromosome;

import java.util.List;

public interface MutationStrategy {

    void mutate(Chromosome chromosome,
                List<Room> rooms,
                List<TimeSlot> slots,
                double mutationRate);
}
