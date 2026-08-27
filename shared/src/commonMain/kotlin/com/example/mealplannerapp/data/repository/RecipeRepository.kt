package com.example.mealplannerapp.data.repository

import com.example.mealplannerapp.data.local.Ingredient
import com.example.mealplannerapp.data.local.Recipe
import com.example.mealplannerapp.data.local.RecipeDao
import com.example.mealplannerapp.data.local.RecipeWithIngredients
import kotlinx.coroutines.flow.Flow

class RecipeRepository(private val recipeDao: RecipeDao) {

    fun observeAllRecipes(): Flow<List<Recipe>> = recipeDao.observeAll()

    fun observeAllWithIngredients(): Flow<List<RecipeWithIngredients>> =
        recipeDao.observeAllWithIngredients()

    fun observeRecipeWithIngredients(recipeId: Long): Flow<RecipeWithIngredients?> =
        recipeDao.observeWithIngredients(recipeId)

    suspend fun saveRecipe(recipe: Recipe, ingredients: List<Ingredient>): Long {
        val recipeId = recipeDao.upsertRecipe(recipe)
        recipeDao.deleteIngredientsForRecipe(recipeId)
        recipeDao.insertIngredients(
            ingredients.mapIndexed { index, ingredient ->
                ingredient.copy(recipeId = recipeId, sortOrder = index)
            }
        )
        return recipeId
    }

    suspend fun deleteRecipe(recipe: Recipe) = recipeDao.deleteRecipe(recipe)
}
