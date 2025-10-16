/**
 * -----------------------------------------------------------------------------
 * File Name: Units.java
 * Project: meal-planner
 * Description:
 * [Add brief description here]
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/15
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner;

/**
 * Unit conversion helpers. We keep a single canonical unit per family:
 * MASS → G, VOLUME → ML, COUNT → PCS.
 */
public final class Units {

    /** Unit families. */
    public enum Family { COUNT, MASS, VOLUME }

    private Units() {}

    /** Family of a unit. */
    public static Family family(Unit u) {
        return switch (u) {
            case PCS -> Family.COUNT;
            case G, KG -> Family.MASS;
            case ML, L -> Family.VOLUME;
        };
    }

    /** Canonical unit of a given unit's family. */
    public static Unit canonical(Unit u) {
        return switch (family(u)) {
            case COUNT -> Unit.PCS;
            case MASS  -> Unit.G;
            case VOLUME-> Unit.ML;
        };
    }

    /** Convert an amount from {@code from} unit to the canonical unit of its family. */
    public static double toCanonical(double amount, Unit from) {
        return switch (from) {
            case KG -> amount * 1000.0;
            case L  -> amount * 1000.0;
            default -> amount; // PCS, G, ML already canonical
        };
    }

    /** Whether two units are convertible (i.e., in the same family). */
    public static boolean convertible(Unit a, Unit b) {
        return family(a) == family(b);
    }
}