//package com.example.timetable.scheduling.constraints.soft;
//
//import com.example.timetable.scheduling.algorithm.Chromosome;
//import java.util.List;
//
//public class SoftConstraintEvaluator {
//
//    private final List<SoftConstraint> constraints;
//
//    public SoftConstraintEvaluator(List<SoftConstraint> constraints) {
//        this.constraints = constraints;
//    }
//
//    public double evaluate(Chromosome chromosome) {
//
//        double weightedSum = 0;
//        double totalWeight = 0;
//
//        for (SoftConstraint constraint : constraints) {
//
//            double score = constraint.evaluate(chromosome);
//
//            weightedSum += score * constraint.weight();
//            totalWeight += constraint.weight();
//        }
//
//        if (totalWeight == 0) return 1.0;
//
//        return weightedSum / totalWeight;
//    }
//}