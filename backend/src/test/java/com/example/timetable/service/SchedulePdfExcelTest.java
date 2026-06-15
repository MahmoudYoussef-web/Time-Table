package com.example.timetable.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootTest
class SchedulePdfExcelTest {

    @Autowired
    private SchedulePdfService pdfService;

    @Autowired
    private ScheduleExcelService excelService;

    @Autowired
    private com.example.timetable.service.ScheduleService scheduleService;

    @Test
    void generateAllYearPdfsAndExcels() throws Exception {
        var summaries = scheduleService.findAll();
        if (summaries.isEmpty()) {
            System.out.println("SKIP: No schedules found in database");
            return;
        }
        var schedule = scheduleService.getScheduleById(summaries.get(0).id());
        System.out.println("Using schedule ID=" + schedule.getId());

        Path out = Paths.get("D:\\my projects\\time-table\\backend");
        Files.createDirectories(out);

        String[] levels = {"1", "2", "3", "4"};
        String[] levelNames = {"first-year", "second-year", "third-year", "fourth-year"};
        ColorTheme[] themes = ColorTheme.values();

        // PDF — one file per year per theme
        for (int yi = 0; yi < levels.length; yi++) {
            for (ColorTheme theme : themes) {
                byte[] pdf = pdfService.exportPdf(schedule, levels[yi], theme);
                String name = "schedule-" + levelNames[yi] + "-" + theme.name().toLowerCase() + ".pdf";
                Files.write(out.resolve(name), pdf);
                System.out.println("PDF: " + name + " (" + pdf.length + " bytes)");
            }
        }

        // Excel — one file per theme
        for (ColorTheme theme : themes) {
            byte[] xls = excelService.exportExcel(schedule, theme);
            String name = "schedule-all-years-" + theme.name().toLowerCase() + ".xlsx";
            Files.write(out.resolve(name), xls);
            System.out.println("XLSX: " + name + " (" + xls.length + " bytes)");
        }
    }
}