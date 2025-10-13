/**
 * -----------------------------------------------------------------------------
 * File Name: MealPlannerService.java
 * Project: meal-planner
 * Description:
 *      Main service class for meal planning and shopping list generation.
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/12
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner;

import com.example.mealplanner.strategy.MealPlanStrategy;

import java.util.List;
import java.util.Objects;

public final class MealPlannerService {

    private final List<Recipe> catalog;
    private final MealPlanStrategy strategy;
    private final GroceryService grocery = new GroceryService();

    public MealPlannerService(List<Recipe> catalog, MealPlanStrategy strategy) {
        this.catalog = List.copyOf(Objects.requireNonNull(catalog, "catalog"));
        this.strategy = Objects.requireNonNull(strategy, "strategy");
        if (this.catalog.isEmpty()) throw new IllegalArgumentException("catalog must not be empty");
    }

    public MealPlannerService(RecipeCatalog catalog, MealPlanStrategy strategy) {
        this(Objects.requireNonNull(catalog, "catalog").all(), strategy);
    }

    // generate a meal plan
    public MealPlan plan(int days, MealType... mealsPerDay) {
        return strategy.generatePlan(days, mealsPerDay, catalog);
    }

    // generate a shopping list from a meal plan
    public ShoppingList buildShoppingList(int days, MealType... mealsPerDay) {
        MealPlan plan = plan(days, mealsPerDay);
        return grocery.buildFrom(plan);
    }

    // convenience method for common case of 2 meals/day (lunch + dinner)
    public MealPlan plan(int days, int mealsPerDay) {
        if (mealsPerDay <= 0) throw new IllegalArgumentException("mealsPerDay must be > 0");
        MealType[] types = new MealType[mealsPerDay];
        for (int i = 0; i < mealsPerDay; i++) {
            types[i] = (i == 0) ? MealType.LUNCH : MealType.DINNER; // 简单映射
        }
        return plan(days, types);
    }

    // convenience method for common case of 2 meals/day (lunch + dinner)
    public ShoppingList buildShoppingList(int days, int mealsPerDay) {
        return buildShoppingList(days, plan(days, mealsPerDay).slots()
                .stream().map(MealSlot::type).toArray(MealType[]::new));
    }
}