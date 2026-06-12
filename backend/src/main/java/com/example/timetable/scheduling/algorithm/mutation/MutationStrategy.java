package com.example.timetable.scheduling.algorithm.mutation;

import com.example.timetable.entity.Room;
import com.example.timetable.entity.Section;
import com.example.timetable.entity.TimeSlot;
import com.example.timetable.scheduling.algorithm.Chromosome;

import java.util.List;
import java.util.function.BiPredicate;

public interface MutationStrategy {

    void mutate(Chromosome chromosome,
                List<Room> rooms,
                List<TimeSlot> slots,
                double mutationRate,
                BiPredicate<Section, Room> roomFilter);

}
