package com.example.timetable.service;

import com.example.timetable.dto.response.ScheduleDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootTest
class SchedulePngImageTest {

    @Autowired
    private SchedulePngService pngService;

    @Autowired
    private com.example.timetable.service.ScheduleService scheduleService;

    @Test
    void generateAllYearPngs() throws Exception {
        var summaries = scheduleService.findAll();
        if (summaries.isEmpty()) {
            System.out.println("SKIP: No schedules found in database");
            return;
        }
        Long latestId = summaries.get(0).id();
        ScheduleDTO schedule = scheduleService.getScheduleById(latestId);
        System.out.println("Using schedule ID=" + latestId);

        Path outputDir = Paths.get("D:\\my projects\\time-table\\backend");
        Files.createDirectories(outputDir);

        String[] yearLevels = {"1", "2", "3", "4"};
        String[] yearNames  = {"first-year", "second-year", "third-year", "fourth-year"};

        for (int yi = 0; yi < yearLevels.length; yi++) {
            for (ColorTheme theme : ColorTheme.values()) {
                byte[] png = pngService.generatePng(schedule, yearLevels[yi], theme);

                BufferedImage img = ImageIO.read(new ByteArrayInputStream(png));
                System.out.println(yearNames[yi] + " " + theme.name().toLowerCase()
                        + ": " + img.getWidth() + "x" + img.getHeight());

                String fileName = "schedule-" + yearNames[yi] + "-" + theme.name().toLowerCase() + ".png";
                Files.write(outputDir.resolve(fileName), png);
                System.out.println("Generated: " + fileName + " (" + png.length + " bytes)");
            }
        }
    }
}
