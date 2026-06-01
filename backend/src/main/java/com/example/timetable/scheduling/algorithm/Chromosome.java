package com.example.timetable.scheduling.algorithm;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Chromosome {

    /* ===================== GENES ===================== */

    private List<Gene> genes;

    /* ===================== FITNESS ===================== */

    private double fitness;

    /* ===================== FITNESS BREAKDOWN ===================== */

    private int hardViolations;
    private int softViolations;

    public Chromosome(List<Gene> genes) {
        this.genes = genes;
        this.fitness = 0.0;
        this.hardViolations = 0;
        this.softViolations = 0;
    }

    /**
     * Creates a deep copy of the chromosome.
     * Fitness values are NOT copied intentionally,
     * as copied chromosomes must be re-evaluated.
     */
    public Chromosome copy() {
        List<Gene> newGenes = new ArrayList<>();
        for (Gene gene : genes) {
            newGenes.add(gene.copy());
        }
        return new Chromosome(newGenes);
    }
}
