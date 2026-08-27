package com.example.mealplannerapp.data.local

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PlannedMealDao {
    @Transaction
    @Query("SELECT * FROM planned_meals WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY dateEpochDay ASC")
    fun observePlannedMealsInRange(startEpochDay: Long, endEpochDay: Long): Flow<List<PlannedMealWithRecipe>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(plannedMeal: PlannedMeal): Long

    @Delete
    suspend fun delete(plannedMeal: PlannedMeal)
}
