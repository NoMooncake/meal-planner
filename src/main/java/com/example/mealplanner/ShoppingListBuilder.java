/**
 * -----------------------------------------------------------------------------
 * File Name: ShoppingListBuilder.java
 * Project: meal-planner
 * Description:
 *      Aggregates ingredients from multiple recipes into a shopping list.
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/12
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner;

import java.util.*;

/**
 * Builds a {@link ShoppingList} from a {@link MealPlan} by aggregating
 * ingredients using the normalized identity (name, unit).
 * <p>
 * Notes:
 * <ul>
 *   <li>No unit conversion is performed in the MVP (e.g., G vs KG are distinct).</li>
 *   <li>Names are normalized (lowercase + trim) before merging.</li>
 * </ul>
 * This class is the Builder pattern in the design.
 * @since 1.0
 */
public final class ShoppingListBuilder {

    // key: name(lowercase trimmed) + "|" + unit
    private final Map<String, Double> totals = new LinkedHashMap<>();
    private final Map<String, Unit> units = new HashMap<>();

    // Normalize name to lowercase trimmed
    public ShoppingListBuilder addRecipe(Recipe recipe) {
        for (Ingredient ing : recipe.ingredients()) {
            String key = ing.name() + "|" + ing.unit(); // normalize name to lowercase trimmed
            totals.merge(key, ing.amount(), Double::sum);
            units.putIfAbsent(key, ing.unit());
        }
        return this;
    }

    public ShoppingListBuilder addRecipes(Collection<Recipe> recipes) {
        for (Recipe r : recipes) addRecipe(r);
        return this;
    }

    // Convenience varargs factory
    public ShoppingList build() {
        List<ShoppingListItem> items = new ArrayList<>();
        for (Map.Entry<String, Double> e : totals.entrySet()) {
            String key = e.getKey();
            double total = e.getValue();
            int sep = key.indexOf('|');
            String name = key.substring(0, sep);
            Unit unit = units.get(key);
            items.add(new ShoppingListItem(name, unit, total));
        }
        return new ShoppingList(items);
    }
}
