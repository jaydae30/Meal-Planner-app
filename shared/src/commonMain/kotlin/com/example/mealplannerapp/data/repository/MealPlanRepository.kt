package com.example.mealplannerapp.data.repository

import com.example.mealplannerapp.data.local.MealIngredient
import com.example.mealplannerapp.data.local.MealSlot
import com.example.mealplannerapp.data.local.PlannedMeal
import com.example.mealplannerapp.data.local.PlannedMealDao
import com.example.mealplannerapp.data.local.PlannedMealRecipe
import com.example.mealplannerapp.data.local.PlannedMealWithRecipes
import com.example.mealplannerapp.data.local.PlannedMealWithRecipeServings
import com.example.mealplannerapp.data.local.Recipe
import com.example.mealplannerapp.data.local.RecipeDao
import com.example.mealplannerapp.data.local.RecipeServing
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

    fun observePlannedMealsInRange(start: LocalDate, end: LocalDate): Flow<List<PlannedMealWithRecipes>> =
        plannedMealDao.observePlannedMealsInRange(start.toEpochDays(), end.toEpochDays())

    /**
     * Observes planned meals with servings information for UI display
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observePlannedMealsWithServingsInRange(start: LocalDate, end: LocalDate): Flow<List<PlannedMealWithRecipeServings>> =
        observePlannedMealsInRange(start, end).flatMapLatest { plannedMeals ->
            if (plannedMeals.isEmpty()) {
                flowOf(emptyList())
            } else {
                plannedMealDao.observePlannedMealRecipesInRange(start.toEpochDays(), end.toEpochDays()).flatMapLatest { mealRecipeInfos ->
                    val servingsByMeal = mealRecipeInfos.groupBy { it.plannedMealId }

                    // Also fetch meal ingredients
                    plannedMealDao.observeMealIngredientsInRange(start.toEpochDays(), end.toEpochDays()).map { allIngredients ->
                        val ingredientsByMeal = allIngredients.groupBy { it.plannedMealId }

                        plannedMeals.map { mealWithRecipes ->
                            val servingsInfo = servingsByMeal[mealWithRecipes.plannedMeal.id].orEmpty()
                            val recipeServings = mealWithRecipes.recipes.mapNotNull { recipe ->
                                val servings = servingsInfo.find { it.recipeId == recipe.id }?.mealServings
                                servings?.let { RecipeServing(recipe, it) }
                            }

                            PlannedMealWithRecipeServings(
                                plannedMeal = mealWithRecipes.plannedMeal,
                                recipeServings = recipeServings,
                                mealIngredients = ingredientsByMeal[mealWithRecipes.plannedMeal.id].orEmpty()
                            )
                        }
                    }
                }
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observePlannedMealDetailsInRange(start: LocalDate, end: LocalDate): Flow<List<PlannedMealDetail>> =
        observePlannedMealsInRange(start, end).flatMapLatest { plannedMeals ->
            val recipeIds = plannedMeals.flatMap { it.recipes.map { recipe -> recipe.id } }.distinct()
            if (recipeIds.isEmpty() && plannedMeals.isEmpty()) {
                flowOf(emptyList())
            } else {
                // Get ingredients for all recipes
                val ingredientsFlow = if (recipeIds.isNotEmpty()) {
                    recipeDao.observeIngredientsForRecipeIds(recipeIds)
                } else {
                    flowOf(emptyList())
                }

                ingredientsFlow.flatMapLatest { ingredients ->
                    val ingredientsByRecipe = ingredients.groupBy { it.recipeId }

                    // Get servings information for all planned meal recipes in range
                    plannedMealDao.observePlannedMealRecipesInRange(start.toEpochDays(), end.toEpochDays()).flatMapLatest { mealRecipeInfos ->
                        // Group servings info by planned meal ID
                        val servingsByMeal = mealRecipeInfos.groupBy { it.plannedMealId }

                        // Get meal ingredients
                        plannedMealDao.observeMealIngredientsInRange(start.toEpochDays(), end.toEpochDays()).map { allMealIngredients ->
                            val mealIngredientsByMealId = allMealIngredients.groupBy { it.plannedMealId }

                            plannedMeals.map { entry ->
                                val servingsForThisMeal = servingsByMeal[entry.plannedMeal.id]?.associateBy { it.recipeId }.orEmpty()

                                PlannedMealDetail(
                                    plannedMeal = entry.plannedMeal,
                                    recipes = entry.recipes.map { recipe ->
                                        RecipeDetail(
                                            recipe = recipe,
                                            ingredients = ingredientsByRecipe[recipe.id].orEmpty(),
                                            servings = servingsForThisMeal[recipe.id]?.mealServings ?: 1.0
                                        )
                                    },
                                    mealIngredients = mealIngredientsByMealId[entry.plannedMeal.id].orEmpty()
                                )
                            }
                        }
                    }
                }
            }
        }

    /**
     * Creates a new planned meal with multiple recipes
     * @param date The date for the meal
     * @param slot The meal slot (breakfast, lunch, dinner, snack)
     * @param recipes List of recipe IDs with their respective servings
     */
    suspend fun addPlannedMealWithRecipes(
        date: LocalDate,
        slot: MealSlot,
        recipes: List<RecipeServingInfo>
    ) {
        val plannedMealId = plannedMealDao.upsert(
            PlannedMeal(
                dateEpochDay = date.toEpochDays(),
                mealSlot = slot
            )
        )

        val plannedMealRecipes = recipes.mapIndexed { index, recipeInfo ->
            PlannedMealRecipe(
                plannedMealId = plannedMealId,
                recipeId = recipeInfo.recipeId,
                servings = recipeInfo.servings,
                sortOrder = index
            )
        }

        plannedMealDao.insertPlannedMealRecipes(plannedMealRecipes)
    }

    /**
     * Adds a single recipe to an existing planned meal
     */
    suspend fun addRecipeToMeal(plannedMealId: Long, recipeId: Long, servings: Double) {
        plannedMealDao.insertPlannedMealRecipe(
            PlannedMealRecipe(
                plannedMealId = plannedMealId,
                recipeId = recipeId,
                servings = servings,
                sortOrder = 0
            )
        )
    }

    /**
     * Removes a specific recipe from a planned meal
     */
    suspend fun removeRecipeFromMeal(plannedMealId: Long, recipeId: Long) {
        plannedMealDao.deleteRecipeFromMeal(plannedMealId, recipeId)
    }

    /**
     * Adds a standalone ingredient to an existing planned meal
     */
    suspend fun addIngredientToMeal(
        plannedMealId: Long,
        name: String,
        quantity: Double,
        unit: String,
        caloriesPerUnit: Double = 0.0,
        proteinGramsPerUnit: Double = 0.0,
        carbsGramsPerUnit: Double = 0.0,
        fatGramsPerUnit: Double = 0.0
    ) {
        plannedMealDao.insertMealIngredient(
            MealIngredient(
                plannedMealId = plannedMealId,
                name = name,
                quantity = quantity,
                unit = unit,
                caloriesPerUnit = caloriesPerUnit,
                proteinGramsPerUnit = proteinGramsPerUnit,
                carbsGramsPerUnit = carbsGramsPerUnit,
                fatGramsPerUnit = fatGramsPerUnit,
                sortOrder = 0
            )
        )
    }

    /**
     * Adds multiple standalone ingredients to an existing planned meal
     */
    suspend fun addIngredientsToMeal(plannedMealId: Long, ingredients: List<MealIngredient>) {
        val ingredientsWithMealId = ingredients.mapIndexed { index, ingredient ->
            ingredient.copy(plannedMealId = plannedMealId, sortOrder = index)
        }
        plannedMealDao.insertMealIngredients(ingredientsWithMealId)
    }

    /**
     * Removes a specific ingredient from a planned meal
     */
    suspend fun removeIngredientFromMeal(ingredientId: Long) {
        plannedMealDao.deleteMealIngredientById(ingredientId)
    }

    /**
     * Creates a new planned meal with a standalone ingredient
     * @param date The date for the meal
     * @param slot The meal slot (breakfast, lunch, dinner, snack)
     * @param name The ingredient name
     * @param quantity The quantity
     * @param unit The unit of measurement
     * @param caloriesPerUnit Calories per unit
     * @param proteinGramsPerUnit Protein grams per unit (optional)
     * @param carbsGramsPerUnit Carbs grams per unit (optional)
     * @param fatGramsPerUnit Fat grams per unit (optional)
     */
    suspend fun addPlannedMealWithIngredient(
        date: LocalDate,
        slot: MealSlot,
        name: String,
        quantity: Double,
        unit: String,
        caloriesPerUnit: Double = 0.0,
        proteinGramsPerUnit: Double = 0.0,
        carbsGramsPerUnit: Double = 0.0,
        fatGramsPerUnit: Double = 0.0
    ) {
        val plannedMealId = plannedMealDao.upsert(
            PlannedMeal(
                dateEpochDay = date.toEpochDays(),
                mealSlot = slot
            )
        )

        plannedMealDao.insertMealIngredient(
            MealIngredient(
                plannedMealId = plannedMealId,
                name = name,
                quantity = quantity,
                unit = unit,
                caloriesPerUnit = caloriesPerUnit,
                proteinGramsPerUnit = proteinGramsPerUnit,
                carbsGramsPerUnit = carbsGramsPerUnit,
                fatGramsPerUnit = fatGramsPerUnit,
                sortOrder = 0
            )
        )
    }

    /**
     * Removes all recipes and the planned meal itself
     */
    suspend fun removePlannedMeal(plannedMeal: PlannedMeal) = plannedMealDao.delete(plannedMeal)
}

/**
 * Data class to hold recipe ID and servings information
 */
data class RecipeServingInfo(
    val recipeId: Long,
    val servings: Double
)
