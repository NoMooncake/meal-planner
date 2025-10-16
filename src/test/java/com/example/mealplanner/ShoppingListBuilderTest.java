/**
 * -----------------------------------------------------------------------------
 * File Name: ShoppingListBuilderTest.java
 * Project: meal-planner
 * Description:
 *     Unit tests for ShoppingListBuilder.
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/12
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ShoppingListBuilderTest {

    @Test
    void aggregatesSameNameSameUnit() {
        Recipe r1 = Recipe.of("Scrambled Egg",
                Ingredient.of("Egg", 2, Unit.PCS),
                Ingredient.of("Milk", 100, Unit.ML));

        Recipe r2 = Recipe.of("Omelette",
                Ingredient.of(" egg  ", 3, Unit.PCS),  // name with spaces and different case
                Ingredient.of("Milk", 50, Unit.ML));

        ShoppingList list = new ShoppingListBuilder()
                .addRecipe(r1)
                .addRecipe(r2)
                .build();

        // transform to easy-to-check map
        var map = list.items().stream()
                .collect(java.util.stream.Collectors.toMap(
                        i -> i.name() + "|" + i.unit(),
                        ShoppingListItem::totalAmount));

        assertEquals(5.0, map.get("egg|PCS"));   // 2 + 3
        assertEquals(150.0, map.get("milk|ML")); // 100 + 50
        assertEquals(2, map.size());
    }

    @Test
    void differentUnitsDoNotMerge() {
        Recipe r = Recipe.of("Weird Recipe",
                Ingredient.of("Sugar", 100, Unit.G),
                Ingredient.of("Sugar", 100, Unit.ML)); // same name, different units

        ShoppingList list = new ShoppingListBuilder().addRecipe(r).build();

        assertEquals(2, list.items().size());
        // transform to easy-to-check keys
        var keys = list.items().stream().map(i -> i.name() + "|" + i.unit()).toList();
        assertTrue(keys.contains("sugar|G"));
        assertTrue(keys.contains("sugar|ML"));
    }

    @Test
    void mergeDifferentUnits_mass() {
        Recipe r1 = Recipe.of("A", Ingredient.of("pasta", 0.5, Unit.KG));
        Recipe r2 = Recipe.of("B", Ingredient.of("pasta", 200, Unit.G));
        ShoppingList list = new ShoppingListBuilder().addRecipe(r1).addRecipe(r2).build();
        assertEquals(1, list.items().size());
        var item = list.items().get(0);
        assertEquals("pasta", item.name());
        assertEquals(Unit.G, item.unit());
        assertEquals(700.0, item.totalAmount(), 1e-9);
    }
}
