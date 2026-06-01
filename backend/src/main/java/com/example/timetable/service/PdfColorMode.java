package com.example.timetable.service;

public enum PdfColorMode {
    COLOR,
    BLACK_AND_WHITE;

    public static PdfColorMode fromString(String mode) {
        return "bw".equalsIgnoreCase(mode) ? BLACK_AND_WHITE : COLOR;
    }
}
