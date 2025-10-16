/**
 * -----------------------------------------------------------------------------
 * File Name: RecipeCatalogJsonTest.java
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RecipeCatalog JSON I/O.
 */
class RecipeCatalogJsonTest {

    // ---------- helpers ----------

    private static void assertIngredientEquals(Ingredient a, Ingredient b) {
        assertEquals(a.name(), b.name(), "ingredient name");
        assertEquals(a.unit(), b.unit(), "ingredient unit");
        assertEquals(a.amount(), b.amount(), 1e-9, "ingredient amount");
    }

    private static void assertRecipeEquals(Recipe a, Recipe b) {
        assertEquals(a.name(), b.name(), "recipe name");
        assertEquals(a.ingredients().size(), b.ingredients().size(), "ingredients size");
        for (int i = 0; i < a.ingredients().size(); i++) {
            assertIngredientEquals(a.ingredients().get(i), b.ingredients().get(i));
        }
    }

    // ---------- tests ----------

    @Test
    void roundTrip_preservesAllData(@TempDir Path tmp) throws IOException {
        // given: a small catalog
        Recipe r1 = Recipe.of("Eggs",
                Ingredient.of("egg", 2, Unit.PCS),
                Ingredient.of("milk", 50, Unit.ML));
        Recipe r2 = Recipe.of("Pasta",
                Ingredient.of("pasta", 100, Unit.G),
                Ingredient.of("milk", 100, Unit.ML));
        RecipeCatalog catalog = new RecipeCatalog(List.of(r1, r2));

        Path file = tmp.resolve("catalog.json");

        // when: save -> load
        RecipeCatalogJson.toFile(catalog, file);
        RecipeCatalog loaded = RecipeCatalogJson.fromFile(file);

        // then
        assertEquals(catalog.size(), loaded.size(), "catalog size");
        List<Recipe> a = catalog.all();
        List<Recipe> b = loaded.all();
        for (int i = 0; i < a.size(); i++) {
            assertRecipeEquals(a.get(i), b.get(i));
        }
    }

    @Test
    void load_handlesCaseInsensitivityAndWhitespace(@TempDir Path tmp) throws IOException {
        // unit 小写, 名字带空格/大小写, 读取后应被规范化（Ingredient 会归一化为小写+trim）
        String json = """
        {
          "recipes": [{
            "name": "  Fried Rice  ",
            "ingredients": [
              { "name": "  RICE ", "amount": 150, "unit": "g"  },
              { "name": "Egg",     "amount":   1, "unit": "pcs" },
              { "name": "Oil",     "amount":  10, "unit": "ml"  }
            ]
          }]
        }
        """;
        Path file = tmp.resolve("catalog.json");
        Files.writeString(file, json);

        RecipeCatalog loaded = RecipeCatalogJson.fromFile(file);
        assertEquals(1, loaded.size());
        Recipe r = loaded.all().get(0);
        assertEquals("Fried Rice", r.name()); // Recipe 构造器会 trim

        // Ingredient 正常化：名称小写、单位大写
        assertEquals("rice", r.ingredients().get(0).name());
        assertEquals(Unit.G, r.ingredients().get(0).unit());
        assertEquals(150.0, r.ingredients().get(0).amount(), 1e-9);

        assertEquals("egg", r.ingredients().get(1).name());
        assertEquals(Unit.PCS, r.ingredients().get(1).unit());

        assertEquals("oil", r.ingredients().get(2).name());
        assertEquals(Unit.ML, r.ingredients().get(2).unit());
    }

    @Test
    void load_emptyCatalog_ok(@TempDir Path tmp) throws IOException {
        Path file = tmp.resolve("empty.json");
        Files.writeString(file, "{ \"recipes\": [] }");

        RecipeCatalog loaded = RecipeCatalogJson.fromFile(file);
        assertEquals(0, loaded.size());
        assertTrue(loaded.all().isEmpty());
    }

    @Test
    void load_badUnit_throws(@TempDir Path tmp) throws IOException {
        Path file = tmp.resolve("bad.json");
        Files.writeString(file,
                "{ \"recipes\": [ { \"name\": \"Water\", \"ingredients\": [ {\"name\":\"water\",\"amount\":100,\"unit\":\"LITER\"} ] } ] }");

        assertThrows(IllegalArgumentException.class, () -> RecipeCatalogJson.fromFile(file));
    }
}