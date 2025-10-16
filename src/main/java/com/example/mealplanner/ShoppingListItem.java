/**
 * -----------------------------------------------------------------------------
 * File Name: ShoppingListItem.java
 * Project: meal-planner
 * Description:
 *   An aggregated line in the shopping list.
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/12
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner;

import java.util.Objects;

/**
 * An aggregated line item in a {@link ShoppingList}.
 *
 * <p>Represents the total amount to purchase for one ingredient identity
 * <code>(normalized name, unit)</code>. The name is expected to be already
 * normalized by upstream components (e.g., the builder), typically
 * {@code trim().toLowerCase(Locale.ROOT)}.</p>
 *
 * @since 1.0
 */
public final class ShoppingListItem {
    /** Normalized ingredient name (lowercase & trimmed). */
    private final String name;
    private final Unit unit;
    private final double totalAmount;

    /**
     * Creates an aggregated shopping list item.
     *
     * @param name         normalized ingredient name; must not be {@code null}
     * @param unit         measurement unit; must not be {@code null}
     * @param totalAmount  non-negative total amount in the given {@code unit}
     * @throws NullPointerException     if {@code name} or {@code unit} is {@code null}
     * @throws IllegalArgumentException if {@code totalAmount < 0}
     */
    public ShoppingListItem(String name, Unit unit, double totalAmount) {
        this.name = Objects.requireNonNull(name, "name");
        this.unit = Objects.requireNonNull(unit, "unit");
        if (totalAmount < 0) throw new IllegalArgumentException("totalAmount must be >= 0");
        this.totalAmount = totalAmount;
    }

    /**
     * Returns the normalized ingredient name used as part of the identity.
     * @return lowercase & trimmed name (never {@code null})
     */
    public String name() {
        return name;
    }

    /**
     * Returns the measurement unit.
     * @return non-null unit
     */
    public Unit unit() {
        return unit;
    }

    /**
     * Returns the non-negative total amount to buy (in {@link #unit}).
     * @return amount &ge; 0
     */
    public double totalAmount() {
        return totalAmount;
    }

    @Override public String toString() {
        return name + " " + totalAmount + " " + unit;
    }
}
