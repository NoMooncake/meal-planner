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
 * Simple in-memory pantry (stock of ingredients).
 * - key: normalized "name|unit"
 * - amount: >= 0
 */
public final class Pantry {

    private final Map<String, Double> stock = new LinkedHashMap<>();

    /** Add (or increase) stock for a given ingredient (name is case/space-insensitive). */
    public Pantry add(String name, double amount, Unit unit) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        if (unit == null) throw new IllegalArgumentException("unit must not be null");
        if (amount < 0) throw new IllegalArgumentException("amount must be >= 0");
        String key = normalize(name) + "|" + unit;
        stock.merge(key, amount, Double::sum);
        return this;
    }

    /** Query how much stock we have for (name, unit). */
    public double amountOf(String name, Unit unit) {
        String key = normalize(Objects.requireNonNull(name)) + "|" + Objects.requireNonNull(unit);
        return stock.getOrDefault(key, 0.0);
    }

    private static String normalize(String s) {
        return s.trim().toLowerCase(Locale.ROOT);
    }

    /** For debugging / tests if needed. */
    Map<String, Double> snapshot() {
        return Map.copyOf(stock);
    }
}