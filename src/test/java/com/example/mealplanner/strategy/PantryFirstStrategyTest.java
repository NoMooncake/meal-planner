/**
 * -----------------------------------------------------------------------------
 * File Name: PantryFirstStrategyTest.java
 * Project: meal-planner
 * Description:
 * [Add brief description here]
 * <p>
 * Author: Yue Wu
 * Date: 2025/11/17
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner.strategy;

import com.example.mealplanner.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic tests for {@link PantryFirstStrategy}.
 */
class PantryFirstStrategyTest {

    @Test
    void prefersRecipeThatMatchesPantry() {
        // Pantry: we already have chicken and rice
        Pantry pantry = new Pantry()
                .add("chicken", 300, Unit.G)
                .add("rice", 500, Unit.G);

        // Recipe A: uses things we have -> should be cheap / missing=0 (first two slots)
        Recipe chickenRice = Recipe.of("Chicken Rice Bowl",
                Ingredient.of("chicken", 150, Unit.G),
                Ingredient.of("rice", 100, Unit.G));

        // Recipe B: uses completely new ingredients -> always missing
        Recipe avocadoToast = Recipe.of("Avocado Toast",
                Ingredient.of("bread", 50, Unit.G),
                Ingredient.of("avocado", 1, Unit.PCS));

        PantryFirstStrategy strat = new PantryFirstStrategy(pantry);

        MealPlan plan = strat.generatePlan(
                2, // 2 days
                new MealType[]{MealType.LUNCH}, // 1 meal per day
                List.of(chickenRice, avocadoToast)
        );

        // Should pick Chicken Rice Bowl for both slots because pantry covers it
        assertEquals(2, plan.slots().size());
        for (MealSlot slot : plan.slots()) {
            assertEquals("Chicken Rice Bowl", slot.recipe().name());
        }
    }
}
