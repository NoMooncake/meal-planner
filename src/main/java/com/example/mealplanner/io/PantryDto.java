/**
 * -----------------------------------------------------------------------------
 * File Name: PantryDto.java
 * Project: meal-planner
 * Description:
 * [Add brief description here]
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/15
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner.io;

import java.util.List;

/**
 * Data-transfer objects (DTOs) that describe the JSON shape for Pantry I/O.
 *
 * <p>JSON schema:</p>
 * <pre>{@code
 * {
 *   "stock": [
 *     { "name": "milk", "amount": 200.0, "unit": "ML" },
 *     { "name": "egg",  "amount": 2.0,   "unit": "PCS" }
 *   ]
 * }
 * }</pre>
 *
 * <p>Notes:</p>
 * <ul>
 *   <li>Validation (non-negative amounts, known units, non-blank names) is performed
 *       when converting to the domain model, not here.</li>
 * </ul>
 *
 * @since 1.0
 */
public final class PantryDto {

    /** The stock entries. May be {@code null} or empty. */
    public List<StockEntry> stock;

    /**
     * One stock record in the JSON array.
     * <p>All fields are public for Jackson databind.</p>
     */
    public static final class StockEntry {
        /** Ingredient name as-is in JSON (will be normalized downstream). */
        public String name;
        /** Amount in the declared unit. */
        public double amount;
        /** Unit token (PCS|G|ML) – case-insensitive. */
        public String unit;
    }
}
