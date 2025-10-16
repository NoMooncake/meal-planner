/**
 * -----------------------------------------------------------------------------
 * File Name: UnitsConversionTest.java
 * Project: meal-planner
 * Description:
 * [Add brief description here]
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/16
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UnitsConversionTest {

    @Test
    void kgToG_and_LToML() {
        assertEquals(Units.Family.MASS, Units.family(Unit.KG));
        assertEquals(Units.Family.VOLUME, Units.family(Unit.L));
        assertEquals(Unit.G, Units.canonical(Unit.KG));
        assertEquals(Unit.ML, Units.canonical(Unit.L));
        assertEquals(1000.0, Units.toCanonical(1.0, Unit.KG), 1e-9);
        assertEquals(500.0, Units.toCanonical(0.5, Unit.L), 1e-9);
    }

    @Test
    void pantry_addMergesCanonical() {
        Pantry p = new Pantry()
                .add("milk", 0.5, Unit.L)
                .add("milk", 200, Unit.ML);
        assertEquals(700.0, p.snapshot().get("milk|ML"), 1e-9);
    }
}