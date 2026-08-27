package com.example.mealplannerapp.data.local

import androidx.room3.Embedded
import androidx.room3.Relation

data class PlannedMealWithRecipe(
    @Embedded
    val plannedMeal: PlannedMeal,
    @Relation(parentColumns = ["recipeId"], entityColumns = ["id"])
    val recipe: Recipe
)
