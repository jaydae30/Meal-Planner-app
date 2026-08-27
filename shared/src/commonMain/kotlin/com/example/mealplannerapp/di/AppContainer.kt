package com.example.mealplannerapp.di

import com.example.mealplannerapp.data.local.MealPlannerDatabase
import com.example.mealplannerapp.data.repository.IngredientNutritionRepository
import com.example.mealplannerapp.data.repository.MealPlanRepository
import com.example.mealplannerapp.data.repository.RecipeRepository
import com.example.mealplannerapp.data.repository.SettingsRepository

class AppContainer(database: MealPlannerDatabase) {
    val recipeRepository: RecipeRepository = RecipeRepository(database.recipeDao())
    val mealPlanRepository: MealPlanRepository =
        MealPlanRepository(database.plannedMealDao(), database.recipeDao())
    val settingsRepository: SettingsRepository = SettingsRepository(database.settingsDao())
    val ingredientNutritionRepository: IngredientNutritionRepository =
        IngredientNutritionRepository(database.ingredientNutritionDao())
}
