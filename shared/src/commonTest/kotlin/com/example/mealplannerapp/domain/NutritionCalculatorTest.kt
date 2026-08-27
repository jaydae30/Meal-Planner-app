package com.example.mealplannerapp.domain

import com.example.mealplannerapp.data.local.MealSlot
import com.example.mealplannerapp.data.local.PlannedMeal
import com.example.mealplannerapp.data.local.Recipe
import com.example.mealplannerapp.data.repository.PlannedMealDetail
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class NutritionCalculatorTest {

    @Test
    fun sumsMacrosScaledByPlannedServingsForEachDay() {
        val monday = LocalDate(2026, 8, 24)
        val tuesday = LocalDate(2026, 8, 25)

        val recipe = Recipe(
            id = 1,
            name = "Oatmeal",
            servings = 1,
            caloriesPerServing = 200.0,
            proteinGramsPerServing = 8.0,
            carbsGramsPerServing = 30.0,
            fatGramsPerServing = 4.0
        )

        val details = listOf(
            PlannedMealDetail(
                plannedMeal = PlannedMeal(
                    dateEpochDay = monday.toEpochDays(),
                    mealSlot = MealSlot.BREAKFAST,
                    recipeId = 1,
                    servings = 2.0
                ),
                recipe = recipe,
                ingredients = emptyList()
            )
        )

        val totals = NutritionCalculator.calculateDailyTotals(monday, tuesday, details)

        assertEquals(2, totals.size)
        assertEquals(monday, totals[0].date)
        assertEquals(400.0, totals[0].calories)
        assertEquals(16.0, totals[0].proteinGrams)
        assertEquals(tuesday, totals[1].date)
        assertEquals(0.0, totals[1].calories)
    }
}
