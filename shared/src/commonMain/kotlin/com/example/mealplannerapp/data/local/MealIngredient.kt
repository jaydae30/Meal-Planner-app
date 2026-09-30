package com.example.mealplannerapp.data.local

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * Represents an ingredient added directly to a meal (not part of a recipe).
 * This allows users to add simple food items like "apple", "protein shake", etc.
 * without creating a full recipe.
 */
@Entity(
    tableName = "meal_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = PlannedMeal::class,
            parentColumns = ["id"],
            childColumns = ["plannedMealId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("plannedMealId")]
)
data class MealIngredient(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val plannedMealId: Long,
    val name: String,
    val quantity: Double,
    val unit: String,
    val caloriesPerUnit: Double = 0.0,
    val proteinGramsPerUnit: Double = 0.0,
    val carbsGramsPerUnit: Double = 0.0,
    val fatGramsPerUnit: Double = 0.0,
    val sortOrder: Int = 0
)
