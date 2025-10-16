/**
 * -----------------------------------------------------------------------------
 * File Name: MealType.java
 * Project: meal-planner
 * Description:
 *     Type of meal: breakfast, lunch or dinner (add snack later)
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/12
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner;

/**
 * Enumeration of supported meal types.
 *
 * <p>Used by planning strategies and {@link MealSlot} to indicate which
 * meal slots exist for each day in a {@link MealPlan}.</p>
 *
 * <p><b>Notes:</b>
 * <ul>
 *   <li>Names are uppercase enum identifiers, while CLI accepts case-insensitive
 *       tokens (e.g., "breakfast,lunch,dinner") that are converted via
 *       {@link Enum#valueOf(Class, String)} after upper-casing.</li>
 *   <li>Additional types (e.g., SNACK) can be added later without breaking
 *       existing behavior, provided parsers/validators are updated accordingly.</li>
 * </ul>
 *
 * @since 1.0
 */
public enum MealType {
    BREAKFAST,
    LUNCH,
    DINNER
}
