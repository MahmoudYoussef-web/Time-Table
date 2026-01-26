package com.example.timetable.scheduling.constraints;

/**
 * Marker interface for hard constraints.
 */
public interface HardConstraint extends Constraint {

    @Override
    default ConstraintType getType() {
        return ConstraintType.HARD;
    }
}
