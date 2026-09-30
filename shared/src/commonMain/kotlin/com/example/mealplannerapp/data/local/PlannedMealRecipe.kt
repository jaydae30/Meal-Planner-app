package com.example.mealplannerapp.data.local

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * Junction table to support many-to-many relationship between PlannedMeal and Recipe.
 * Allows a single meal to contain multiple recipes/food items.
 */
@Entity(
    tableName = "planned_meal_recipes",
    foreignKeys = [
        ForeignKey(
            entity = PlannedMeal::class,
            parentColumns = ["id"],
            childColumns = ["plannedMealId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["plannedMealId"]),
        Index(value = ["recipeId"])
    ]
)
data class PlannedMealRecipe(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val plannedMealId: Long,
    val recipeId: Long,
    val servings: Double,
    val sortOrder: Int = 0  // To maintain order of food items in a meal
)
