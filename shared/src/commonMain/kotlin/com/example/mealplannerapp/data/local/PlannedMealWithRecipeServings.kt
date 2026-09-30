package com.example.mealplannerapp.data.local

/**
 * UI-friendly representation of a planned meal with all recipe, servings, and ingredient information
 */
data class PlannedMealWithRecipeServings(
    val plannedMeal: PlannedMeal,
    val recipeServings: List<RecipeServing>,
    val mealIngredients: List<MealIngredient>
)

data class RecipeServing(
    val recipe: Recipe,
    val servings: Double
)
