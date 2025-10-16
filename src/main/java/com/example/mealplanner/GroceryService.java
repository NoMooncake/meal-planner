/**
 * -----------------------------------------------------------------------------
 * File Name: GroceryService.java
 * Project: meal-planner
 * Description:
 *      Service for generating a shopping list from a meal plan.
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/12
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner;

import java.util.ArrayList;
import java.util.List;

/**
 * Facade service that produces a {@link ShoppingList} from a {@link MealPlan}.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Aggregate all ingredients of the recipes present in a plan.</li>
 *   <li>Optionally subtract the user's {@link Pantry} stock to compute the remaining items to buy.</li>
 * </ul>
 * Notes:
 * <ul>
 *   <li>Aggregation and subtraction are strictly based on the identity (normalized name, unit);
 *       no unit conversion is performed in the MVP (e.g., G and KG are distinct).</li>
 *   <li>The service does not mutate inputs.</li>
 * </ul>
 * This class plays the Facade role in the design, delegating aggregation to
 * {@link ShoppingListBuilder} and coordinating optional pantry subtraction.
 *
 * @since 1.0
 */
public class GroceryService {

    /**
     * Builds a shopping list that contains the total amount required for the given plan,
     * without considering any pantry stock.
     *
     * <p>Implementation detail: delegates aggregation to {@link ShoppingListBuilder} by
     * visiting all {@link MealSlot}s in the plan and adding their recipes.</p>
     *
     * @param plan non-{@code null} {@link MealPlan} whose slots contribute ingredients
     * @return an immutable {@link ShoppingList} aggregated by (name, unit)
     * @throws NullPointerException if {@code plan} is {@code null}
     */
    public ShoppingList buildFrom(MealPlan plan) {
        ShoppingListBuilder builder = new ShoppingListBuilder();
        for (MealSlot slot : plan.slots()) {
            builder.addRecipe(slot.recipe());
        }
        return builder.build();
    }

    /**
     * Builds a shopping list for the given plan and subtracts the amounts available in
     * the provided {@link Pantry}, returning only the items that still need to be bought.
     *
     * <p>Behavior:</p>
     * <ul>
     *   <li>Subtraction is performed item-wise by (name, unit). Units must match exactly;
     *       no unit conversion takes place.</li>
     *   <li>If the pantry covers an item fully (or more), that item is omitted from the result.</li>
     *   <li>A small epsilon is used when comparing floating-point remainders to avoid negative
     *       values due to rounding error.</li>
     * </ul>
     *
     * @param plan   non-{@code null} {@link MealPlan}
     * @param pantry non-{@code null} {@link Pantry} representing current stock
     * @return a {@link ShoppingList} containing only the remaining amounts to purchase
     * @throws NullPointerException if {@code plan} or {@code pantry} is {@code null}
     */
    public ShoppingList buildFrom(MealPlan plan, Pantry pantry) {
        // First, get the full shopping list needed for the meal plan
        ShoppingList need = buildFrom(plan);

        List<ShoppingListItem> remaining = new ArrayList<>();
        for (ShoppingListItem item : need.items()) {
            double have = pantry.amountOf(item.name(), item.unit());
            double buy = item.totalAmount() - have;
            if (buy > 0.0000001) { // consider floating point precision
                remaining.add(new ShoppingListItem(item.name(), item.unit(), buy));
            }
        }
        return new ShoppingList(remaining);
    }
}