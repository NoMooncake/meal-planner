/**
 * -----------------------------------------------------------------------------
 * File Name: RecipeCatalog.java
 * Project: meal-planner
 * Description:
 *      A simple in-memory catalog of recipes.
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/12
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable, in-memory catalog of {@link Recipe} objects.
 *
 * <p><b>Semantics:</b>
 * <ul>
 *   <li>The backing list is defensively copied on construction; this catalog is immutable.</li>
 *   <li>No de-duplication is enforced; recipes may repeat names if provided as such.</li>
 *   <li>Downstream components (e.g., strategies) treat the returned list as read-only.</li>
 * </ul>
 *
 * @since 1.0
 */
public final class RecipeCatalog {
    private final List<Recipe> recipes;

    /**
     * Creates a catalog from the given list of recipes.
     *
     * @param recipes non-null list of {@link Recipe}; copied defensively
     * @throws NullPointerException if {@code recipes} is {@code null} or contains {@code null} elements
     */
    public RecipeCatalog(List<Recipe> recipes) {
        this.recipes = List.copyOf(recipes);
    }

    /**
     * Returns an unmodifiable view of all recipes in this catalog, in insertion order.
     *
     * @return unmodifiable list of {@link Recipe}
     */
    public List<Recipe> all() {
        return Collections.unmodifiableList(recipes);
    }

    /**
     * Returns the number of recipes in the catalog.
     *
     * @return non-negative size
     */
    public int size() {
        return recipes.size();
    }

    /**
     * Returns a new catalog that contains all current recipes plus {@code extra}.
     * The original catalog remains unchanged (shallow copy).
     *
     * @param extra non-null recipe to append
     * @return a new {@link RecipeCatalog} instance with the appended recipe
     * @throws NullPointerException if {@code extra} is {@code null}
     */
    public RecipeCatalog plus(Recipe extra) {
        List<Recipe> copy = new ArrayList<>(recipes);
        copy.add(extra);
        return new RecipeCatalog(copy);
    }

    /**
     * Returns a small sample catalog used by the MVP and tests.
     * <p>Includes: Eggs, Pasta, Chicken Salad, Fried Rice.</p>
     *
     * @return sample {@link RecipeCatalog}
     */
    public static RecipeCatalog samples() {
        List<Recipe> list = new ArrayList<>();

        // 1) Eggs
        list.add(Recipe.of("Eggs",
                Ingredient.of("Egg", 2, Unit.PCS),
                Ingredient.of("Milk", 50, Unit.ML)));

        // 2) Pasta
        list.add(Recipe.of("Pasta",
                Ingredient.of("Pasta", 100, Unit.G),
                Ingredient.of("Milk", 100, Unit.ML)));

        // 3) Chicken Salad
        list.add(Recipe.of("Chicken Salad",
                Ingredient.of("Chicken", 150, Unit.G),
                Ingredient.of("Lettuce", 100, Unit.G),
                Ingredient.of("Olive Oil", 10, Unit.ML)));

        // 4) Fried Rice
        list.add(Recipe.of("Fried Rice",
                Ingredient.of("Rice", 150, Unit.G),
                Ingredient.of("Egg", 1, Unit.PCS),
                Ingredient.of("Oil", 10, Unit.ML)));

        return new RecipeCatalog(list);
    }
}