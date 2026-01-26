package com.example.timetable.mapper;

import com.example.timetable.dto.ScheduleDTO;
import com.example.timetable.dto.ScheduleEntryDTO;
import com.example.timetable.model.Schedule;
import com.example.timetable.model.ScheduleEntry;

import java.util.Comparator;
import java.util.List;

public class ScheduleMapper {

    public static ScheduleDTO toDTO(Schedule schedule) {

        List<ScheduleEntryDTO> entries =
                schedule.getEntries()
                        .stream()
                        .sorted(
                                Comparator
                                        .comparing(
                                                (ScheduleEntry e) ->
                                                        e.getTimeSlot().getDayOfWeek()
                                        )
                                        .thenComparing(
                                                e -> e.getTimeSlot().getStartTime()
                                        )
                        )
                        .map(ScheduleMapper::toEntryDTO)
                        .toList();

        return new ScheduleDTO(
                schedule.getId(),
                schedule.getFitnessScore(),
                schedule.getHardViolations(),
                schedule.getSoftViolations(),
                entries
        );
    }

    private static ScheduleEntryDTO toEntryDTO(ScheduleEntry entry) {

        return new ScheduleEntryDTO(
                entry.getClassSection().getId(),

                entry.getClassSection().getCourse().getCode(),
                entry.getClassSection().getCourse().getName(),

                entry.getClassSection().getInstructor().getName(),
                entry.getRoom().getRoomNumber(),

                // 👇 هنا التصليح
                entry.getTimeSlot().getDayOfWeek().name(),
                entry.getTimeSlot().getStartTime().toString(),
                entry.getTimeSlot().getEndTime().toString(),

                0,
                0
        );
    }
}
