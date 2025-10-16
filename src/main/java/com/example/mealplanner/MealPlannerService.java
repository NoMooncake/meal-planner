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

/**
 * Facade service that orchestrates meal planning and shopping list generation.
 *
 * <p><b>Responsibilities:</b>
 * <ul>
 *   <li>Hold the read-only recipe catalog and the selected {@link MealPlanStrategy}.</li>
 *   <li>Delegate plan creation to the strategy.</li>
 *   <li>Build shopping lists (with current MVP rules) via {@link GroceryService}.</li>
 * </ul>
 *
 * <p><b>Immutability:</b> the catalog is defensively copied on construction; this service
 * does not mutate inputs.</p>
 *
 * @since 1.0
 */
public final class MealPlannerService {

    private final List<Recipe> catalog;
    private final MealPlanStrategy strategy;
    private final GroceryService grocery = new GroceryService();

    /**
     * Creates a service over a concrete recipe catalog and strategy.
     *
     * @param catalog  non-null, non-empty list of {@link Recipe}; copied defensively
     * @param strategy non-null planning strategy to use
     * @throws NullPointerException     if {@code catalog} or {@code strategy} is {@code null}
     * @throws IllegalArgumentException if {@code catalog} is empty
     */
    public MealPlannerService(List<Recipe> catalog, MealPlanStrategy strategy) {
        this.catalog = List.copyOf(Objects.requireNonNull(catalog, "catalog"));
        this.strategy = Objects.requireNonNull(strategy, "strategy");
        if (this.catalog.isEmpty()) throw new IllegalArgumentException("catalog must not be empty");
    }

    /**
     * Convenience constructor that extracts the underlying list of recipes
     * from a {@link RecipeCatalog}.
     *
     * @param catalog  non-null recipe catalog
     * @param strategy non-null planning strategy
     * @throws NullPointerException     if {@code catalog} or {@code strategy} is {@code null}
     * @throws IllegalArgumentException if the catalog has no recipes
     */
    public MealPlannerService(RecipeCatalog catalog, MealPlanStrategy strategy) {
        this(Objects.requireNonNull(catalog, "catalog").all(), strategy);
    }

    /**
     * Generates a meal plan using the configured strategy.
     *
     * @param days        number of days to plan; must be {@code > 0}
     * @param mealsPerDay ordered array of meal types per day; must be non-empty
     * @return a {@link MealPlan} with exactly {@code days × mealsPerDay.length} slots
     * @throws IllegalArgumentException if {@code days <= 0} or {@code mealsPerDay} is empty
     * @throws NullPointerException     if {@code mealsPerDay} is {@code null}
     */
    public MealPlan plan(int days, MealType... mealsPerDay) {
        return strategy.generatePlan(days, mealsPerDay, catalog);
    }

    /**
     * Generates a shopping list by first planning and then aggregating ingredients
     * (no pantry subtraction at this level).
     *
     * @param days        number of days to plan; must be {@code > 0}
     * @param mealsPerDay ordered array of meal types per day; must be non-empty
     * @return aggregated {@link ShoppingList} for the planned meals
     * @throws IllegalArgumentException if {@code days <= 0} or {@code mealsPerDay} is empty
     * @throws NullPointerException     if {@code mealsPerDay} is {@code null}
     */
    public ShoppingList buildShoppingList(int days, MealType... mealsPerDay) {
        MealPlan plan = plan(days, mealsPerDay);
        return grocery.buildFrom(plan);
    }

    /**
     * Convenience overload that creates a repeating pattern of meal types for each day,
     * using a simple mapping: index {@code 0} → {@link MealType#LUNCH}, others → {@link MealType#DINNER}.
     * <p>Intended only for quick demos; callers needing full control should use
     * {@link #plan(int, MealType...)}.</p>
     *
     * @param days         number of days to plan; must be {@code > 0}
     * @param mealsPerDay  number of meals per day; must be {@code > 0}
     * @return generated {@link MealPlan}
     * @throws IllegalArgumentException if {@code days <= 0} or {@code mealsPerDay <= 0}
     */
    public MealPlan plan(int days, int mealsPerDay) {
        if (mealsPerDay <= 0) throw new IllegalArgumentException("mealsPerDay must be > 0");
        MealType[] types = new MealType[mealsPerDay];
        for (int i = 0; i < mealsPerDay; i++) {
            types[i] = (i == 0) ? MealType.LUNCH : MealType.DINNER; // 简单映射
        }
        return plan(days, types);
    }

    /**
     * Convenience overload that builds a shopping list for the simple
     * {@code mealsPerDay} pattern produced by {@link #plan(int, int)}.
     *
     * @param days         number of days to plan; must be {@code > 0}
     * @param mealsPerDay  number of meals per day; must be {@code > 0}
     * @return aggregated {@link ShoppingList} without pantry subtraction
     * @throws IllegalArgumentException if {@code days <= 0} or {@code mealsPerDay <= 0}
     */
    public ShoppingList buildShoppingList(int days, int mealsPerDay) {
        return buildShoppingList(days, plan(days, mealsPerDay).slots()
                .stream().map(MealSlot::type).toArray(MealType[]::new));
    }
}