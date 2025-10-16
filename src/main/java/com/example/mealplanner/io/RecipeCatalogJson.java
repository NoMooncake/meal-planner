/**
 * -----------------------------------------------------------------------------
 * File Name: RecipeCatalogJson.java
 * Project: meal-planner
 * Description:
 * [Add brief description here]
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/15
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner.io;

import com.example.mealplanner.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON load/save utilities for {@link RecipeCatalog}.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Load a {@link RecipeCatalog} from a JSON file matching {@link RecipeCatalogDto} schema.</li>
 *   <li>Save a {@link RecipeCatalog} to a JSON file (pretty-printed).</li>
 * </ul>
 *
 * <p>Design notes:</p>
 * <ul>
 *   <li>Unit tokens are normalized via {@link Unit#valueOf(String)} using upper-case.</li>
 *   <li>Name normalization and validation are handled by domain objects
 *       ({@link Ingredient#of(String, double, Unit)} and {@link Recipe}).</li>
 * </ul>
 *
 * @since 1.0
 */
public final class RecipeCatalogJson {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private RecipeCatalogJson() { /* no instances */ }

    /**
     * Loads a {@link RecipeCatalog} from a JSON file.
     *
     * @param path path to a file that matches {@link RecipeCatalogDto} schema
     * @return populated {@link RecipeCatalog}; never {@code null}
     * @throws IOException              if I/O or parsing fails
     * @throws IllegalArgumentException if a DTO entry is semantically invalid for the domain
     */
    public static RecipeCatalog fromFile(Path path) throws IOException {
        var dto = MAPPER.readValue(path.toFile(), RecipeCatalogDto.class);
        List<Recipe> recipes = new ArrayList<>();
        if (dto != null && dto.recipes != null) {
            for (var r : dto.recipes) {
                if (r == null) continue;
                List<Ingredient> ings = new ArrayList<>();
                if (r.ingredients != null) {
                    for (var ie : r.ingredients) {
                        if (ie == null) continue;
                        Unit u = Unit.valueOf(ie.unit.toUpperCase());
                        ings.add(Ingredient.of(ie.name, ie.amount, u));
                    }
                }
                recipes.add(new Recipe(r.name, ings));
            }
        }
        return new RecipeCatalog(recipes);
    }

    /**
     * Saves the provided {@link RecipeCatalog} to a JSON file.
     *
     * @param catalog non-null catalog to serialize
     * @param path    output path to write (will be overwritten)
     * @throws IOException          if writing fails
     * @throws NullPointerException if {@code catalog} or {@code path} is {@code null}
     */
    public static void toFile(RecipeCatalog catalog, Path path) throws IOException {
        RecipeCatalogDto dto = new RecipeCatalogDto();
        dto.recipes = catalog.all().stream().map(r -> {
            RecipeCatalogDto.RecipeEntry re = new RecipeCatalogDto.RecipeEntry();
            re.name = r.name();
            re.ingredients = r.ingredients().stream().map(ing -> {
                RecipeCatalogDto.IngredientEntry ie = new RecipeCatalogDto.IngredientEntry();
                ie.name = ing.name();
                ie.amount = ing.amount();
                ie.unit = ing.unit().name();
                return ie;
            }).toList();
            return re;
        }).toList();
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), dto);
    }
}