package com.example.timetable.service;

public enum ColorTheme {
    NAVY,
    BLACK;

    public static ColorTheme fromString(String theme) {
        if (theme == null) return NAVY;
        return switch (theme.toUpperCase()) {
            case "BLACK" -> BLACK;
            default -> NAVY;
        };
    }
}
