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
 * Builds a {@link ShoppingList} by aggregating ingredients from one or more {@link Recipe}s,
 * using the normalized identity <code>(name, unit)</code> as the merge key.
 *
 * <p><b>Rules:</b>
 * <ul>
 *   <li>No unit conversion in the MVP (e.g., G and KG are distinct keys).</li>
 *   <li>Names are assumed to be already normalized by {@link Ingredient} (lowercase + trim);
 *       the merge key is <code>name + "|" + unit</code>.</li>
 *   <li>Amounts are summed as {@code double}; a LinkedHashMap preserves insertion order.</li>
 * </ul>
 *
 * <p>This class embodies the <em>Builder</em> pattern in the design.</p>
 *
 * @since 1.0
 */
public final class ShoppingListBuilder {

    // key: name(lowercase trimmed) + "|" + unit
    private final Map<String, Double> totals = new LinkedHashMap<>();
    private final Map<String, Unit> units = new HashMap<>();

    /**
     * Adds all ingredients from the given {@link Recipe} into the aggregation.
     * Existing items with the same identity (normalized name, unit) will have their
     * amounts increased by the ingredient's amount.
     *
     * @param recipe non-null recipe whose ingredients will be aggregated
     * @return this builder (for fluent chaining)
     * @throws NullPointerException if {@code recipe} is {@code null}
     */
    public ShoppingListBuilder addRecipe(Recipe recipe) {
        for (Ingredient ing : recipe.ingredients()) {
            Unit cu = Units.canonical(ing.unit());
            double amt = Units.toCanonical(ing.amount(), ing.unit());
            String key = ing.name() + "|" + cu; // name 已在 Ingredient 内部小写化
            totals.merge(key, amt, Double::sum);
            units.putIfAbsent(key, cu);
        }
        return this;
    }

    /**
     * Adds all ingredients from a collection of recipes.
     * Equivalent to calling {@link #addRecipe(Recipe)} for each element.
     *
     * @param recipes non-null collection of recipes
     * @return this builder (for fluent chaining)
     * @throws NullPointerException if {@code recipes} is {@code null} or contains {@code null}
     */
    public ShoppingListBuilder addRecipes(Collection<Recipe> recipes) {
        for (Recipe r : recipes) addRecipe(r);
        return this;
    }

    /**
     * Builds an immutable {@link ShoppingList} from the current aggregation state.
     * The builder itself remains reusable (you may continue to add and build again).
     *
     * @return a {@link ShoppingList} whose items are derived from the current totals
     */
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
