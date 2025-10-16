/**
 * -----------------------------------------------------------------------------
 * File Name: RecipeCatalogDto.java
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
 * DTOs describing the JSON shape for {@code RecipeCatalog} I/O.
 *
 * <p>JSON schema:</p>
 * <pre>{@code
 * {
 *   "recipes": [
 *     {
 *       "name": "Fried Rice",
 *       "ingredients": [
 *         { "name": "rice", "amount": 150, "unit": "G" },
 *         { "name": "egg",  "amount": 1,   "unit": "PCS" }
 *       ]
 *     }
 *   ]
 * }
 * }</pre>
 *
 * @since 1.0
 */
public final class RecipeCatalogDto {
    /** Recipe entries (may be null/empty). */
    public List<RecipeEntry> recipes;

    /** One recipe record. */
    public static final class RecipeEntry {
        public String name;
        public List<IngredientEntry> ingredients;
    }

    /** One ingredient record in a recipe. */
    public static final class IngredientEntry {
        public String name;
        public double amount;
        /** Unit token (PCS|G|ML) – case-insensitive. */
        public String unit;
    }
}