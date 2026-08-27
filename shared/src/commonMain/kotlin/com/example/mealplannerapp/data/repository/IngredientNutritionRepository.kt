package com.example.mealplannerapp.data.repository

import com.example.mealplannerapp.data.local.IngredientNutrition
import com.example.mealplannerapp.data.local.IngredientNutritionDao
import kotlinx.coroutines.flow.Flow

class IngredientNutritionRepository(private val dao: IngredientNutritionDao) {
    fun observeAll(): Flow<List<IngredientNutrition>> = dao.observeAll()

    suspend fun save(item: IngredientNutrition) {
        dao.upsert(item)
    }

    suspend fun delete(item: IngredientNutrition) {
        dao.delete(item)
    }
}
