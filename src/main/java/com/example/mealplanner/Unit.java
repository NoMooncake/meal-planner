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
 * Measurement units used by the MVP.
 *
 * <p>Families:</p>
 * <ul>
 *   <li>COUNT: {@link #PCS}</li>
 *   <li>MASS:  {@link #G}, {@link #KG} (1 KG = 1000 G)</li>
 *   <li>VOLUME:{@link #ML}, {@link #L} (1 L  = 1000 ML)</li>
 * </ul>
 *
 * @since 1.0
 */
public enum Unit {
    PCS,   // pieces
    G, KG, // mass
    ML, L  // milliliters
}
