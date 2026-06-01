package com.example.timetable.scheduling.constraints;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConstraintViolation {

    private String constraintName;
    private Long sectionId;
    private String message;
}