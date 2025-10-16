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

/**
 * A single slot in a {@link MealPlan}, identified by its zero-based day index and
 * {@link MealType}, and holding the chosen {@link Recipe}.
 *
 * <p><b>Semantics:</b>
 * <ul>
 *   <li>{@code dayIndex} is zero-based in range {@code [0, days-1]} as produced by the strategy.</li>
 *   <li>Instances are immutable; fields are set in the constructor and never change.</li>
 *   <li>No ordering guarantees are enforced here; the enclosing {@link MealPlan} preserves order.</li>
 * </ul>
 *
 * @since 1.0
 */
public final class MealSlot {
    private final int dayIndex;       // 从 0 开始：0..(days-1)
    private final MealType type;
    private final Recipe recipe;

    /**
     * Creates a new immutable meal slot.
     *
     * @param dayIndex zero-based day index (must be {@code >= 0})
     * @param type     non-null {@link MealType}
     * @param recipe   non-null {@link Recipe} assigned to this slot
     * @throws IllegalArgumentException if {@code dayIndex < 0}
     * @throws NullPointerException     if {@code type} or {@code recipe} is {@code null}
     */
    public MealSlot(int dayIndex, MealType type, Recipe recipe) {
        if (dayIndex < 0) throw new IllegalArgumentException("dayIndex >= 0");
        this.dayIndex = dayIndex;
        this.type = Objects.requireNonNull(type);
        this.recipe = Objects.requireNonNull(recipe);
    }

    /** @return zero-based day index */
    public int dayIndex() { return dayIndex; }

    /** @return the {@link MealType} for this slot */
    public MealType type() { return type; }

    /** @return the assigned {@link Recipe} for this slot */
    public Recipe recipe() { return recipe; }
}