package com.example.mealplannerapp.domain

import kotlinx.datetime.LocalDate

data class DayNutrition(
    val date: LocalDate,
    val calories: Double,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double
)
