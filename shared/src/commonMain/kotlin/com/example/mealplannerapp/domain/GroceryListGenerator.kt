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
            val scale = if (detail.recipe.servings != 0) {
                detail.plannedMeal.servings / detail.recipe.servings
            } else {
                detail.plannedMeal.servings
            }

            for (ingredient in detail.ingredients) {
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
                    sourceRecipeNames.add(detail.recipe.name)
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
