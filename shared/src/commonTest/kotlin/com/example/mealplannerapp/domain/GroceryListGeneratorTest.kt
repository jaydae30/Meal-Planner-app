package com.example.mealplannerapp.domain

import com.example.mealplannerapp.data.local.Ingredient
import com.example.mealplannerapp.data.local.MealSlot
import com.example.mealplannerapp.data.local.PlannedMeal
import com.example.mealplannerapp.data.local.Recipe
import com.example.mealplannerapp.data.repository.PlannedMealDetail
import kotlin.test.Test
import kotlin.test.assertEquals

class GroceryListGeneratorTest {

    private fun recipe(id: Long, name: String, servings: Int) = Recipe(
        id = id,
        name = name,
        servings = servings,
        caloriesPerServing = 100.0,
        proteinGramsPerServing = 10.0,
        carbsGramsPerServing = 10.0,
        fatGramsPerServing = 10.0
    )

    private fun plannedMeal(recipeId: Long, servings: Double) = PlannedMeal(
        dateEpochDay = 0,
        mealSlot = MealSlot.DINNER,
        recipeId = recipeId,
        servings = servings
    )

    @Test
    fun sumsMatchingNameAndUnitAcrossRecipes() {
        val chili = recipe(id = 1, name = "Chili", servings = 4)
        val tacos = recipe(id = 2, name = "Tacos", servings = 2)

        val details = listOf(
            PlannedMealDetail(
                plannedMeal = plannedMeal(recipeId = 1, servings = 4.0),
                recipe = chili,
                ingredients = listOf(Ingredient(recipeId = 1, name = "Ground Beef", quantity = 1.0, unit = "lb"))
            ),
            PlannedMealDetail(
                plannedMeal = plannedMeal(recipeId = 2, servings = 2.0),
                recipe = tacos,
                ingredients = listOf(Ingredient(recipeId = 2, name = "ground beef", quantity = 0.5, unit = "LB"))
            )
        )

        val result = GroceryListGenerator.generate(details)

        assertEquals(1, result.size)
        assertApproxEquals(1.5, result.single().totalQuantity)
        assertEquals(setOf("Chili", "Tacos"), result.single().sourceRecipeNames)
    }

    @Test
    fun scalesQuantityByPlannedServingsRelativeToRecipeServings() {
        val recipe = recipe(id = 1, name = "Soup", servings = 4)
        val details = listOf(
            PlannedMealDetail(
                plannedMeal = plannedMeal(recipeId = 1, servings = 2.0),
                recipe = recipe,
                ingredients = listOf(Ingredient(recipeId = 1, name = "Carrot", quantity = 4.0, unit = "cup"))
            )
        )

        val result = GroceryListGenerator.generate(details)

        assertApproxEquals(2.0, result.single().totalQuantity)
    }

    @Test
    fun keepsDifferentUnitsForSameNameAsSeparateLines() {
        val recipe = recipe(id = 1, name = "Bread", servings = 1)
        val details = listOf(
            PlannedMealDetail(
                plannedMeal = plannedMeal(recipeId = 1, servings = 1.0),
                recipe = recipe,
                ingredients = listOf(
                    Ingredient(recipeId = 1, name = "Flour", quantity = 2.0, unit = "cup"),
                    Ingredient(recipeId = 1, name = "Flour", quantity = 500.0, unit = "g")
                )
            )
        )

        val result = GroceryListGenerator.generate(details)

        assertEquals(2, result.size)
    }
}

private fun assertApproxEquals(expected: Double, actual: Double, tolerance: Double = 0.0001) {
    kotlin.test.assertTrue(
        kotlin.math.abs(expected - actual) <= tolerance,
        "Expected $expected but was $actual"
    )
}
