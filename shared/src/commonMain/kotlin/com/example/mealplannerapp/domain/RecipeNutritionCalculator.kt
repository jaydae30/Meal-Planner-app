package com.example.mealplannerapp.domain

import com.example.mealplannerapp.data.local.IngredientNutrition

/** One line from a recipe's (not-yet-saved) ingredient list, as entered in the edit form. */
data class RecipeIngredientInput(
    val name: String,
    val quantity: Double,
    val unit: String
)

data class RecipeNutritionEstimate(
    val caloriesPerServing: Double,
    val proteinGramsPerServing: Double,
    val carbsGramsPerServing: Double,
    val fatGramsPerServing: Double,
    val matchedIngredientNames: List<String>,
    val unmatchedIngredientNames: List<String>
)

/**
 * Auto-calculates a recipe's per-serving nutrition by matching each ingredient line against a
 * user-maintained nutrition catalog (name, kcal/macros per some serving size). Matching requires
 * both the ingredient name AND unit to line up with a catalog entry (trim + lowercase) — like
 * GroceryListGenerator, this deliberately does no unit conversion, so "2 cups flour" only matches
 * a catalog entry defined in "cups", not one defined in "g". Unmatched ingredients contribute
 * nothing and are reported back so the UI can be upfront about what wasn't counted.
 */
object RecipeNutritionCalculator {

    fun calculate(
        ingredients: List<RecipeIngredientInput>,
        nutritionCatalog: List<IngredientNutrition>,
        recipeServings: Int
    ): RecipeNutritionEstimate {
        val catalogByKey = nutritionCatalog.associateBy {
            it.name.trim().lowercase() to it.servingUnit.trim().lowercase()
        }

        var totalCalories = 0.0
        var totalProtein = 0.0
        var totalCarbs = 0.0
        var totalFat = 0.0
        val matched = mutableListOf<String>()
        val unmatched = mutableListOf<String>()

        for (ingredient in ingredients) {
            val key = ingredient.name.trim().lowercase() to ingredient.unit.trim().lowercase()
            val info = catalogByKey[key]
            if (info == null || info.servingQuantity == 0.0) {
                unmatched.add(ingredient.name)
                continue
            }
            val scale = ingredient.quantity / info.servingQuantity
            totalCalories += scale * info.caloriesPerServing
            totalProtein += scale * info.proteinGramsPerServing
            totalCarbs += scale * info.carbsGramsPerServing
            totalFat += scale * info.fatGramsPerServing
            matched.add(ingredient.name)
        }

        val servings = recipeServings.coerceAtLeast(1)
        return RecipeNutritionEstimate(
            caloriesPerServing = totalCalories / servings,
            proteinGramsPerServing = totalProtein / servings,
            carbsGramsPerServing = totalCarbs / servings,
            fatGramsPerServing = totalFat / servings,
            matchedIngredientNames = matched,
            unmatchedIngredientNames = unmatched
        )
    }
}
