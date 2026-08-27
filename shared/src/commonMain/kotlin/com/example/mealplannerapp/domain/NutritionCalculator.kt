package com.example.mealplannerapp.domain

import com.example.mealplannerapp.data.repository.PlannedMealDetail
import kotlinx.datetime.LocalDate

object NutritionCalculator {

    /** One [DayNutrition] per date in [start]..[end] inclusive, in order, even if a day has no planned meals. */
    fun calculateDailyTotals(
        start: LocalDate,
        end: LocalDate,
        plannedMealDetails: List<PlannedMealDetail>
    ): List<DayNutrition> {
        val byDate = plannedMealDetails.groupBy {
            LocalDate.fromEpochDays(it.plannedMeal.dateEpochDay.toInt())
        }

        return start.datesUntilInclusive(end).map { date ->
            val meals = byDate[date].orEmpty()
            var calories = 0.0
            var protein = 0.0
            var carbs = 0.0
            var fat = 0.0
            for (detail in meals) {
                val servings = detail.plannedMeal.servings
                calories += servings * detail.recipe.caloriesPerServing
                protein += servings * detail.recipe.proteinGramsPerServing
                carbs += servings * detail.recipe.carbsGramsPerServing
                fat += servings * detail.recipe.fatGramsPerServing
            }
            DayNutrition(
                date = date,
                calories = calories,
                proteinGrams = protein,
                carbsGrams = carbs,
                fatGrams = fat
            )
        }
    }
}
