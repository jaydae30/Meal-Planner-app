package com.example.mealplannerapp.data.repository

import com.example.mealplannerapp.data.local.Ingredient
import com.example.mealplannerapp.data.local.MealIngredient
import com.example.mealplannerapp.data.local.PlannedMeal
import com.example.mealplannerapp.data.local.Recipe

data class PlannedMealDetail(
    val plannedMeal: PlannedMeal,
    val recipes: List<RecipeDetail>,
    val mealIngredients: List<MealIngredient> = emptyList()
)

data class RecipeDetail(
    val recipe: Recipe,
    val ingredients: List<Ingredient>,
    val servings: Double  // Servings for this specific recipe in this meal
)
