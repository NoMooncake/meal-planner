/**
 * -----------------------------------------------------------------------------
 * File Name: MealTypesTest.java
 * Project: meal-planner
 * Description:
 *      Service class for meal planning and shopping list generation.
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/12
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner;

import com.example.mealplanner.strategy.RandomStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class MealTypesTest {

    @Test
    void planRespectsDaysAndMealTypes() {
        var catalog = RecipeCatalog.samples().all();
        var service = new MealPlannerService(catalog, new RandomStrategy(new Random(42)));

        MealPlan plan = service.plan(3, MealType.BREAKFAST, MealType.DINNER); // 每天两餐
        assertEquals(3 * 2, plan.slots().size());

        var types = plan.slots().stream().map(MealSlot::type).toList();
        assertTrue(types.contains(MealType.BREAKFAST));
        assertTrue(types.contains(MealType.DINNER));
    }
}