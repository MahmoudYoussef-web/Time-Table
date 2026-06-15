package com.example.timetable.service;

public enum CourseCategory {

    PROGRAMMING  ("#E8F5E9", "#A5D6A7", "#2E7D32", "Programming"),
    CS_CORE      ("#E3F2FD", "#90CAF9", "#1565C0", "CS Core"),
    ENGINEERING  ("#F3E5F5", "#CE93D8", "#6A1B9A", "Engineering"),
    MATH_SCIENCE ("#FFFDE7", "#FFF176", "#F57F17", "Math / Science"),
    WEB          ("#FCE4EC", "#F48FB1", "#AD1457", "Web"),
    GENERAL      ("#E0F7FA", "#80DEEA", "#00695C", "General");

    public final String bgHex;
    public final String borderHex;
    public final String codeHex;
    public final String label;

    CourseCategory(String bgHex, String borderHex, String codeHex, String label) {
        this.bgHex     = bgHex;
        this.borderHex = borderHex;
        this.codeHex   = codeHex;
        this.label     = label;
    }

    public static CourseCategory fromCode(String courseCode) {
        if (courseCode == null || courseCode.isBlank()) return GENERAL;
        String upper = courseCode.trim().toUpperCase().replaceAll("\s+", "");

        // Extract prefix (letters) and numeric part
        String prefix = upper.replaceAll("[0-9]", ""); // e.g., CS, IT, IS, AI, MATH
        String numStr = upper.replaceAll("[^0-9]", ""); // e.g., 101
        int num = 0;
        try { num = Integer.parseInt(numStr); } catch (Exception ignored) {}

        return switch (prefix) {
            case "CS" -> classifyCs(num);
            case "IT" -> CS_CORE;
            case "IS" -> ENGINEERING;
            case "AI" -> CS_CORE;
            case "MATH" -> MATH_SCIENCE;
            default -> GENERAL;
        };
    }

    private static CourseCategory classifyCs(int num) {
        // Specific CS course number classifications
        if (num == 101 || num == 105 || num == 201) return PROGRAMMING;
        if (num == 103 || num == 104 || num == 111 || num == 204) return MATH_SCIENCE;
        if (num == 106) return GENERAL;
        if (num == 206 || num == 207) return WEB;
        if (num == 202 || num == 203 || num == 301 || num == 302 || num == 303 || num == 304 ||
            num == 305 || num == 320 || num == 401) return ENGINEERING;
        // Default for any other CS code
        return CS_CORE;
    }

    public static CourseCategory[] legendOrder() {
        return new CourseCategory[]{
            PROGRAMMING, CS_CORE, ENGINEERING, MATH_SCIENCE, WEB, GENERAL
        };
    }
}
