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
 * A simple in-memory catalog of recipes.
 */
public final class RecipeCatalog {
    private final List<Recipe> recipes;

    public RecipeCatalog(List<Recipe> recipes) {
        this.recipes = List.copyOf(recipes);
    }

    /** Unmodifiable view of all recipes. */
    public List<Recipe> all() {
        return Collections.unmodifiableList(recipes);
    }

    public int size() {
        return recipes.size();
    }

    /** Make a shallow copy with an extra recipe (便于后续扩展). */
    public RecipeCatalog plus(Recipe extra) {
        List<Recipe> copy = new ArrayList<>(recipes);
        copy.add(extra);
        return new RecipeCatalog(copy);
    }

    /** Samples for MVP & tests. */
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