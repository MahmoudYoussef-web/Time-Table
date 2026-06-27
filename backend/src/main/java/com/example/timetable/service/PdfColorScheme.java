package com.example.timetable.service;

import java.awt.Color;

public record PdfColorScheme(
        Color headerBg,
        Color headerFg,
        Color outerBorder,
        Color gridBorder,
        Color text,
        Color grayText,
        Color timeBg,
        Color altBg,
        Color breakBg,
        Color breakFg,
        Color emptyColor,
        Color footerRule
) {
    public static final PdfColorScheme COLOR = new PdfColorScheme(
            new Color(0x0D, 0x2B, 0x6E),   // headerBg   — navy (matches PNG)
            Color.WHITE,                    // headerFg
            new Color(0x13, 0x38, 0x7A),   // outerBorder — navy frame
            new Color(0xE5, 0xE7, 0xEB),   // gridBorder  — light gray
            new Color(0x1E, 0x29, 0x3B),   // text
            new Color(0x4F, 0x5D, 0x75),   // grayText
            new Color(0xF8, 0xFA, 0xFC),   // timeBg
            new Color(0xF8, 0xFA, 0xFC),   // altBg       — even rows
            new Color(0xF3, 0xF4, 0xF6),   // breakBg
            new Color(0x6B, 0x72, 0x80),   // breakFg
            new Color(0x94, 0xA3, 0xB8),   // emptyColor  — dash
            new Color(0xD1, 0xD5, 0xDB)    // footerRule
    );

    public static final PdfColorScheme BW = new PdfColorScheme(
            Color.BLACK,                    // headerBg
            Color.WHITE,                    // headerFg
            new Color(0x55, 0x55, 0x55),   // outerBorder
            new Color(0xCC, 0xCC, 0xCC),   // gridBorder
            Color.BLACK,                    // text
            new Color(0x55, 0x55, 0x55),   // grayText
            new Color(0xF0, 0xF0, 0xF0),   // timeBg
            new Color(0xF5, 0xF5, 0xF5),   // altBg
            new Color(0xE0, 0xE0, 0xE0),   // breakBg
            new Color(0x44, 0x44, 0x44),   // breakFg
            new Color(0x88, 0x88, 0x88),   // emptyColor
            new Color(0x88, 0x88, 0x88)    // footerRule
    );
}