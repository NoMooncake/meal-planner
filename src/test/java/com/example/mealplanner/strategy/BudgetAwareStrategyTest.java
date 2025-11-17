/**
 * -----------------------------------------------------------------------------
 * File Name: BudgetAwareStrategyTest.java
 * Project: meal-planner
 * Description:
 * [Add brief description here]
 * <p>
 * Author: Yue Wu
 * Date: 2025/11/17
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner.strategy;

import com.example.mealplanner.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BudgetAwareStrategyTest {

    private static Recipe makeSingle(String name, String ingName, double amount, Unit unit) {
        return Recipe.of(name, Ingredient.of(ingName, amount, unit));
    }

    @Test
    void choosesCheaperRecipeWhenBudgetLow() {
        // cheap: Rice 100g, expensive: Chicken 200g
        Recipe cheap = makeSingle("Rice Bowl", "Rice", 100, Unit.G);
        Recipe expensive = makeSingle("Chicken Plate", "Chicken", 200, Unit.G);

        PriceBook prices = new PriceBook()
                .add("Rice", Unit.G, 0.01)      // 1.0 total
                .add("Chicken", Unit.G, 0.05);  // 10.0 total

        BudgetAwareStrategy s = new BudgetAwareStrategy(prices, 3.0); // budget too small for chicken
        MealPlan plan = s.generatePlan(1, new MealType[]{MealType.LUNCH}, List.of(cheap, expensive));

        assertEquals("Rice Bowl", plan.slots().get(0).recipe().name());
    }

    @Test
    void canChooseExpensiveRecipeWhenBudgetHigh() {
        Recipe cheap = makeSingle("Rice Bowl", "Rice", 100, Unit.G);
        Recipe expensive = makeSingle("Chicken Plate", "Chicken", 200, Unit.G);

        PriceBook prices = new PriceBook()
                .add("Rice", Unit.G, 0.01)      // 1.0
                .add("Chicken", Unit.G, 0.05);  // 10.0

        BudgetAwareStrategy s = new BudgetAwareStrategy(prices, 20.0); // enough budget
        MealPlan plan = s.generatePlan(1, new MealType[]{MealType.LUNCH}, List.of(cheap, expensive));

        assertEquals("Chicken Plate", plan.slots().get(0).recipe().name());
    }
}
