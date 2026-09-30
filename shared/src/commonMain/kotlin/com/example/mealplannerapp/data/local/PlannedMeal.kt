package com.example.mealplannerapp.data.local

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "planned_meals",
    indices = [Index("dateEpochDay")]
)
data class PlannedMeal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateEpochDay: Long,
    val mealSlot: MealSlot
)
