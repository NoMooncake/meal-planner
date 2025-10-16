/**
 * -----------------------------------------------------------------------------
 * File Name: Pantry.java
 * Project: meal-planner
 * Description:
 *      A simple in-memory pantry (stock of ingredients).
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/12
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * In-memory pantry that tracks the current stock of ingredients.
 *
 * <p><b>Identity rule:</b> stock is keyed by <code>(normalized name, unit)</code>.
 * Name normalization is {@code trim().toLowerCase(Locale.ROOT)}; units must match
 * exactly (no unit conversion in the MVP).</p>
 *
 * <p><b>Mutability:</b> the pantry is a mutable component; use {@link #snapshot()}
 * in tests to assert internal state without exposing the backing map.</p>
 *
 * @since 1.0
 */
public final class Pantry {

    private final Map<String, Double> stock = new LinkedHashMap<>();

    /**
     * Adds to (or initializes) the stock for a given ingredient.
     * If the key already exists, the amount is increased by the given value.
     *
     * <p>Identity is case/whitespace-insensitive on {@code name} and exact on {@link Unit}.</p>
     *
     * @param name   ingredient name; case/space-insensitive; must not be blank
     * @param amount non-negative amount to add
     * @param unit   measurement unit; must not be {@code null}
     * @return this pantry instance (for fluent usage)
     * @throws IllegalArgumentException if {@code name} is null/blank or {@code amount < 0}
     * @throws NullPointerException     if {@code unit} is {@code null}
     */
    public Pantry add(String name, double amount, Unit unit) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        if (unit == null) throw new IllegalArgumentException("unit must not be null");
        if (amount < 0) throw new IllegalArgumentException("amount must be >= 0");

        Unit cu = Units.canonical(unit);
        double amt = Units.toCanonical(amount, unit);

        String key = normalize(name) + "|" + cu;
        stock.merge(key, amt, Double::sum);
        return this;
    }

    /**
     * Returns the current stock amount for the given ingredient identity.
     *
     * @param name ingredient name; case/space-insensitive
     * @param unit measurement unit (must match exactly)
     * @return the non-negative amount in stock, or {@code 0.0} if not present
     * @throws NullPointerException if {@code name} or {@code unit} is {@code null}
     */
    public double amountOf(String name, Unit unit) {
        Unit cu = Units.canonical(unit);
        String key = normalize(java.util.Objects.requireNonNull(name)) + "|" + cu;
        return stock.getOrDefault(key, 0.0);
    }

    private static String normalize(String s) {
        return s.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Returns an immutable snapshot of the internal state.
     * <p>Intended for debugging/tests; callers cannot mutate the pantry via this view.</p>
     *
     * @return an unmodifiable copy of the current key→amount map
     */
    public Map<String, Double> snapshot() {
        return Map.copyOf(stock);
    }
}