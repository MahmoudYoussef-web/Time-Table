package com.example.timetable.dto.response;

import java.util.List;

public record DayScheduleDTO(
        String day,
        List<SlotDTO> slots
) {}