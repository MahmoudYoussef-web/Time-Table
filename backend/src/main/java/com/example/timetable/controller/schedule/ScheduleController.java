package com.example.timetable.controller.schedule;

import com.example.timetable.dto.response.ScheduleDTO;
import com.example.timetable.entity.ScheduleGenerationJob;
import com.example.timetable.entity.enums.YearLevel;
import com.example.timetable.scheduling.constraints.ConstraintViolation;
import com.example.timetable.service.ColorTheme;
import com.example.timetable.service.ScheduleExcelService;
import com.example.timetable.service.SchedulePdfService;
import com.example.timetable.service.SchedulePngService;
import com.example.timetable.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
    // List All Schedules
    // ===============================
    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")
    @GetMapping
    public ResponseEntity<List<com.example.timetable.dto.response.ScheduleSummaryResponse>> findAll() {
        return ResponseEntity.ok(scheduleService.findAll());
    }

    // ===============================
    // Get Schedule By ID
    // ===============================
    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER','INSTRUCTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<ScheduleDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.getScheduleById(id));
    }

    // ===============================
    // Generate Schedule (Async)
    // ===============================
    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")
    @PostMapping("/generate/{semesterId}")
    public ResponseEntity<UUID> generateAsync(
            @PathVariable Long semesterId
    ) {

        UUID jobId = scheduleService.generateScheduleAsync(semesterId);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(jobId);
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
            @RequestParam(required = false) YearLevel year,
            @RequestParam(defaultValue = "NAVY") String theme
    ) {

        var schedule = scheduleService.getScheduleById(id);
        ColorTheme colorTheme = ColorTheme.fromString(theme);

        byte[] pdf;
        if (year != null) {
            pdf = schedulePdfService.exportPdf(schedule, year.getDisplayName(), colorTheme);
        } else {
            pdf = schedulePdfService.exportPdf(schedule, colorTheme);
        }

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=schedule-" + id + ".pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // ===============================
    // Export Schedule PNG
    // ===============================
    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")
    @GetMapping("/{id}/png")
    public ResponseEntity<byte[]> exportPng(
            @PathVariable Long id,
            @RequestParam(defaultValue = "NAVY") String theme,
            @RequestParam(required = false) YearLevel year
    ) {

        var schedule = scheduleService.getScheduleById(id);
        ColorTheme colorTheme = ColorTheme.fromString(theme);

        String yearLevel = year != null ? year.getDisplayName() : "ALL";

        byte[] png = schedulePngService.generatePng(schedule, yearLevel, colorTheme);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=schedule-" + id + ".png"
                )
                .contentType(MediaType.IMAGE_PNG)
                .body(png);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")
    @GetMapping("/{id}/excel")
    public ResponseEntity<byte[]> exportExcel(
            @PathVariable Long id,
            @RequestParam(defaultValue = "NAVY") String theme
    ) {

        var schedule = scheduleService.getScheduleById(id);
        ColorTheme excelTheme = ColorTheme.fromString(theme);

        byte[] excel =
                scheduleExcelService.exportExcel(schedule, excelTheme);

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
}