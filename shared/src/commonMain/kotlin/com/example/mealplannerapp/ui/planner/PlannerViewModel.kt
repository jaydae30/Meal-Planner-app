@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.mealplannerapp.ui.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealplannerapp.data.local.MealSlot
import com.example.mealplannerapp.data.local.PlannedMeal
import com.example.mealplannerapp.data.local.PlannedMealWithRecipe
import com.example.mealplannerapp.data.local.Recipe
import com.example.mealplannerapp.data.repository.MealPlanRepository
import com.example.mealplannerapp.data.repository.RecipeRepository
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
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _selectedWeekStart = MutableStateFlow(todayLocalDate().startOfWeek())
    val selectedWeekStart: StateFlow<LocalDate> = _selectedWeekStart.asStateFlow()

    val recipes: StateFlow<List<Recipe>> = recipeRepository.observeAllRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyCalorieGoal: StateFlow<Double> = settingsRepository.observeDailyCalorieGoal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_DAILY_CALORIE_GOAL)

    val plannedMeals: StateFlow<List<PlannedMealWithRecipe>> = _selectedWeekStart
        .flatMapLatest { start ->
            mealPlanRepository.observePlannedMealsInRange(start, start.plus(6, DateTimeUnit.DAY))
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

    fun addMeal(date: LocalDate, slot: MealSlot, recipeId: Long, servings: Double) {
        viewModelScope.launch {
            mealPlanRepository.addPlannedMeal(date, slot, recipeId, servings)
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
}
