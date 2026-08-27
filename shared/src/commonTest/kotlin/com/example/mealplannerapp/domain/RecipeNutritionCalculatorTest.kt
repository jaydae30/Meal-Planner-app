package com.example.mealplannerapp.domain

import com.example.mealplannerapp.data.local.IngredientNutrition
import kotlin.test.Test
import kotlin.test.assertEquals

class RecipeNutritionCalculatorTest {

    private fun nutrition(
        name: String,
        servingQuantity: Double,
        servingUnit: String,
        calories: Double,
        protein: Double = 0.0,
        carbs: Double = 0.0,
        fat: Double = 0.0
    ) = IngredientNutrition(
        name = name,
        servingQuantity = servingQuantity,
        servingUnit = servingUnit,
        caloriesPerServing = calories,
        proteinGramsPerServing = protein,
        carbsGramsPerServing = carbs,
        fatGramsPerServing = fat
    )

    @Test
    fun sumsMatchedIngredientsAndDividesByServings() {
        val catalog = listOf(
            nutrition("chicken breast", 100.0, "g", calories = 165.0, protein = 31.0),
            nutrition("rice", 1.0, "cup", calories = 200.0, carbs = 45.0)
        )
        val ingredients = listOf(
            RecipeIngredientInput("chicken breast", 200.0, "g"),
            RecipeIngredientInput("rice", 2.0, "cup")
        )

        // total: chicken 2x165=330, rice 2x200=400 -> 730 total / 4 servings = 182.5 per serving
        val result = RecipeNutritionCalculator.calculate(ingredients, catalog, recipeServings = 4)

        assertEquals(182.5, result.caloriesPerServing)
        assertEquals(15.5, result.proteinGramsPerServing) // 2x31 / 4
        assertEquals(22.5, result.carbsGramsPerServing) // 2x45 / 4
        assertEquals(listOf("chicken breast", "rice"), result.matchedIngredientNames)
        assertEquals(emptyList(), result.unmatchedIngredientNames)
    }

    @Test
    fun treatsUnitMismatchAsUnmatched() {
        val catalog = listOf(nutrition("flour", 100.0, "g", calories = 364.0))
        val ingredients = listOf(RecipeIngredientInput("flour", 2.0, "cups"))

        val result = RecipeNutritionCalculator.calculate(ingredients, catalog, recipeServings = 1)

        assertEquals(0.0, result.caloriesPerServing)
        assertEquals(listOf("flour"), result.unmatchedIngredientNames)
        assertEquals(emptyList(), result.matchedIngredientNames)
    }

    @Test
    fun treatsUnknownIngredientAsUnmatched() {
        val result = RecipeNutritionCalculator.calculate(
            ingredients = listOf(RecipeIngredientInput("mystery sauce", 1.0, "tbsp")),
            nutritionCatalog = emptyList(),
            recipeServings = 1
        )

        assertEquals(listOf("mystery sauce"), result.unmatchedIngredientNames)
    }

    @Test
    fun matchIsCaseAndWhitespaceInsensitive() {
        val catalog = listOf(nutrition("Chicken Breast", 100.0, "G", calories = 165.0))
        val ingredients = listOf(RecipeIngredientInput("  chicken breast  ", 100.0, " g "))

        val result = RecipeNutritionCalculator.calculate(ingredients, catalog, recipeServings = 1)

        assertEquals(165.0, result.caloriesPerServing)
    }
}
