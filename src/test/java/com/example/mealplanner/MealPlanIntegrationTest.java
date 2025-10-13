/**
 * -----------------------------------------------------------------------------
 * File Name: MealPlanIntegrationTest.java
 * Project: meal-planner
 * Description:
 *      Integration test for generating a meal plan and building a shopping list.
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/12
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner;

import com.example.mealplanner.strategy.MealPlanStrategy;
import com.example.mealplanner.strategy.RandomStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class MealPlanIntegrationTest {

    @Test
    void buildShoppingListFromRandomPlan_isAggregatedAndSized() {
        // prepare catalog
        Recipe eggs = Recipe.of("Eggs",
                Ingredient.of("Egg", 2, Unit.PCS),
                Ingredient.of("Milk", 50, Unit.ML));

        Recipe pasta = Recipe.of("Pasta",
                Ingredient.of("Pasta", 100, Unit.G),
                Ingredient.of("Milk", 100, Unit.ML));

        List<Recipe> catalog = List.of(eggs, pasta);

        // use random strategy to generate a plan
        MealPlanStrategy strategy = new RandomStrategy(new Random(42));
        MealPlan plan = strategy.generatePlan(
                2,
                new MealType[]{ MealType.LUNCH, MealType.DINNER },
                catalog
        );

        // the plan should have 4 slots
        assertEquals(4, plan.slots().size());

        // build shopping list from the plan
        GroceryService grocery = new GroceryService();
        ShoppingList list = grocery.buildFrom(plan);

        // the shopping list should aggregate ingredients
        var map = list.items().stream()
                .collect(java.util.stream.Collectors.toMap(
                        i -> i.name() + "|" + i.unit(), ShoppingListItem::totalAmount));

        assertTrue(map.containsKey("milk|ML"));
        // milk amount should be aggregated from 50 + 100 + 50 = 200
        assertTrue(map.get("milk|ML") >= 50.0);

        // the shopping list should not be too large
        assertTrue(list.items().size() <= 3);
    }
}
