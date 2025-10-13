/**
 * -----------------------------------------------------------------------------
 * File Name: MealPlannerServiceTest.java
 * Project: meal-planner
 * Description:
 *      Unit tests for {@link MealPlannerService}.
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/12
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner;

import com.example.mealplanner.strategy.RandomStrategy;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class MealPlannerServiceTest {

    @Test
    void buildShoppingListFromServiceWorks() {
        var catalog = RecipeCatalog.samples();
        var service = new MealPlannerService(catalog, new RandomStrategy(new Random(123)));

        ShoppingList list = service.buildShoppingList(/*days*/3, /*mealsPerDay*/2);

        assertFalse(list.items().isEmpty());

        var map = list.items().stream()
                .collect(java.util.stream.Collectors.toMap(
                        i -> i.name() + "|" + i.unit(), ShoppingListItem::totalAmount));

        // multiple recipes use milk, so it should be aggregated
        assertTrue(map.keySet().stream().anyMatch(k -> k.equals("milk|ML")));
    }

    @Test
    void invalidArgsThrow() {
        var catalog = RecipeCatalog.samples();
        var service = new MealPlannerService(catalog, new RandomStrategy(new Random(1)));

        assertThrows(IllegalArgumentException.class, () -> service.buildShoppingList(0, 2));
        assertThrows(IllegalArgumentException.class, () -> service.buildShoppingList(2, 0));
    }
}