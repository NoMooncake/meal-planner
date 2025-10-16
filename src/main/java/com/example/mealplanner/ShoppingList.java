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
 * Immutable container of aggregated items.
 *
 * <p>Contains a list of {@link ShoppingListItem}.</p>
 *
 * @since 1.0
 */
public final class ShoppingList {
    private final List<ShoppingListItem> items;

    public ShoppingList(List<ShoppingListItem> items) {
        this.items = List.copyOf(items);
    }

    public List<ShoppingListItem> items() {
        return Collections.unmodifiableList(items);
    }
}