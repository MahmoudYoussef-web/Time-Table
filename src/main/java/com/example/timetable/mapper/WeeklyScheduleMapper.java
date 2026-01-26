package com.example.timetable.mapper;

import com.example.timetable.dto.ScheduleDTO;
import com.example.timetable.dto.ScheduleEntryDTO;
import com.example.timetable.dto.WeeklyScheduleDTO;

import java.util.HashMap;
import java.util.Map;

public class WeeklyScheduleMapper {

    public static WeeklyScheduleDTO toWeeklyTable(ScheduleDTO schedule) {

        Map<String, Map<String, ScheduleEntryDTO>> table = new HashMap<>();

        for (ScheduleEntryDTO entry : schedule.entries()) {

            String timeKey =
                    entry.startTime() + "-" + entry.endTime();

            String day =
                    entry.dayOfWeek();

            table
                    .computeIfAbsent(timeKey, k -> new HashMap<>())
                    .put(day, entry);
        }

        return new WeeklyScheduleDTO(table);
    }
}
