package com.example.mealplannerapp.data.repository

import com.example.mealplannerapp.data.local.MealSlot
import com.example.mealplannerapp.data.local.PlannedMeal
import com.example.mealplannerapp.data.local.PlannedMealDao
import com.example.mealplannerapp.data.local.PlannedMealWithRecipe
import com.example.mealplannerapp.data.local.RecipeDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class MealPlanRepository(
    private val plannedMealDao: PlannedMealDao,
    private val recipeDao: RecipeDao
) {

    fun observePlannedMealsInRange(start: LocalDate, end: LocalDate): Flow<List<PlannedMealWithRecipe>> =
        plannedMealDao.observePlannedMealsInRange(start.toEpochDays(), end.toEpochDays())

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observePlannedMealDetailsInRange(start: LocalDate, end: LocalDate): Flow<List<PlannedMealDetail>> =
        observePlannedMealsInRange(start, end).flatMapLatest { plannedMeals ->
            val recipeIds = plannedMeals.map { it.recipe.id }.distinct()
            if (recipeIds.isEmpty()) {
                flowOf(emptyList())
            } else {
                recipeDao.observeIngredientsForRecipeIds(recipeIds).map { ingredients ->
                    val ingredientsByRecipe = ingredients.groupBy { it.recipeId }
                    plannedMeals.map { entry ->
                        PlannedMealDetail(
                            plannedMeal = entry.plannedMeal,
                            recipe = entry.recipe,
                            ingredients = ingredientsByRecipe[entry.recipe.id].orEmpty()
                        )
                    }
                }
            }
        }

    suspend fun addPlannedMeal(date: LocalDate, slot: MealSlot, recipeId: Long, servings: Double) {
        plannedMealDao.upsert(
            PlannedMeal(
                dateEpochDay = date.toEpochDays(),
                mealSlot = slot,
                recipeId = recipeId,
                servings = servings
            )
        )
    }

    suspend fun removePlannedMeal(plannedMeal: PlannedMeal) = plannedMealDao.delete(plannedMeal)
}
