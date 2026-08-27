@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.mealplannerapp.ui.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealplannerapp.data.repository.MealPlanRepository
import com.example.mealplannerapp.domain.DayNutrition
import com.example.mealplannerapp.domain.NutritionCalculator
import com.example.mealplannerapp.domain.startOfWeek
import com.example.mealplannerapp.domain.todayLocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

class NutritionViewModel(private val mealPlanRepository: MealPlanRepository) : ViewModel() {

    private val _selectedWeekStart = MutableStateFlow(todayLocalDate().startOfWeek())
    val selectedWeekStart: StateFlow<LocalDate> = _selectedWeekStart.asStateFlow()

    val dailyTotals: StateFlow<List<DayNutrition>> = _selectedWeekStart
        .flatMapLatest { start ->
            val end = start.plus(6, DateTimeUnit.DAY)
            mealPlanRepository.observePlannedMealDetailsInRange(start, end)
                .map { details -> NutritionCalculator.calculateDailyTotals(start, end, details) }
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
}
