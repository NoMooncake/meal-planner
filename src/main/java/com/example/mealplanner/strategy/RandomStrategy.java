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

public class RandomStrategy implements MealPlanStrategy {

    private final Random random;

    public RandomStrategy() { this(new Random()); }
    public RandomStrategy(Random random) { this.random = Objects.requireNonNull(random); }

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
