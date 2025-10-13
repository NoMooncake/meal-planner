/**
 * -----------------------------------------------------------------------------
 * File Name: Ingredient.java
 * Project: meal-planner
 * Description:
 *     An immutable ingredient entry used by recipes and shopping lists.
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/12
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner;

import java.util.Locale;
import java.util.Objects;

/**
 * An immutable ingredient entry used by recipes and shopping lists.
 *
 * <p>Identity rule:</p>
 * <ul>
 *   <li>Two ingredients are considered the <b>same identity</b> if their
 *       {@code name} (case/whitespace-insensitive) and {@link Unit} are equal.
 *       The {@code amount} is <b>NOT</b> part of identity, so we can combine amounts later.</li>
 * </ul>
 *
 * <p>Validation:</p>
 * <ul>
 *   <li>name: not null/blank</li>
 *   <li>unit: not null</li>
 *   <li>amount: must be &gt;= 0</li>
 * </ul>
 */
public final class Ingredient {

    private final String name;   // normalized: trimmed + lowercased for identity
    private final double amount;
    private final Unit unit;

    /**
     * Factory method with basic validation.
     */
    public static Ingredient of(String name, double amount, Unit unit) {
        return new Ingredient(name, amount, unit);
    }

    public Ingredient(String name, double amount, Unit unit) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (unit == null) {
            throw new IllegalArgumentException("unit must not be null");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be >= 0");
        }
        this.name = name.trim().toLowerCase(Locale.ROOT);
        this.amount = amount;
        this.unit = unit;
    }

    /** Ingredient name (normalized to lowercase & trimmed). */
    public String name() { return name; }

    /** Amount in the given unit. */
    public double amount() { return amount; }

    /** Measurement unit. */
    public Unit unit() { return unit; }

    /** Returns a copy with a different amount (keeps immutability). */
    public Ingredient withAmount(double newAmount) {
        return new Ingredient(this.name, newAmount, this.unit);
    }

    /** Identity is (name, unit). Amount is intentionally excluded for aggregation use-cases. */
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ingredient that)) return false;
        return name.equals(that.name) && unit == that.unit;
    }

    @Override public int hashCode() {
        return Objects.hash(name, unit);
    }

    @Override public String toString() {
        return "Ingredient{" + name + " " + amount + " " + unit + '}';
    }
}
