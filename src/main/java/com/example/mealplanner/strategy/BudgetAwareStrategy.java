/**
 * -----------------------------------------------------------------------------
 * File Name: BudgetAwareStrategy.java
 * Project: meal-planner
 * Description:
 *      Meal plan strategy that tries to stay within a total budget using a
 *      {@link com.example.mealplanner.PriceBook}.
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/22
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */

package com.example.mealplanner.strategy;

import com.example.mealplanner.*;

import java.util.*;

/**
 * {@link MealPlanStrategy} that tries to choose recipes whose total estimated cost
 * (according to a {@link PriceBook}) stays within a global budget.
 *
 * <p>For each meal slot this strategy prefers <em>more expensive</em> recipes that still
 * fit into the remaining budget; if none fit, it falls back to the cheapest recipe
 * so that a plan is always produced.</p>
 *
 * @since 1.0
 */
public final class BudgetAwareStrategy implements MealPlanStrategy {

    private final PriceBook priceBook;
    private final double maxTotalCost;

    /**
     * Creates a budget-aware strategy.
     *
     * @param priceBook    price book used to estimate recipe costs
     * @param maxTotalCost total budget for the whole plan (must be &gt; 0)
     */
    public BudgetAwareStrategy(PriceBook priceBook, double maxTotalCost) {
        this.priceBook = Objects.requireNonNull(priceBook, "priceBook");
        if (maxTotalCost <= 0) {
            throw new IllegalArgumentException("maxTotalCost must be > 0");
        }
        this.maxTotalCost = maxTotalCost;
    }

    @Override
    public MealPlan generatePlan(int days, MealType[] mealsPerDay, List<Recipe> catalog) {
        if (catalog == null || catalog.isEmpty()) {
            throw new IllegalArgumentException("catalog must not be empty");
        }
        if (days <= 0) {
            throw new IllegalArgumentException("days must be > 0");
        }
        if (mealsPerDay == null || mealsPerDay.length == 0) {
            throw new IllegalArgumentException("mealsPerDay must not be empty");
        }

        // Pre-compute cost per recipe
        List<Recipe> recipes = new ArrayList<>(catalog);
        Map<Recipe, Double> costs = new HashMap<>();
        for (Recipe r : recipes) {
            double c = priceBook.estimateCost(r);
            // If no ingredient had a price, treat as a modest cost so it is still usable.
            if (c == 0.0) {
                c = 1.0;
            }
            costs.put(r, c);
        }

        // Sort by descending cost: "most expensive first"
        recipes.sort(Comparator.comparingDouble(costs::get).reversed());
        Recipe cheapest = recipes.get(recipes.size() - 1);

        double used = 0.0;
        List<MealSlot> slots = new ArrayList<>(days * mealsPerDay.length);

        for (int d = 0; d < days; d++) {
            for (MealType type : mealsPerDay) {
                Recipe chosen = null;
                double remaining = maxTotalCost - used;

                if (remaining > 0) {
                    for (Recipe candidate : recipes) {
                        double cost = costs.get(candidate);
                        if (cost <= remaining + 1e-9) {
                            chosen = candidate;
                            break; // pick the most expensive that still fits
                        }
                    }
                }

                if (chosen == null) {
                    // nothing fits into the remaining budget → fallback to cheapest
                    chosen = cheapest;
                }

                used += costs.get(chosen);
                slots.add(new MealSlot(d, type, chosen));
            }
        }

        return new MealPlan(slots);
    }
}
