/**
 * -----------------------------------------------------------------------------
 * File Name: MealSlot.java
 * Project: meal-planner
 * Description:
 *      A slot in the meal plan, consisting of a day index, meal type, and chosen recipe.
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/12
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner;

import java.util.Objects;

/** A slot in the plan: day index + meal type + chosen recipe. */
public final class MealSlot {
    private final int dayIndex;       // 从 0 开始：0..(days-1)
    private final MealType type;
    private final Recipe recipe;

    public MealSlot(int dayIndex, MealType type, Recipe recipe) {
        if (dayIndex < 0) throw new IllegalArgumentException("dayIndex >= 0");
        this.dayIndex = dayIndex;
        this.type = Objects.requireNonNull(type);
        this.recipe = Objects.requireNonNull(recipe);
    }

    public int dayIndex() { return dayIndex; }
    public MealType type() { return type; }
    public Recipe recipe() { return recipe; }
}