package com.example.mealplannerapp.data.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/** Single-row table (fixed id) holding app-wide user preferences. */
@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey
    val id: Int = SINGLETON_ID,
    val dailyCalorieGoal: Double
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}
