/**
 * -----------------------------------------------------------------------------
 * File Name: ShoppingList.java
 * Project: meal-planner
 * Description:
 *      Immutable container of aggregated items.
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/12
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner;

import java.util.Collections;
import java.util.List;

/**
 * Immutable container of aggregated shopping items.
 *
 * <p><b>Semantics:</b>
 * <ul>
 *   <li>Holds the result of aggregating ingredients by identity (normalized name, unit).</li>
 *   <li>Ordering of items is preserved from the provided list; this class does not sort.</li>
 *   <li>Immutable: the backing list is defensively copied and exposed as an unmodifiable view.</li>
 * </ul>
 *
 * <p><b>Notes:</b> This class does not perform unit conversion; upstream components
 * (e.g., {@link ShoppingListBuilder}) decide how to aggregate and in which units.</p>
 *
 * @since 1.0
 */
public final class ShoppingList {
    private final List<ShoppingListItem> items;

    /**
     * Creates an immutable shopping list from the given items.
     *
     * @param items non-null list of {@link ShoppingListItem}; copied defensively
     * @throws NullPointerException if {@code items} is {@code null} or contains {@code null} elements
     */
    public ShoppingList(List<ShoppingListItem> items) {
        this.items = List.copyOf(items);
    }

    /**
     * Returns an unmodifiable view of items in this list.
     * The returned view preserves the original order and cannot be mutated.
     *
     * @return unmodifiable list of {@link ShoppingListItem}
     */
    public List<ShoppingListItem> items() {
        return Collections.unmodifiableList(items);
    }
}