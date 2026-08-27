package com.example.mealplannerapp.data.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val servings: Int,
    val caloriesPerServing: Double,
    val proteinGramsPerServing: Double,
    val carbsGramsPerServing: Double,
    val fatGramsPerServing: Double,
    val tagsCsv: String = ""
) {
    val tags: List<String>
        get() = tagsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}
