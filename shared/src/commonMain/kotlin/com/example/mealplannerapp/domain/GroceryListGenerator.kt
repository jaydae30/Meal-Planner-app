package com.example.mealplannerapp.domain

import com.example.mealplannerapp.data.repository.PlannedMealDetail

/**
 * Aggregates ingredients across planned meals. Ingredients only merge when both their
 * (trimmed, lowercased) name AND unit match — mismatched units for the same ingredient name
 * are intentionally kept as separate lines since no unit-conversion table exists (v1 scope).
 */
object GroceryListGenerator {

    fun generate(plannedMealDetails: List<PlannedMealDetail>): List<GroceryItem> {
        data class Accumulated(
            var displayName: String,
            var unit: String,
            var totalQuantity: Double,
            val sourceRecipeNames: MutableSet<String>
        )

        val accumulator = LinkedHashMap<String, Accumulated>()

        for (detail in plannedMealDetails) {
            // Iterate through all recipes in this meal
            for (recipeDetail in detail.recipes) {
                val scale = if (recipeDetail.recipe.servings != 0) {
                    recipeDetail.servings / recipeDetail.recipe.servings
                } else {
                    recipeDetail.servings
                }

                for (ingredient in recipeDetail.ingredients) {
                    val normalizedName = ingredient.name.trim().lowercase()
                    val normalizedUnit = ingredient.unit.trim().lowercase()
                    if (normalizedName.isEmpty()) continue

                    val key = "$normalizedName|$normalizedUnit"
                    val scaledQuantity = ingredient.quantity * scale

                    accumulator.getOrPut(key) {
                        Accumulated(
                            displayName = ingredient.name.trim(),
                            unit = ingredient.unit.trim(),
                            totalQuantity = 0.0,
                            sourceRecipeNames = mutableSetOf()
                        )
                    }.apply {
                        totalQuantity += scaledQuantity
                        sourceRecipeNames.add(recipeDetail.recipe.name)
                    }
                }
            }

            // Also add standalone meal ingredients
            for (mealIngredient in detail.mealIngredients) {
                val normalizedName = mealIngredient.name.trim().lowercase()
                val normalizedUnit = mealIngredient.unit.trim().lowercase()
                if (normalizedName.isEmpty()) continue

                val key = "$normalizedName|$normalizedUnit"

                accumulator.getOrPut(key) {
                    Accumulated(
                        displayName = mealIngredient.name.trim(),
                        unit = mealIngredient.unit.trim(),
                        totalQuantity = 0.0,
                        sourceRecipeNames = mutableSetOf()
                    )
                }.apply {
                    totalQuantity += mealIngredient.quantity
                    sourceRecipeNames.add("Direct ingredient")
                }
            }
        }

        return accumulator.map { (key, acc) ->
            GroceryItem(
                key = key,
                displayName = acc.displayName,
                unit = acc.unit,
                totalQuantity = acc.totalQuantity,
                sourceRecipeNames = acc.sourceRecipeNames
            )
        }.sortedBy { it.displayName.lowercase() }
    }
}
