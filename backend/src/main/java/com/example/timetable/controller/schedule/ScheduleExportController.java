package com.example.timetable.controller.schedule;

import com.example.timetable.dto.response.ScheduleDTO;
import com.example.timetable.service.ColorTheme;
import com.example.timetable.service.ScheduleExcelService;
import com.example.timetable.service.SchedulePdfService;
import com.example.timetable.service.SchedulePngService;
import com.example.timetable.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ScheduleExportController {

    private final ScheduleService scheduleService;
    private final SchedulePdfService schedulePdfService;
    private final SchedulePngService schedulePngService;
    private final ScheduleExcelService scheduleExcelService;

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam Long scheduleId,
            @RequestParam(required = false) String year,
            @RequestParam(defaultValue = "NAVY") String theme
    ) {
        ScheduleDTO schedule = scheduleService.getScheduleById(scheduleId);
        ColorTheme colorTheme = ColorTheme.fromString(theme);

        byte[] pdf;
        if (year != null && !year.isBlank()) {
            pdf = schedulePdfService.exportPdf(schedule, year, colorTheme);
        } else {
            pdf = schedulePdfService.exportPdf(schedule, colorTheme);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=schedule-" + scheduleId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")
    @GetMapping("/png")
    public ResponseEntity<byte[]> exportPng(
            @RequestParam Long scheduleId,
            @RequestParam(defaultValue = "NAVY") String theme,
            @RequestParam(required = false) String year
    ) {
        ScheduleDTO schedule = scheduleService.getScheduleById(scheduleId);
        ColorTheme colorTheme = ColorTheme.fromString(theme);

        String yearLevel = year != null && !year.isBlank() ? year : "ALL";

        byte[] png = schedulePngService.generatePng(schedule, yearLevel, colorTheme);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=schedule-" + scheduleId + ".png")
                .contentType(MediaType.IMAGE_PNG)
                .body(png);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")
    @GetMapping("/excel")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam Long scheduleId,
            @RequestParam(defaultValue = "NAVY") String theme
    ) {
        ScheduleDTO schedule = scheduleService.getScheduleById(scheduleId);

        ColorTheme excelTheme = ColorTheme.fromString(theme);
        byte[] excel = scheduleExcelService.exportExcel(schedule, excelTheme);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=schedule-" + scheduleId + ".xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}
