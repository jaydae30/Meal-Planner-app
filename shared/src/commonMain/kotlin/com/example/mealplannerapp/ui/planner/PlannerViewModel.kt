@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.mealplannerapp.ui.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealplannerapp.data.local.IngredientNutrition
import com.example.mealplannerapp.data.local.MealSlot
import com.example.mealplannerapp.data.local.PlannedMeal
import com.example.mealplannerapp.data.local.PlannedMealWithRecipeServings
import com.example.mealplannerapp.data.local.Recipe
import com.example.mealplannerapp.data.repository.IngredientNutritionRepository
import com.example.mealplannerapp.data.repository.MealPlanRepository
import com.example.mealplannerapp.data.repository.RecipeRepository
import com.example.mealplannerapp.data.repository.RecipeServingInfo
import com.example.mealplannerapp.data.repository.SettingsRepository
import com.example.mealplannerapp.domain.startOfWeek
import com.example.mealplannerapp.domain.todayLocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

class PlannerViewModel(
    private val mealPlanRepository: MealPlanRepository,
    private val recipeRepository: RecipeRepository,
    private val settingsRepository: SettingsRepository,
    private val ingredientNutritionRepository: IngredientNutritionRepository
) : ViewModel() {

    private val _selectedWeekStart = MutableStateFlow(todayLocalDate().startOfWeek())
    val selectedWeekStart: StateFlow<LocalDate> = _selectedWeekStart.asStateFlow()

    val recipes: StateFlow<List<Recipe>> = recipeRepository.observeAllRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val storedIngredients: StateFlow<List<IngredientNutrition>> = ingredientNutritionRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyCalorieGoal: StateFlow<Double> = settingsRepository.observeDailyCalorieGoal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_DAILY_CALORIE_GOAL)

    val plannedMeals: StateFlow<List<PlannedMealWithRecipeServings>> = _selectedWeekStart
        .flatMapLatest { start ->
            mealPlanRepository.observePlannedMealsWithServingsInRange(start, start.plus(6, DateTimeUnit.DAY))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun goToPreviousWeek() {
        _selectedWeekStart.value = _selectedWeekStart.value.minus(7, DateTimeUnit.DAY)
    }

    fun goToNextWeek() {
        _selectedWeekStart.value = _selectedWeekStart.value.plus(7, DateTimeUnit.DAY)
    }

    fun goToThisWeek() {
        _selectedWeekStart.value = todayLocalDate().startOfWeek()
    }

    /**
     * Adds a single meal (recipe) to a date/slot.
     * For adding multiple recipes to one meal, use addMealWithMultipleRecipes instead.
     */
    fun addMeal(date: LocalDate, slot: MealSlot, recipeId: Long, servings: Double) {
        viewModelScope.launch {
            mealPlanRepository.addPlannedMealWithRecipes(
                date = date,
                slot = slot,
                recipes = listOf(RecipeServingInfo(recipeId, servings))
            )
        }
    }

    /**
     * Adds a meal with multiple recipes to a date/slot.
     */
    fun addMealWithMultipleRecipes(date: LocalDate, slot: MealSlot, recipes: List<RecipeServingInfo>) {
        viewModelScope.launch {
            mealPlanRepository.addPlannedMealWithRecipes(date, slot, recipes)
        }
    }

    fun removeMeal(plannedMeal: PlannedMeal) {
        viewModelScope.launch {
            mealPlanRepository.removePlannedMeal(plannedMeal)
        }
    }

    fun setDailyCalorieGoal(goal: Double) {
        viewModelScope.launch {
            settingsRepository.setDailyCalorieGoal(goal)
        }
    }

    /**
     * Adds a stored ingredient to a meal with a specific quantity.
     * Calculates the nutritional values based on the quantity.
     */
    fun addStoredIngredient(
        date: LocalDate,
        slot: MealSlot,
        ingredientNutrition: IngredientNutrition,
        quantity: Double
    ) {
        viewModelScope.launch {
            // Calculate calories per unit based on the ingredient's serving size
            val ratio = quantity / ingredientNutrition.servingQuantity
            val totalCalories = ingredientNutrition.caloriesPerServing * ratio
            val totalProtein = ingredientNutrition.proteinGramsPerServing * ratio
            val totalCarbs = ingredientNutrition.carbsGramsPerServing * ratio
            val totalFat = ingredientNutrition.fatGramsPerServing * ratio

            // Store as calories per unit (so 1 unit = the quantity entered)
            val caloriesPerUnit = totalCalories / quantity
            val proteinPerUnit = totalProtein / quantity
            val carbsPerUnit = totalCarbs / quantity
            val fatPerUnit = totalFat / quantity

            mealPlanRepository.addPlannedMealWithIngredient(
                date = date,
                slot = slot,
                name = ingredientNutrition.name,
                quantity = quantity,
                unit = ingredientNutrition.servingUnit,
                caloriesPerUnit = caloriesPerUnit,
                proteinGramsPerUnit = proteinPerUnit,
                carbsGramsPerUnit = carbsPerUnit,
                fatGramsPerUnit = fatPerUnit
            )
        }
    }
}
