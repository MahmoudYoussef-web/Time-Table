package com.example.timetable.service;

import com.example.timetable.dto.response.*;
import com.example.timetable.mapper.WeeklyScheduleMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class ScheduleExcelService {

    public byte[] generateExcel(ScheduleDTO schedule) {

        try {

            WeeklyScheduleDTO weekly =
                    WeeklyScheduleMapper.toWeeklyTable(schedule);

            Workbook workbook = new XSSFWorkbook();

            Sheet sheet = workbook.createSheet("Schedule");

            int rowIndex = 0;

            Row header = sheet.createRow(rowIndex++);

            header.createCell(0).setCellValue("Day");
            header.createCell(1).setCellValue("Course");
            header.createCell(2).setCellValue("Instructor");
            header.createCell(3).setCellValue("Room");
            header.createCell(4).setCellValue("Time");

            for (DayScheduleDTO day : weekly.days()) {

                for (SlotDTO slot : day.slots()) {

                    if (slot.entry() == null) continue;

                    Row row = sheet.createRow(rowIndex++);

                    row.createCell(0).setCellValue(day.day());

                    row.createCell(1).setCellValue(
                            slot.entry().courseCode() +
                                    " - " +
                                    slot.entry().courseName()
                    );

                    row.createCell(2).setCellValue(
                            slot.entry().instructorName()
                    );

                    row.createCell(3).setCellValue(
                            slot.entry().roomNumber()
                    );

                    row.createCell(4).setCellValue(
                            slot.startTime() +
                                    " - " +
                                    slot.endTime()
                    );
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            workbook.write(out);
            workbook.close();

            return out.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException("Failed to generate Excel", e);

        }
    }
}