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

/**
 * Immutable meal plan composed of ordered {@link MealSlot} entries.
 *
 * <p><b>Semantics:</b>
 * <ul>
 *   <li>The list of slots is copied defensively on construction; the instance is immutable.</li>
 *   <li>Slot ordering is preserved as provided by the strategy that produced the plan
 *       (typically day-major order, then meal type order).</li>
 *   <li>This class does not enforce a specific day/meal layout; the chosen {@code MealPlanStrategy}
 *       is responsible for producing the correct number and order of slots.</li>
 * </ul>
 *
 * <p><b>Null-safety:</b> {@link java.util.List#copyOf(java.util.Collection) List.copyOf} throws a {@link NullPointerException}
 * if {@code slots} is {@code null} or contains {@code null} elements.</p>
 *
 * @since 1.0
 */
public final class MealPlan {
    private final List<MealSlot> slots;

    /**
     * Creates an immutable plan from the given slots.
     * The provided list is defensively copied; subsequent external mutations
     * to the original list will not affect this instance.
     *
     * @param slots list of {@link MealSlot} (non-null, no null elements)
     * @throws NullPointerException if {@code slots} is null or contains nulls
     */
    public MealPlan(List<MealSlot> slots) {
        this.slots = List.copyOf(slots);
    }

    /**
     * Returns an unmodifiable view of this plan's slots.
     * The returned list is never {@code null} and preserves the original order.
     *
     * @return unmodifiable list of {@link MealSlot}
     */
    public List<MealSlot> slots() {
        return Collections.unmodifiableList(slots);
    }
}
