package com.example.timetable.scheduling.util;

import com.example.timetable.entity.Room;
import com.example.timetable.entity.Section;
import com.example.timetable.entity.enums.RoomType;
import com.example.timetable.entity.enums.SessionType;

import java.util.List;

public final class RoomTypeUtil {

    private RoomTypeUtil() {}

    public static boolean isCompatible(SessionType sessionType, RoomType roomType) {
        if (roomType == null) return true;
        if (sessionType == null) return true;
        return switch (sessionType) {
            case LECTURE -> roomType == RoomType.LECTURE_HALL;
            case LAB -> roomType == RoomType.LAB;
            case TUTORIAL, SEMINAR, SECTION ->
                    roomType == RoomType.SEMINAR_ROOM || roomType == RoomType.LECTURE_HALL;
        };
    }

    public static boolean isCompatible(Section section, Room room) {
        SessionType st = section.getSessionType() != null
                ? section.getSessionType()
                : SessionType.LECTURE;
        return isCompatible(st, room.getRoomType());
    }

    public static List<Room> filterCompatible(Section section, List<Room> rooms) {
        return rooms.stream()
                .filter(r -> isCompatible(section, r))
                .toList();
    }
}
