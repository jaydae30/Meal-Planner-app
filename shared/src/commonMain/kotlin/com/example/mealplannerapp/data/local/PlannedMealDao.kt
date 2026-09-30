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
    fun observePlannedMealsInRange(startEpochDay: Long, endEpochDay: Long): Flow<List<PlannedMealWithRecipes>>

    @Transaction
    @Query("SELECT * FROM planned_meals WHERE id = :plannedMealId")
    suspend fun getPlannedMealWithRecipes(plannedMealId: Long): PlannedMealWithRecipes?

    @Query("""
        SELECT r.*, pmr.servings as mealServings, pmr.sortOrder
        FROM recipes r
        INNER JOIN planned_meal_recipes pmr ON r.id = pmr.recipeId
        WHERE pmr.plannedMealId = :plannedMealId
        ORDER BY pmr.sortOrder ASC
    """)
    suspend fun getRecipeDetailsForMeal(plannedMealId: Long): List<RecipeWithServingsEntity>

    @Query("""
        SELECT pmr.plannedMealId, pmr.recipeId, pmr.servings as mealServings, pmr.sortOrder
        FROM planned_meal_recipes pmr
        INNER JOIN planned_meals pm ON pmr.plannedMealId = pm.id
        WHERE pm.dateEpochDay BETWEEN :startEpochDay AND :endEpochDay
    """)
    fun observePlannedMealRecipesInRange(startEpochDay: Long, endEpochDay: Long): Flow<List<PlannedMealRecipeInfo>>

    @Query("SELECT * FROM planned_meal_recipes WHERE plannedMealId = :plannedMealId ORDER BY sortOrder ASC")
    fun observePlannedMealRecipes(plannedMealId: Long): Flow<List<PlannedMealRecipe>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(plannedMeal: PlannedMeal): Long

    @Delete
    suspend fun delete(plannedMeal: PlannedMeal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlannedMealRecipe(plannedMealRecipe: PlannedMealRecipe): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlannedMealRecipes(plannedMealRecipes: List<PlannedMealRecipe>)

    @Delete
    suspend fun deletePlannedMealRecipe(plannedMealRecipe: PlannedMealRecipe)

    @Query("DELETE FROM planned_meal_recipes WHERE plannedMealId = :plannedMealId")
    suspend fun deleteAllRecipesForMeal(plannedMealId: Long)

    @Query("DELETE FROM planned_meal_recipes WHERE plannedMealId = :plannedMealId AND recipeId = :recipeId")
    suspend fun deleteRecipeFromMeal(plannedMealId: Long, recipeId: Long)

    // Meal Ingredient methods
    @Query("SELECT * FROM meal_ingredients WHERE plannedMealId = :plannedMealId ORDER BY sortOrder ASC")
    fun observeMealIngredients(plannedMealId: Long): Flow<List<MealIngredient>>

    @Query("""
        SELECT mi.* FROM meal_ingredients mi
        INNER JOIN planned_meals pm ON mi.plannedMealId = pm.id
        WHERE pm.dateEpochDay BETWEEN :startEpochDay AND :endEpochDay
        ORDER BY mi.sortOrder ASC
    """)
    fun observeMealIngredientsInRange(startEpochDay: Long, endEpochDay: Long): Flow<List<MealIngredient>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealIngredient(ingredient: MealIngredient): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealIngredients(ingredients: List<MealIngredient>)

    @Delete
    suspend fun deleteMealIngredient(ingredient: MealIngredient)

    @Query("DELETE FROM meal_ingredients WHERE plannedMealId = :plannedMealId")
    suspend fun deleteAllIngredientsForMeal(plannedMealId: Long)

    @Query("DELETE FROM meal_ingredients WHERE id = :ingredientId")
    suspend fun deleteMealIngredientById(ingredientId: Long)
}

/**
 * Helper data class for query results that include servings and sort order
 */
data class RecipeWithServingsEntity(
    val id: Long,
    val name: String,
    val servings: Int,  // Base servings from recipe
    val mealServings: Double,  // Servings for this specific meal
    val sortOrder: Int,
    val caloriesPerServing: Double,
    val proteinGramsPerServing: Double,
    val carbsGramsPerServing: Double,
    val fatGramsPerServing: Double,
    val tagsCsv: String
)

/**
 * Helper data class for planned meal recipe info queries
 */
data class PlannedMealRecipeInfo(
    val plannedMealId: Long,
    val recipeId: Long,
    val mealServings: Double,
    val sortOrder: Int
)
