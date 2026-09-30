package com.example.mealplannerapp.domain

import com.example.mealplannerapp.data.local.Ingredient
import com.example.mealplannerapp.data.local.MealSlot
import com.example.mealplannerapp.data.local.PlannedMeal
import com.example.mealplannerapp.data.local.Recipe
import com.example.mealplannerapp.data.repository.PlannedMealDetail
import com.example.mealplannerapp.data.repository.RecipeDetail
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

    private fun plannedMeal() = PlannedMeal(
        dateEpochDay = 0,
        mealSlot = MealSlot.DINNER
    )

    @Test
    fun sumsMatchingNameAndUnitAcrossRecipes() {
        val chili = recipe(id = 1, name = "Chili", servings = 4)
        val tacos = recipe(id = 2, name = "Tacos", servings = 2)

        val details = listOf(
            PlannedMealDetail(
                plannedMeal = plannedMeal(),
                recipes = listOf(
                    RecipeDetail(
                        recipe = chili,
                        ingredients = listOf(Ingredient(recipeId = 1, name = "Ground Beef", quantity = 1.0, unit = "lb")),
                        servings = 4.0
                    )
                )
            ),
            PlannedMealDetail(
                plannedMeal = plannedMeal(),
                recipes = listOf(
                    RecipeDetail(
                        recipe = tacos,
                        ingredients = listOf(Ingredient(recipeId = 2, name = "ground beef", quantity = 0.5, unit = "LB")),
                        servings = 2.0
                    )
                )
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
                plannedMeal = plannedMeal(),
                recipes = listOf(
                    RecipeDetail(
                        recipe = recipe,
                        ingredients = listOf(Ingredient(recipeId = 1, name = "Carrot", quantity = 4.0, unit = "cup")),
                        servings = 2.0
                    )
                )
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
                plannedMeal = plannedMeal(),
                recipes = listOf(
                    RecipeDetail(
                        recipe = recipe,
                        ingredients = listOf(
                            Ingredient(recipeId = 1, name = "Flour", quantity = 2.0, unit = "cup"),
                            Ingredient(recipeId = 1, name = "Flour", quantity = 500.0, unit = "g")
                        ),
                        servings = 1.0
                    )
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
