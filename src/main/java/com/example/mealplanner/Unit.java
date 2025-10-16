/**
 * -----------------------------------------------------------------------------
 * File Name: Unit.java
 * Project: meal-planner
 * Description:
 *      Placeholder class for unit tests.
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/12
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner;

/**
 * Measurement units supported by the MVP.
 *
 * <p><b>Semantics:</b>
 * <ul>
 *   <li>No unit conversion is performed in the MVP; aggregation/subtraction require
 *       exact unit matches (e.g., {@code G} and {@code KG} are distinct and will not merge).</li>
 *   <li>Units appear in {@link Ingredient}, {@link ShoppingListItem}, and {@link Pantry} identities.</li>
 * </ul>
 *
 * <p>Future work (optional): introduce a {@code UnitConverter} to normalize compatible units
 * (e.g., KG↔G, L↔ML) before aggregation.</p>
 *
 * @since 1.0
 */
public enum Unit {
    PCS,   // pieces
    G,     // grams
    ML     // milliliters
}
