package com.example.timetable.service;

import java.awt.Color;

public record PdfColorScheme(
    Color headerBg,
    Color headerFg,
    Color text,
    Color grayText,
    Color border,
    Color emptyBg
) {
    public static final PdfColorScheme COLOR = new PdfColorScheme(
        new Color(0x12, 0x3A, 0x8C),
        Color.WHITE,
        new Color(0x22, 0x22, 0x22),
        new Color(0x99, 0x99, 0x99),
        new Color(0xD3, 0xDE, 0xF2),
        Color.WHITE
    );

    public static final PdfColorScheme BW = new PdfColorScheme(
        Color.BLACK,
        Color.WHITE,
        Color.BLACK,
        new Color(0x88, 0x88, 0x88),
        new Color(0x44, 0x44, 0x44),
        Color.WHITE
    );
}
