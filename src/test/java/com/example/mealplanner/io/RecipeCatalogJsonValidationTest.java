/**
 * -----------------------------------------------------------------------------
 * File Name: RecipeCatalogJsonValidationTest.java
 * Project: meal-planner
 * Description:
 * [Add brief description here]
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/16
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class RecipeCatalogJsonValidationTest {

    @Test
    void blankRecipeName_throws(@TempDir Path tmp) throws IOException {
        String json = """
        { "recipes": [ { "name": "   ", "ingredients": [] } ] }
        """;
        Path f = tmp.resolve("bad.json");
        Files.writeString(f, json);
        assertThrows(IllegalArgumentException.class, () -> RecipeCatalogJson.fromFile(f));
    }

    @Test
    void blankIngredientName_throws(@TempDir Path tmp) throws IOException {
        String json = """
        { "recipes": [ { "name": "Fried Rice",
          "ingredients": [ { "name": "  ", "amount": 1, "unit": "PCS" } ] } ] }
        """;
        Path f = tmp.resolve("bad2.json");
        Files.writeString(f, json);
        assertThrows(IllegalArgumentException.class, () -> RecipeCatalogJson.fromFile(f));
    }

    @Test
    void negativeOrNaNAmount_throws(@TempDir Path tmp) throws IOException {
        String json = """
        { "recipes": [ { "name": "Soup",
          "ingredients": [ { "name": "water", "amount": -1, "unit": "ML" } ] } ] }
        """;
        Path f = tmp.resolve("bad3.json");
        Files.writeString(f, json);
        assertThrows(IllegalArgumentException.class, () -> RecipeCatalogJson.fromFile(f));
    }
}