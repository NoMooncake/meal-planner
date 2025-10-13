/**
 * -----------------------------------------------------------------------------
 * File Name: IngredientTest.java
 * Project: meal-planner
 * Description:
 *      Placeholder class for ingredient tests.
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/12
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Placeholder class for ingredient tests. */
public class IngredientTest {

    @Test
    // Valid creation
    void createValidIngredient() {
        Ingredient egg = Ingredient.of("Egg", 2, Unit.PCS);
        assertEquals("egg", egg.name());       // normalized to lowercase
        assertEquals(2.0, egg.amount());
        assertEquals(Unit.PCS, egg.unit());
    }

    @Test
    // Null unit should fail
    void negativeAmountShouldFail() {
        assertThrows(IllegalArgumentException.class,
                () -> Ingredient.of("Milk", -1, Unit.ML));
    }

    @Test
    // Blank name should fail
    void blankNameShouldFail() {
        assertThrows(IllegalArgumentException.class,
                () -> Ingredient.of("   ", 1, Unit.G));
    }

    @Test
    // Identity tests
    void identityIgnoresAmountButRespectsUnit() {
        Ingredient a = Ingredient.of("  EGG ", 2, Unit.PCS);
        Ingredient b = Ingredient.of("egg", 5, Unit.PCS);
        Ingredient c = Ingredient.of("egg", 2, Unit.G);

        // same name+unit => equal (amount ignored by design)
        assertEquals(a, b);
        // different unit => not equal
        assertNotEquals(a, c);
    }
}