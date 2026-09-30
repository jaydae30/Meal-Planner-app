package com.example.mealplannerapp.data.local

import androidx.room3.Embedded
import androidx.room3.Junction
import androidx.room3.Relation

/**
 * Data class representing a planned meal with all its associated recipes/food items.
 */
data class PlannedMealWithRecipes(
    @Embedded
    val plannedMeal: PlannedMeal,

    @Relation(
        parentColumns = ["id"],
        entityColumns = ["id"],
        associateBy = Junction(
            value = PlannedMealRecipe::class,
            parentColumns = ["plannedMealId"],
            entityColumns = ["recipeId"]
        )
    )
    val recipes: List<Recipe>
)

/**
 * Data class representing a recipe with its servings and sort order for a specific meal.
 */
data class RecipeWithServings(
    val recipe: Recipe,
    val servings: Double,
    val sortOrder: Int
)

/**
 * Enhanced data class that includes servings information for each recipe.
 */
data class PlannedMealWithRecipeDetails(
    val plannedMeal: PlannedMeal,
    val recipeDetails: List<RecipeWithServings>
)
