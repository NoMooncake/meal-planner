/**
 * -----------------------------------------------------------------------------
 * File Name: MealPlanStrategy.java
 * Project: meal-planner
 * Description:
 *      Interface for meal plan generation strategies.
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/12
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner.strategy;

import com.example.mealplanner.MealPlan;
import com.example.mealplanner.MealType;
import com.example.mealplanner.Recipe;

import java.util.List;

/**
 * Strategy that produces a {@link MealPlan} given:
 * <ul>
 *   <li>the number of days to plan,</li>
 *   <li>the ordered set of {@link MealType} slots per day, and</li>
 *   <li>a catalog of candidate {@link Recipe} items to choose from.</li>
 * </ul>
 * <p><b>Contract:</b>
 * <ul>
 *   <li>Implementations must return a plan with exactly {@code days} entries,
 *       each day containing the provided {@code mealsPerDay} in the same order.</li>
 *   <li>Recipe reuse is allowed unless the implementation states otherwise.</li>
 *   <li>If randomness is used, the implementation should support injecting a
 *       {@link java.util.Random} (e.g., via constructor) so results can be deterministic in tests.</li>
 *   <li>Inputs should be treated as read-only; do not mutate {@code mealsPerDay} or {@code catalog}.</li>
 * </ul>
 * This interface represents the <em>Strategy</em> pattern in the application.
 *
 * @since 1.0
 */
public interface MealPlanStrategy {

    /**
     * Generates a meal plan by selecting recipes for the requested number of days and meal types.
     *
     * @param days        number of days to plan; must be {@code >= 1}
     * @param mealsPerDay ordered array of {@link MealType} slots to fill for each day;
     *                    must be non-{@code null} and non-empty; order is preserved in the output
     * @param catalog     list of candidate {@link Recipe} objects to choose from; must be non-{@code null}
     * @return a new {@link MealPlan} containing exactly {@code days} × {@code mealsPerDay.length} slots
     * @throws IllegalArgumentException if {@code days < 1} or {@code mealsPerDay.length == 0}
     * @throws NullPointerException     if {@code mealsPerDay} or {@code catalog} is {@code null},
     *                                  or if {@code mealsPerDay} contains {@code null}
     */
    MealPlan generatePlan(int days, MealType[] mealsPerDay, List<Recipe> catalog);
}