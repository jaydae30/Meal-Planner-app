package com.example.mealplannerapp.data.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * A reusable "nutrition facts" entry for one ingredient (e.g. "chicken breast: 165 kcal per 100 g"),
 * independent of any particular recipe. Recipes look these up by (name, unit) to auto-calculate
 * their own per-serving nutrition — see RecipeNutritionCalculator.
 */
@Entity(tableName = "ingredient_nutrition")
data class IngredientNutrition(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val servingQuantity: Double,
    val servingUnit: String,
    val caloriesPerServing: Double,
    val proteinGramsPerServing: Double,
    val carbsGramsPerServing: Double,
    val fatGramsPerServing: Double
)
