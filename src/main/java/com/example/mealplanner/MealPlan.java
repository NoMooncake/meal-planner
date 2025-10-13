/**
 * -----------------------------------------------------------------------------
 * File Name: MealPlan.java
 * Project: meal-planner
 * Description:
 *    Immutable meal plan.
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/12
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner;

import java.util.Collections;
import java.util.List;

/** Immutable meal plan. */
public final class MealPlan {
    private final List<MealSlot> slots;

    public MealPlan(List<MealSlot> slots) {
        this.slots = List.copyOf(slots);
    }

    public List<MealSlot> slots() {
        return Collections.unmodifiableList(slots);
    }
}
