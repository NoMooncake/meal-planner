/**
 * -----------------------------------------------------------------------------
 * File Name: PriceBook.java
 * Project: meal-planner
 * Description:
 *      Simple in-memory price book mapping (ingredient name, unit) to unit price.
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/22
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */

package com.example.mealplanner;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Simple in-memory price book: maps a normalized ingredient name and {@link Unit}
 * to a unit price.
 *
 * <p>Prices are expressed "per unit", where the unit is {@link Unit}. For example,
 * {@code ("chicken", G) -> 0.02} means 0.02 currency units per gram of chicken.</p>
 *
 * <p>Name normalization follows {@link Ingredient}: {@code trim().toLowerCase(Locale.ROOT)}.</p>
 *
 * @since 1.0
 */
public final class PriceBook {

    private final Map<String, Double> unitPrices = new LinkedHashMap<>();

    private static String key(String name, Unit unit) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        Objects.requireNonNull(unit, "unit");
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        return normalized + "|" + unit;
    }

    /**
     * Adds or replaces the unit price for a given (name, unit) pair.
     *
     * @param name          ingredient name (will be normalized)
     * @param unit          measurement unit
     * @param pricePerUnit  non-negative price per single {@code unit}
     * @return this instance for fluent calls
     * @throws IllegalArgumentException if {@code pricePerUnit < 0} or name is blank
     * @throws NullPointerException     if {@code unit} is null
     */
    public PriceBook add(String name, Unit unit, double pricePerUnit) {
        if (pricePerUnit < 0) {
            throw new IllegalArgumentException("pricePerUnit must be >= 0");
        }
        unitPrices.put(key(name, unit), pricePerUnit);
        return this;
    }

    /**
     * Returns the price per single {@code unit} of the given ingredient,
     * or {@link Double#NaN} if the ingredient is unknown to this price book.
     *
     * @param name ingredient name
     * @param unit measurement unit
     * @return price per unit, or {@code Double.NaN} if unknown
     */
    public double unitPrice(String name, Unit unit) {
        return unitPrices.getOrDefault(key(name, unit), Double.NaN);
    }

    /** Convenience overload using an {@link Ingredient}. */
    public double unitPrice(Ingredient ingredient) {
        return unitPrice(ingredient.name(), ingredient.unit());
    }

    /**
     * Estimates the total cost of a recipe as the sum of
     * {@code ingredient.amount * unitPrice(ingredient)}.
     * Unknown ingredients (no price in this book) are ignored (treated as zero).
     *
     * @param recipe recipe to evaluate
     * @return non-negative estimated cost
     */
    public double estimateCost(Recipe recipe) {
        double total = 0.0;
        for (Ingredient ing : recipe.ingredients()) {
            double up = unitPrice(ing);
            if (!Double.isNaN(up)) {
                total += up * ing.amount();
            }
        }
        return total;
    }

    /** Returns an immutable snapshot of the internal key → unitPrice map. */
    Map<String, Double> snapshot() {
        return Map.copyOf(unitPrices);
    }

    /**
     * Sample price book roughly matching {@link RecipeCatalog#samples()}.
     *
     * <p>Numbers are purely illustrative and not meant to be realistic.</p>
     */
    public static PriceBook samples() {
        PriceBook pb = new PriceBook();
        pb.add("Egg",       Unit.PCS, 0.30);
        pb.add("Milk",      Unit.ML,  0.002);   // 2 per liter
        pb.add("Pasta",     Unit.G,   0.015);
        pb.add("Chicken",   Unit.G,   0.020);
        pb.add("Lettuce",   Unit.G,   0.010);
        pb.add("Olive Oil", Unit.ML,  0.050);
        pb.add("Rice",      Unit.G,   0.012);
        pb.add("Oil",       Unit.ML,  0.030);
        return pb;
    }
}
