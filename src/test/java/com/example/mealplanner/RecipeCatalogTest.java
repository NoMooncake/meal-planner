/**
 * -----------------------------------------------------------------------------
 * File Name: RecipeCatalogTest.java
 * Project: meal-planner
 * Description:
 *     Unit tests for {@link RecipeCatalog}.
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/12
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RecipeCatalog}.
 */
public class RecipeCatalogTest {

    @Test
    void samplesCatalogIsNonEmptyAndContainsKnownRecipes() {
        RecipeCatalog catalog = RecipeCatalog.samples();

        assertTrue(catalog.size() >= 4);
        var names = catalog.all().stream().map(Recipe::name).toList();
        assertTrue(names.contains("Eggs"));
        assertTrue(names.contains("Pasta"));
    }
}
