/**
 * -----------------------------------------------------------------------------
 * File Name: Recipe.java
 * Project: meal-planner
 * Description:
 *    A simple recipe consisting of a name and a list of ingredients.
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
import java.util.Objects;

/**
 * Immutable recipe consisting of a human-readable name and an ordered list of
 * {@link Ingredient} entries.
 *
 * <p><b>Semantics:</b>
 * <ul>
 *   <li>The ingredient list is defensively copied on construction and exposed as
 *       an unmodifiable view; the {@code Recipe} is immutable.</li>
 *   <li>This class does not enforce unique ingredient identities; downstream
 *       aggregation (e.g., in {@link ShoppingListBuilder}) may choose to merge
 *       by (name, unit) if desired.</li>
 * </ul>
 *
 * @since 1.0
 */
public final class Recipe {
    private final String name;
    private final List<Ingredient> ingredients;

    /**
     * Creates a recipe with the given name and ingredients.
     *
     * @param name        non-blank recipe name (will be trimmed)
     * @param ingredients non-null list of {@link Ingredient}; copied defensively
     * @throws IllegalArgumentException if {@code name} is null/blank
     * @throws NullPointerException     if {@code ingredients} is null or contains nulls
     */
    public Recipe(String name, List<Ingredient> ingredients) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("recipe name must not be blank");
        }
        Objects.requireNonNull(ingredients, "ingredients");
        this.name = name.trim();
        this.ingredients = List.copyOf(ingredients);
    }

    /**
     * Returns the trimmed display name of this recipe.
     *
     * @return non-empty recipe name
     */
    public String name() { return name; }

    /**
     * Returns an unmodifiable view of the ingredients in declaration order.
     *
     * @return unmodifiable list of {@link Ingredient}
     */
    public List<Ingredient> ingredients() {
        return Collections.unmodifiableList(ingredients);
    }

    /**
     * Convenience factory for small recipes.
     *
     * @param name  non-blank recipe name
     * @param items zero or more {@link Ingredient} entries
     * @return a new immutable {@link Recipe}
     * @throws IllegalArgumentException if {@code name} is null/blank
     * @throws NullPointerException     if {@code items} contains nulls
     */
    public static Recipe of(String name, Ingredient... items) {
        List<Ingredient> list = new ArrayList<>();
        Collections.addAll(list, items);
        return new Recipe(name, list);
    }
}

