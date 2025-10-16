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
 * Immutable ingredient used across recipes and shopping lists.
 *
 * <p><b>Identity rule:</b> identity is the pair <code>(normalized name, unit)</code>.
 * The {@code amount} is <em>not</em> part of identity so that multiple occurrences
 * can be aggregated later. Name normalization = {@code trim().toLowerCase(Locale.ROOT)}.</p>
 *
 * <p><b>Validation:</b></p>
 * <ul>
 *   <li>name: non-null and non-blank (normalized on construction)</li>
 *   <li>unit: non-null</li>
 *   <li>amount: {@code >= 0}</li>
 * </ul>
 *
 * @since 1.0
 */
public final class Ingredient {

    private final String name;   // normalized: trimmed + lowercased for identity
    private final double amount;
    private final Unit unit;

    /**
     * Static factory with validation that delegates to the canonical constructor.
     *
     * @param name   ingredient name (will be normalized; must not be blank)
     * @param amount non-negative amount
     * @param unit   measurement unit (non-null)
     * @return an immutable {@link Ingredient}
     * @throws IllegalArgumentException if {@code name} is null/blank or {@code amount < 0}
     * @throws NullPointerException     if {@code unit} is null
     */
    public static Ingredient of(String name, double amount, Unit unit) {
        return new Ingredient(name, amount, unit);
    }

    /**
     * Creates an {@link Ingredient} with validation and name normalization.
     *
     * @param name   ingredient name (will be normalized; must not be blank)
     * @param amount non-negative amount
     * @param unit   measurement unit (non-null)
     * @throws IllegalArgumentException if {@code name} is null/blank or {@code amount < 0}
     * @throws NullPointerException     if {@code unit} is null
     */
    public Ingredient(String name, double amount, Unit unit) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (unit == null) {
            throw new IllegalArgumentException("unit must not be null");
        }
        if (!Double.isFinite(amount) || amount < 0) {
            throw new IllegalArgumentException("amount must be finite and >= 0");
        }
        this.name = name.trim().toLowerCase(Locale.ROOT);
        this.amount = amount;
        this.unit = unit;
    }

    /**
     * Returns the normalized ingredient name used for identity comparisons.
     *
     * @return lowercase & trimmed name
     */
    public String name() { return name; }

    /**
     * Returns the amount expressed in {@link #unit}.
     *
     * @return non-negative amount
     */
    public double amount() { return amount; }

    /**
     * Returns the measurement unit.
     *
     * @return non-null unit
     */
    public Unit unit() { return unit; }

    /**
     * Returns a new {@link Ingredient} with the same name and unit but a different amount.
     * @param newAmount non-negative amount
     * @return a new {@link Ingredient} with the specified amount
     * @throws IllegalArgumentException if {@code newAmount < 0}
     */
    public Ingredient withAmount(double newAmount) {
        if (!Double.isFinite(newAmount) || newAmount < 0) {
            throw new IllegalArgumentException("amount must be finite and >= 0");
        }
        return new Ingredient(this.name, newAmount, this.unit);
    }

    /**
     * Equality is based on <code>(name, unit)</code> only; {@code amount} is excluded.
     * This enables downstream aggregation where amounts are summed.
     */
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ingredient that)) return false;
        return name.equals(that.name) && unit == that.unit;
    }

    /** Hash code derives from <code>(name, unit)</code> only. */
    @Override public int hashCode() {
        return Objects.hash(name, unit);
    }

    @Override public String toString() {
        return "Ingredient{" + name + " " + amount + " " + unit + '}';
    }
}
