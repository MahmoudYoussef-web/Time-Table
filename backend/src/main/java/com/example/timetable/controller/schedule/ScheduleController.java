package com.example.timetable.controller.schedule;

import com.example.timetable.entity.ScheduleGenerationJob;
import com.example.timetable.scheduling.constraints.ConstraintViolation;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final SchedulePdfService schedulePdfService;
    private final ScheduleExcelService scheduleExcelService;
    private final SchedulePngService schedulePngService;


    // ===============================
    // Generate Schedule (Async)
    // ===============================
    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")
    @PostMapping("/generate/{semesterId}")
    public ResponseEntity<UUID> generateAsync(
            @PathVariable Long semesterId
    ) {

        UUID jobId = scheduleService.generateScheduleAsync(semesterId);

        return ResponseEntity.ok(jobId);
    }


    // ===============================
    // Get Job Status
    // ===============================
    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<ScheduleGenerationJob> getJob(
            @PathVariable UUID jobId
    ) {

        return ResponseEntity.ok(
                scheduleService.getJob(jobId)
        );
    }


    // ===============================
    // Get Schedule Conflicts
    // ===============================
    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")
    @GetMapping("/{id}/conflicts")
    public ResponseEntity<List<ConstraintViolation>> conflicts(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                scheduleService.getConflicts(id)
        );
    }


    // ===============================
    // Export Schedule PDF
    // ===============================
    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @PathVariable Long id,
            @RequestParam(defaultValue = "NAVY") String theme
    ) {
        var schedule = scheduleService.getScheduleById(id);
        ColorTheme colorTheme = ColorTheme.fromString(theme);

        byte[] pdf = schedulePdfService.exportPdf(schedule, colorTheme);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=schedule-" + id + ".pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")
    @GetMapping("/{id}/excel")
    public ResponseEntity<byte[]> exportExcel(
            @PathVariable Long id,
            @RequestParam(defaultValue = "NAVY") String theme
    ) {
        var schedule = scheduleService.getScheduleById(id);
        ColorTheme colorTheme = ColorTheme.fromString(theme);

        byte[] excel = scheduleExcelService.exportExcel(schedule, colorTheme);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=schedule.xlsx"
                )
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                )
                .body(excel);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")
    @GetMapping("/{id}/year/{year}/pdf")
    public ResponseEntity<byte[]> exportYearPdf(
            @PathVariable Long id,
            @PathVariable String year,
            @RequestParam(defaultValue = "NAVY") String theme
    ) {
        var schedule = scheduleService.getScheduleById(id);
        ColorTheme colorTheme = ColorTheme.fromString(theme);

        byte[] pdf = schedulePdfService.exportPdf(schedule, year, colorTheme);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=schedule-" + id + "-" + year + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")
    @GetMapping("/{id}/year/{year}/excel")
    public ResponseEntity<byte[]> exportYearExcel(
            @PathVariable Long id,
            @PathVariable String year,
            @RequestParam(defaultValue = "NAVY") String theme
    ) {
        var schedule = scheduleService.getScheduleById(id);
        ColorTheme colorTheme = ColorTheme.fromString(theme);

        byte[] excel = scheduleExcelService.exportExcel(schedule, year, colorTheme);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=schedule-" + id + "-" + year + ".xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")
    @GetMapping("/{id}/year/{year}/png")
    public ResponseEntity<byte[]> exportYearPng(
            @PathVariable Long id,
            @PathVariable String year,
            @RequestParam(defaultValue = "NAVY") String theme
    ) {
        var schedule = scheduleService.getScheduleById(id);
        ColorTheme colorTheme = ColorTheme.fromString(theme);

        byte[] png = schedulePngService.generatePng(schedule, year, colorTheme);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=schedule-" + id + "-" + year + ".png")
                .contentType(MediaType.IMAGE_PNG)
                .body(png);
    }
}