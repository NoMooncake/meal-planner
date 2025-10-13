/**
 * -----------------------------------------------------------------------------
 * File Name: MealPlanStrategy.java
 * Project: meal-planner
 * Description:
 *      Interface for meal plan generation strategies.
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/12
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner.strategy;

import com.example.mealplanner.MealPlan;
import com.example.mealplanner.MealType;
import com.example.mealplanner.Recipe;

import java.util.List;

public interface MealPlanStrategy {
    /**
     * Generate a meal plan.
     */
    MealPlan generatePlan(int days, MealType[] mealsPerDay, List<Recipe> catalog);
}