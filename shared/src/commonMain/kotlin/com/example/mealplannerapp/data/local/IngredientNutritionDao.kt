package com.example.mealplannerapp.data.local

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientNutritionDao {
    @Query("SELECT * FROM ingredient_nutrition ORDER BY name ASC")
    fun observeAll(): Flow<List<IngredientNutrition>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: IngredientNutrition): Long

    @Delete
    suspend fun delete(item: IngredientNutrition)
}
