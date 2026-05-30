package com.example.timetable.entity.enums;

public enum YearLevel {
    FIRST("First Year"),
    SECOND("Second Year"),
    THIRD("Third Year"),
    FOURTH("Fourth Year");

    private final String displayName;

    YearLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}