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
 * An aggregated line in the shopping list.
 *
 * <p>Consists of a normalized ingredient name, a unit, and a total amount.</p>
 *
 * @since 1.0
 */
public final class ShoppingListItem {
    private final String name; // already normalized (lowercase) by builder
    private final Unit unit;
    private final double totalAmount;

    public ShoppingListItem(String name, Unit unit, double totalAmount) {
        this.name = Objects.requireNonNull(name, "name");
        this.unit = Objects.requireNonNull(unit, "unit");
        if (totalAmount < 0) throw new IllegalArgumentException("totalAmount must be >= 0");
        this.totalAmount = totalAmount;
    }

    public String name() { return name; }
    public Unit unit() { return unit; }
    public double totalAmount() { return totalAmount; }

    @Override public String toString() {
        return name + " " + totalAmount + " " + unit;
    }
}
