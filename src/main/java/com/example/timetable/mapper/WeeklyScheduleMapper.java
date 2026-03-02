package com.example.timetable.mapper;

import com.example.timetable.dto.response.*;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

public class WeeklyScheduleMapper {

    public static WeeklyScheduleDTO toWeeklyTable(
            ScheduleDTO schedule
    ) {

        Map<String, List<ScheduleEntryDTO>> groupedByDay =
                schedule.getEntries()
                        .stream()
                        .collect(Collectors.groupingBy(
                                ScheduleEntryDTO::dayOfWeek
                        ));

        List<DayScheduleDTO> days = new ArrayList<>();

        for (DayOfWeek day : DayOfWeek.values()) {

            String dayName = day.name();

            List<ScheduleEntryDTO> entries =
                    groupedByDay.getOrDefault(dayName, List.of());

            List<SlotDTO> slots =
                    entries.stream()
                            .sorted(
                                    Comparator.comparing(
                                            ScheduleEntryDTO::startTime
                                    )
                            )
                            .map(entry ->
                                    new SlotDTO(
                                            entry.startTime(),
                                            entry.endTime(),
                                            entry
                                    )
                            )
                            .collect(Collectors.toList());

            days.add(new DayScheduleDTO(dayName, slots));
        }

        return new WeeklyScheduleDTO(days);
    }
}