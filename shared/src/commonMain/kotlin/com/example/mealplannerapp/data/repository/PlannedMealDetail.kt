package com.example.mealplannerapp.data.repository

import com.example.mealplannerapp.data.local.Ingredient
import com.example.mealplannerapp.data.local.PlannedMeal
import com.example.mealplannerapp.data.local.Recipe

data class PlannedMealDetail(
    val plannedMeal: PlannedMeal,
    val recipe: Recipe,
    val ingredients: List<Ingredient>
)
