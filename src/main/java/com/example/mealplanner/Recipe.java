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
 * A simple recipe consisting of a name and a list of ingredients.
 */
public final class Recipe {
    private final String name;
    private final List<Ingredient> ingredients;

    public Recipe(String name, List<Ingredient> ingredients) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("recipe name must not be blank");
        }
        Objects.requireNonNull(ingredients, "ingredients");
        this.name = name.trim();
        this.ingredients = List.copyOf(ingredients);
    }

    public String name() { return name; }

    /** Unmodifiable view. */
    public List<Ingredient> ingredients() {
        return Collections.unmodifiableList(ingredients);
    }

    /** Convenience factory. */
    public static Recipe of(String name, Ingredient... items) {
        List<Ingredient> list = new ArrayList<>();
        Collections.addAll(list, items);
        return new Recipe(name, list);
    }
}

