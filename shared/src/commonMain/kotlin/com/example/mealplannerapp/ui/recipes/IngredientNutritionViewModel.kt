package com.example.mealplannerapp.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealplannerapp.data.local.IngredientNutrition
import com.example.mealplannerapp.data.repository.IngredientNutritionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IngredientNutritionViewModel(
    private val repository: IngredientNutritionRepository
) : ViewModel() {

    val items: StateFlow<List<IngredientNutrition>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun save(item: IngredientNutrition) {
        viewModelScope.launch { repository.save(item) }
    }

    fun delete(item: IngredientNutrition) {
        viewModelScope.launch { repository.delete(item) }
    }
}
