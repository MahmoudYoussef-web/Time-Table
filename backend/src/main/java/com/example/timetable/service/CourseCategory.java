package com.example.timetable.service;

public enum CourseCategory {

    PROGRAMMING  ("#EEF8EE", "#B2DFDB", "#2E7D32", "Programming"),
    CS_CORE      ("#EBF4FF", "#BBDEFB", "#1565C0", "CS Core"),
    ENGINEERING  ("#F5F0FF", "#D1C4E9", "#6A1B9A", "Engineering"),
    MATH_SCIENCE ("#FFFEF0", "#FFF9C4", "#F57F17", "Math / Science"),
    WEB          ("#FFF0F3", "#FFCDD2", "#AD1457", "Web"),
    GENERAL      ("#E6FAF8", "#B2EBF2", "#00695C", "General");

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

    /**
     * Classifies a course code into a category.
     * Handles formats: CS101, CS-101, CS 101, cs101, CS.101, CS_101
     */
    public static CourseCategory fromCode(String courseCode) {
        if (courseCode == null || courseCode.isBlank()) return GENERAL;

        // Normalize: uppercase + remove all non-alphanumeric chars
        String clean = courseCode.trim().toUpperCase()
                .replaceAll("[^A-Z0-9]", "");

        if (clean.isEmpty()) return GENERAL;

        // Split into prefix (letters) and number
        String prefix = clean.replaceAll("[0-9]", "");
        String numStr = clean.replaceAll("[^0-9]", "");

        int num = 0;
        try { num = Integer.parseInt(numStr); } catch (Exception ignored) {}

        return switch (prefix) {
            case "CS"                        -> classifyCs(num);
            case "IT"                        -> classifyIt(num);
            case "IS"                        -> classifyIs(num);
            case "AI"                        -> classifyAi(num);
            case "MATH", "MTH"               -> MATH_SCIENCE;
            case "PHYS", "PHY"               -> MATH_SCIENCE;
            case "CHEM", "CHE"               -> MATH_SCIENCE;
            case "STAT", "STA"               -> MATH_SCIENCE;
            case "EE", "ECE", "CE"           -> ENGINEERING;
            case "SE"                        -> ENGINEERING;
            case "NET", "NW"                 -> ENGINEERING;
            case "DB"                        -> CS_CORE;
            case "WEB"                       -> WEB;
            case "LANG", "ENG", "AR", "GER",
                 "FR",  "ESP"               -> GENERAL;
            default                          -> GENERAL;
        };
    }

    // ───────────────────────── CS ─────────────────────────
    private static CourseCategory classifyCs(int num) {
        if (num == 0) return CS_CORE;

        // ── Programming / Coding ──
        // CS101 Intro to Programming, CS102 OOP, CS105 Python,
        // CS201 Data Structures, CS202 Algorithms, CS301 Advanced Programming
        if (num == 101 || num == 102 || num == 105 || num == 108 ||
                num == 201 || num == 202 || num == 301)
            return PROGRAMMING;

        // ── Math & Science ──
        // CS103 Discrete Math, CS104 Calculus, CS111 Linear Algebra,
        // CS204 Probability & Stats, CS203 Numerical Analysis
        if (num == 103 || num == 104 || num == 111 || num == 112 ||
                num == 203 || num == 204)
            return MATH_SCIENCE;

        // ── Web ──
        // CS206 Web Dev, CS207 Advanced Web, CS306 Full Stack, CS406 Cloud/Web
        if (num == 206 || num == 207 || num == 306 || num == 406)
            return WEB;

        // ── Engineering / Systems ──
        // CS302 OS, CS303 Networks, CS304 Mobile, CS305 Distributed Systems,
        // CS320 Software Engineering, CS401 AI, CS402 ML, CS403 Computer Vision,
        // CS404 NLP, CS405 Robotics, CS410 Cybersecurity, CS420 Cloud Computing
        if (num == 205 || num == 302 || num == 303 || num == 304 ||
                num == 305 || num == 307 || num == 308 || num == 320 ||
                num == 401 || num == 402 || num == 403 || num == 404 ||
                num == 405 || num == 410 || num == 415 || num == 420 ||
                num == 430)
            return ENGINEERING;

        // ── General ──
        // CS106 English for CS, CS107 Academic Writing
        if (num == 106 || num == 107)
            return GENERAL;

        // ── Range-based fallback ──
        if (num >= 100 && num <= 199) return CS_CORE;       // Year 1 → Core
        if (num >= 200 && num <= 299) return PROGRAMMING;   // Year 2 → Programming
        if (num >= 300 && num <= 399) return ENGINEERING;   // Year 3 → Engineering
        if (num >= 400 && num <= 499) return ENGINEERING;   // Year 4 → Engineering

        return CS_CORE;
    }

    // ───────────────────────── IT ─────────────────────────
    private static CourseCategory classifyIt(int num) {
        if (num == 0) return CS_CORE;

        if (num == 101 || num == 102) return CS_CORE;         // Intro IT
        if (num == 201 || num == 202) return PROGRAMMING;     // Programming courses
        if (num == 301 || num == 302 || num == 401) return ENGINEERING; // Systems/Networks
        if (num == 305 || num == 405) return WEB;             // Web/Mobile

        if (num >= 100 && num <= 199) return CS_CORE;
        if (num >= 200 && num <= 299) return PROGRAMMING;
        if (num >= 300 && num <= 499) return ENGINEERING;

        return CS_CORE;
    }

    // ───────────────────────── IS ─────────────────────────
    private static CourseCategory classifyIs(int num) {
        if (num == 0) return CS_CORE;

        if (num == 101 || num == 102) return CS_CORE;
        if (num == 201 || num == 202) return PROGRAMMING;
        if (num == 301 || num == 401) return ENGINEERING;
        if (num == 305) return WEB;

        if (num >= 100 && num <= 199) return CS_CORE;
        if (num >= 200 && num <= 299) return PROGRAMMING;
        if (num >= 300 && num <= 499) return ENGINEERING;

        return ENGINEERING;
    }

    // ───────────────────────── AI ─────────────────────────
    private static CourseCategory classifyAi(int num) {
        if (num == 0) return CS_CORE;

        if (num == 101 || num == 102) return CS_CORE;         // Intro AI
        if (num == 201 || num == 202) return PROGRAMMING;     // AI Programming
        if (num >= 300) return ENGINEERING;                    // Advanced AI/ML

        return CS_CORE;
    }

    public static CourseCategory[] legendOrder() {
        return new CourseCategory[]{
                PROGRAMMING, CS_CORE, ENGINEERING, MATH_SCIENCE, WEB, GENERAL
        };
    }
}