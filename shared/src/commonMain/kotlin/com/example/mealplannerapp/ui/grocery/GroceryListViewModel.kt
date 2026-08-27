@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.mealplannerapp.ui.grocery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealplannerapp.data.repository.MealPlanRepository
import com.example.mealplannerapp.domain.GroceryItem
import com.example.mealplannerapp.domain.GroceryListGenerator
import com.example.mealplannerapp.domain.startOfWeek
import com.example.mealplannerapp.domain.todayLocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

class GroceryListViewModel(private val mealPlanRepository: MealPlanRepository) : ViewModel() {

    private val thisWeekStart = todayLocalDate().startOfWeek()

    private val _rangeStart = MutableStateFlow(thisWeekStart)
    val rangeStart: StateFlow<LocalDate> = _rangeStart.asStateFlow()

    private val _rangeEnd = MutableStateFlow(thisWeekStart.plus(6, DateTimeUnit.DAY))
    val rangeEnd: StateFlow<LocalDate> = _rangeEnd.asStateFlow()

    val groceryItems: StateFlow<List<GroceryItem>> = combine(_rangeStart, _rangeEnd) { start, end -> start to end }
        .flatMapLatest { (start, end) -> mealPlanRepository.observePlannedMealDetailsInRange(start, end) }
        .map { GroceryListGenerator.generate(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _checkedKeys = MutableStateFlow<Set<String>>(emptySet())
    val checkedKeys: StateFlow<Set<String>> = _checkedKeys.asStateFlow()

    fun toggleChecked(key: String) {
        _checkedKeys.value = if (key in _checkedKeys.value) {
            _checkedKeys.value - key
        } else {
            _checkedKeys.value + key
        }
    }

    fun selectThisWeek() {
        _rangeStart.value = thisWeekStart
        _rangeEnd.value = thisWeekStart.plus(6, DateTimeUnit.DAY)
    }

    fun selectNextWeek() {
        val start = thisWeekStart.plus(7, DateTimeUnit.DAY)
        _rangeStart.value = start
        _rangeEnd.value = start.plus(6, DateTimeUnit.DAY)
    }
}
