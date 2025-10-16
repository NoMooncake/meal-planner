/**
 * -----------------------------------------------------------------------------
 * File Name: RandomStrategy.java
 * Project: meal-planner
 * Description:
 *    Implementation of {@link MealPlanStrategy} that generates random meal plans.
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/12
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner.strategy;

import com.example.mealplanner.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Objects;

/**
 * A {@link MealPlanStrategy} that fills each requested meal slot with a recipe
 * chosen uniformly at random from the provided catalog.
 *
 * <p><b>Determinism for testing:</b> pass a {@link Random} with a fixed seed to the
 * constructor so the same inputs produce the same {@link MealPlan}. The no-arg
 * constructor uses a default {@code new Random()} and is therefore non-deterministic.</p>
 *
 * <p><b>Behavior:</b>
 * <ul>
 *   <li>Recipe reuse is allowed (the same recipe may appear multiple times).</li>
 *   <li>Inputs are treated as read-only; the catalog is not modified.</li>
 *   <li>The resulting plan contains exactly {@code days × mealsPerDay.length} slots,
 *       preserving the order of {@code mealsPerDay} for each day.</li>
 * </ul>
 *
 * <p>This class is a concrete Strategy in the Strategy pattern.</p>
 *
 * @since 1.0
 */
public class RandomStrategy implements MealPlanStrategy {

    /** Source of randomness; may be seeded by clients for reproducible tests. */
    private final Random random;

    /**
     * Creates a non-deterministic strategy using {@code new Random()}.
     * Prefer {@link #RandomStrategy(Random)} with a fixed seed in tests.
     */
    public RandomStrategy() {
        this(new Random());
    }
    /**
     * Creates a strategy that draws from the given {@link Random} instance.
     * Supplying a seeded {@code Random} makes the strategy deterministic.
     *
     * @param random non-null random source
     * @throws NullPointerException if {@code random} is {@code null}
     */
    public RandomStrategy(Random random) {
        this.random = Objects.requireNonNull(random);
    }

    /**
     * Generates a meal plan by randomly picking a recipe for every day/meal slot.
     *
     * @param days        number of days to plan; must be {@code > 0}
     * @param mealsPerDay ordered array of {@link MealType} per day; must be non-empty
     * @param catalog     candidate {@link Recipe} list; must be non-empty
     * @return a {@link MealPlan} with {@code days × mealsPerDay.length} slots, in order
     *
     * @throws IllegalArgumentException if {@code days <= 0},
     *                                  or {@code mealsPerDay} is null/empty,
     *                                  or {@code catalog} is null/empty
     */
    @Override
    public MealPlan generatePlan(int days, MealType[] mealsPerDay, List<Recipe> catalog) {
        if (catalog == null || catalog.isEmpty())
            throw new IllegalArgumentException("catalog must not be empty");
        if (days <= 0)
            throw new IllegalArgumentException("days must be > 0");
        if (mealsPerDay == null || mealsPerDay.length == 0)
            throw new IllegalArgumentException("mealsPerDay must not be empty");

        List<MealSlot> slots = new ArrayList<>(days * mealsPerDay.length);
        for (int d = 0; d < days; d++) {
            for (MealType type : mealsPerDay) {
                Recipe pick = catalog.get(random.nextInt(catalog.size()));
                slots.add(new MealSlot(d, type, pick));
            }
        }
        return new MealPlan(slots);
    }
}
