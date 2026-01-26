package com.example.timetable.scheduling.constraints;

/**
 * Marker interface for soft constraints.
 */
public interface SoftConstraint extends Constraint {

    @Override
    default ConstraintType getType() {
        return ConstraintType.SOFT;
    }
}
