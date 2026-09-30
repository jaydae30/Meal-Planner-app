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
                // Iterate through all recipes in this meal
                for (recipeDetail in detail.recipes) {
                    val servings = recipeDetail.servings
                    calories += servings * recipeDetail.recipe.caloriesPerServing
                    protein += servings * recipeDetail.recipe.proteinGramsPerServing
                    carbs += servings * recipeDetail.recipe.carbsGramsPerServing
                    fat += servings * recipeDetail.recipe.fatGramsPerServing
                }

                // Also add nutrition from standalone meal ingredients
                for (mealIngredient in detail.mealIngredients) {
                    calories += mealIngredient.quantity * mealIngredient.caloriesPerUnit
                    protein += mealIngredient.quantity * mealIngredient.proteinGramsPerUnit
                    carbs += mealIngredient.quantity * mealIngredient.carbsGramsPerUnit
                    fat += mealIngredient.quantity * mealIngredient.fatGramsPerUnit
                }
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
