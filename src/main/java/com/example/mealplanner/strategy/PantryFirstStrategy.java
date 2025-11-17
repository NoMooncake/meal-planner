/**
 * -----------------------------------------------------------------------------
 * File Name: PantryFirstStrategy.java
 * Project: meal-planner
 * Description:
 *      Meal plan strategy that prefers recipes which consume more of the
 *      existing pantry and require less new purchases.
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/24
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */

package com.example.mealplanner.strategy;

import com.example.mealplanner.*;

import java.util.*;

/**
 * Strategy that tries to minimize how much you need to buy by
 * prioritizing recipes that can be mostly covered by the current pantry.
 *
 * <p>Design idea:</p>
 * <ul>
 *   <li>Work on a mutable copy of the pantry's internal stock (key: name|canonicalUnit).</li>
 *   <li>For each meal slot, score every recipe by the total "missing amount"
 *       (how much you have to buy in canonical units); choose the recipe with
 *       the smallest missing amount.</li>
 *   <li>After picking a recipe, "consume" the used ingredients from the working
 *       pantry stock so later slots see updated remaining amounts.</li>
 * </ul>
 *
 * <p>This is a {@link MealPlanStrategy} implementation; it does not mutate the
 * original {@link Pantry} instance.</p>
 *
 * @since 1.0
 */
public final class PantryFirstStrategy implements MealPlanStrategy {

    /** Mutable working stock: key = normalizedName + "|" + canonicalUnit, value = amount. */
    private final Map<String, Double> stock;

    /**
     * Creates a pantry-first strategy using a snapshot of the given pantry.
     *
     * @param pantry pantry whose stock will be used to guide recipe choices
     * @throws NullPointerException if {@code pantry} is {@code null}
     */
    public PantryFirstStrategy(Pantry pantry) {
        this(pantry, new Random());
    }

    /**
     * Creates a pantry-first strategy using a snapshot of the given pantry
     * and a {@link Random} instance (reserved for tie-breaking if needed).
     *
     * @param pantry pantry whose stock will be used to guide recipe choices
     * @param random random instance (non-null)
     * @throws NullPointerException if any argument is {@code null}
     */
    public PantryFirstStrategy(Pantry pantry, Random random) {
        Objects.requireNonNull(pantry, "pantry");
        Objects.requireNonNull(random, "random");
        // Make a mutable copy of the pantry internal map
        this.stock = new HashMap<>(pantry.snapshot());
    }

    /**
     * Generates a meal plan by greedily picking, for each slot, the recipe that
     * requires the least additional purchase based on the current working stock.
     *
     * @param days         number of days (&gt; 0)
     * @param mealsPerDay  meal types per day (non-empty)
     * @param catalog      available recipes (non-empty)
     * @return a {@link MealPlan} with {@code days * mealsPerDay.length} slots
     * @throws IllegalArgumentException if {@code days &lt;= 0}, meals/cat are empty
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
                Recipe best = chooseBestRecipe(catalog);
                slots.add(new MealSlot(d, type, best));
                consumeFromStock(best); // update working stock
            }
        }
        return new MealPlan(slots);
    }

    /**
     * Pick the recipe that minimizes total "missing" amount compared to current stock.
     */
    private Recipe chooseBestRecipe(List<Recipe> catalog) {
        Recipe best = null;
        double bestMissing = Double.POSITIVE_INFINITY;

        for (Recipe r : catalog) {
            double missing = totalMissingFor(r);
            if (missing < bestMissing) {
                bestMissing = missing;
                best = r;
            }
        }
        // catalog is non-empty by precondition, so best will not be null
        return best;
    }

    /**
     * Computes how much additional amount (in canonical units) is needed for a recipe.
     */
    private double totalMissingFor(Recipe recipe) {
        double missing = 0.0;
        for (Ingredient ing : recipe.ingredients()) {
            // canonicalize units to match pantry stock keys
            Unit cu = Units.canonical(ing.unit());
            double need = Units.toCanonical(ing.amount(), ing.unit());
            String key = ing.name() + "|" + cu;
            double have = stock.getOrDefault(key, 0.0);
            double diff = need - have;
            if (diff > 0) {
                missing += diff;
            }
        }
        return missing;
    }

    /**
     * Applies the consumption of ingredients for the chosen recipe to the working stock.
     */
    private void consumeFromStock(Recipe recipe) {
        for (Ingredient ing : recipe.ingredients()) {
            Unit cu = Units.canonical(ing.unit());
            double need = Units.toCanonical(ing.amount(), ing.unit());
            String key = ing.name() + "|" + cu;
            double have = stock.getOrDefault(key, 0.0);
            double newHave = have - need;
            if (newHave <= 0) {
                stock.remove(key);
            } else {
                stock.put(key, newHave);
            }
        }
    }
}
