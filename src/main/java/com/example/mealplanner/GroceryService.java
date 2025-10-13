/**
 * -----------------------------------------------------------------------------
 * File Name: GroceryService.java
 * Project: meal-planner
 * Description:
 *      Service for generating a shopping list from a meal plan.
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/12
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner;

import java.util.ArrayList;
import java.util.List;

public class GroceryService {

    /** Build a shopping list from a meal plan. */
    public ShoppingList buildFrom(MealPlan plan) {
        ShoppingListBuilder builder = new ShoppingListBuilder();
        for (MealSlot slot : plan.slots()) {
            builder.addRecipe(slot.recipe());
        }
        return builder.build();
    }

    public ShoppingList buildFrom(MealPlan plan, Pantry pantry) {
        // First, get the full shopping list needed for the meal plan
        ShoppingList need = buildFrom(plan);

        List<ShoppingListItem> remaining = new ArrayList<>();
        for (ShoppingListItem item : need.items()) {
            double have = pantry.amountOf(item.name(), item.unit());
            double buy = item.totalAmount() - have;
            if (buy > 0.0000001) { // consider floating point precision
                remaining.add(new ShoppingListItem(item.name(), item.unit(), buy));
            }
        }
        return new ShoppingList(remaining);
    }
}