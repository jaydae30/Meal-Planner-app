package com.example.mealplannerapp.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecipeTextParserTest {

    @Test
    fun parsesWellFormattedRecipeText() {
        val text = """
            Chicken Stir Fry
            Serves 4

            Ingredients:
            2 cups rice
            1 lb chicken breast
            1/2 cup soy sauce
            3 cloves garlic

            Instructions:
            1. Cook the rice.
            2. Stir fry the chicken.
        """.trimIndent()

        val result = RecipeTextParser.parse(text)

        assertEquals("Chicken Stir Fry", result.name)
        assertEquals(4, result.servings)
        assertEquals(4, result.ingredients.size)
        assertEquals("rice", result.ingredients[0].name)
        assertEquals(2.0, result.ingredients[0].quantity)
        assertEquals("cups", result.ingredients[0].unit)
        assertEquals(0.5, result.ingredients[2].quantity)
        assertTrue(result.ingredients.none { it.name.contains("Cook", ignoreCase = true) })
    }

    @Test
    fun parsesCasualCaptionWithHashtagsAndBullets() {
        val text = """
            best 5 minute pasta 🍝 #easyrecipe #dinner
            420 calories per serving
            - 2 cups pasta
            - 1 cup cream
            - parmesan
        """.trimIndent()

        val result = RecipeTextParser.parse(text)

        assertEquals("best 5 minute pasta 🍝", result.name)
        assertEquals(listOf("easyrecipe", "dinner"), result.tags)
        assertEquals(420.0, result.caloriesPerServing)
        assertEquals(3, result.ingredients.size)
        assertEquals("pasta", result.ingredients[0].name)
        assertEquals("parmesan", result.ingredients[2].name)
    }

    @Test
    fun doesNotLeakIngredientQuantityIntoServingsViaPerServingPhrase() {
        val text = """
            best 5 minute pasta #easyrecipe #dinner
            420 calories per serving
            - 2 cups pasta
            - 1 cup cream
            - parmesan
        """.trimIndent()

        val result = RecipeTextParser.parse(text)

        assertEquals(null, result.servings)
        assertEquals(420.0, result.caloriesPerServing)
    }

    @Test
    fun returnsNullFieldsForEmptyInput() {
        val result = RecipeTextParser.parse("")

        assertEquals(null, result.name)
        assertEquals(null, result.servings)
        assertTrue(result.ingredients.isEmpty())
    }
}
