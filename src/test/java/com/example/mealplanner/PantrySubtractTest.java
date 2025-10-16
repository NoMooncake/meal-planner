/**
 * -----------------------------------------------------------------------------
 * File Name: PantrySubtractTest.java
 * Project: meal-planner
 * Description:
 *     Unit tests for Pantry subtraction in GroceryService.
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

public class PantrySubtractTest {

    /** A meal plan that needs 300 ML milk in total. */
    private static MealPlan planWithMilk300() {
        Recipe r1 = Recipe.of("A", Ingredient.of("Milk", 100, Unit.ML));
        Recipe r2 = Recipe.of("B", Ingredient.of("Milk", 200, Unit.ML));
        return new MealPlan(List.of(
                new MealSlot(0, MealType.LUNCH, r1),
                new MealSlot(0, MealType.DINNER, r2)
        ));
        // total milk need = 300 ML
    }

    @Test
    void subtractsPantryPartial() {
        MealPlan plan = planWithMilk300();
        Pantry pantry = new Pantry().add("milk", 120, Unit.ML);

        ShoppingList list = new GroceryService().buildFrom(plan, pantry);

        assertEquals(1, list.items().size());
        ShoppingListItem item = list.items().get(0);
        assertEquals("milk", item.name());
        assertEquals(Unit.ML, item.unit());
        assertEquals(180.0, item.totalAmount(), 1e-9); // 300 - 120
    }

    @Test
    void ifPantryCoversNeed_itemOmitted() {
        MealPlan plan = planWithMilk300();
        Pantry pantry = new Pantry().add("Milk", 500, Unit.ML);

        ShoppingList list = new GroceryService().buildFrom(plan, pantry);

        assertTrue(list.items().isEmpty()); // no need to buy anything
    }

    @Test
    void differentUnitsDoNotOffset() {
        // Plan needs 100G sugar
        Recipe r = Recipe.of("Weird", Ingredient.of("Sugar", 100, Unit.G));
        MealPlan plan = new MealPlan(List.of(new MealSlot(0, MealType.LUNCH, r)));

        Pantry pantry = new Pantry()
                .add("Sugar", 100, Unit.ML) // different unit, does not offset
                .add("sugar", 40, Unit.G);  // same name, different case

        ShoppingList list = new GroceryService().buildFrom(plan, pantry);

        assertEquals(1, list.items().size());
        ShoppingListItem item = list.items().get(0);
        assertEquals("sugar", item.name());
        assertEquals(Unit.G, item.unit());
        assertEquals(60.0, item.totalAmount(), 1e-9); // 100G - 40G
    }

    @Test
    void pantrySubtract_volumeConversion() {
        Pantry p = new Pantry().add("milk", 1, Unit.L); // = 1000 ML
        Recipe r = Recipe.of("Cereal", Ingredient.of("milk", 250, Unit.ML));
        ShoppingList need = new GroceryService().buildFrom(new MealPlan(
                java.util.List.of(new MealSlot(0, MealType.BREAKFAST, r))), p);
        // 1000 - 250 = 750 ML left in pantry, no need to buy
        assertTrue(need.items().isEmpty());
    }
}