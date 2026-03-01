package com.example.timetable.mapper;

import com.example.timetable.dto.response.ScheduleDTO;
import com.example.timetable.dto.response.ScheduleEntryDTO;
import com.example.timetable.dto.response.WeeklyScheduleDTO;

import java.util.HashMap;
import java.util.Map;

public class WeeklyScheduleMapper {

    // Convert schedule to weekly table format
    public static WeeklyScheduleDTO toWeeklyTable(
            ScheduleDTO schedule
    ) {

        Map<String, Map<String, ScheduleEntryDTO>> table =
                new HashMap<>();

        for (ScheduleEntryDTO entry : schedule.entries()) {

            // Example: 09:00-11:00
            String timeKey =
                    entry.startTime() + "-" + entry.endTime();

            // Example: MONDAY
            String day =
                    entry.dayOfWeek();

            table
                    .computeIfAbsent(timeKey, k -> new HashMap<>())
                    .put(day, entry);
        }

        return new WeeklyScheduleDTO(table);
    }
}
