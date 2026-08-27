package com.example.mealplannerapp.domain

/**
 * Best-effort, fully offline extraction of recipe fields from freeform text (a pasted recipe,
 * a TikTok/Instagram caption, OCR output from a photo, etc). No AI involved — pure pattern
 * matching, so it works best on clearly-labeled text and is expected to need manual cleanup
 * on very casual/run-on captions. Always show the result in an editable form before saving.
 */
data class ParsedIngredientDraft(
    val name: String,
    val quantity: Double?,
    val unit: String
)

data class ParsedRecipeDraft(
    val name: String?,
    val servings: Int?,
    val caloriesPerServing: Double?,
    val proteinGramsPerServing: Double?,
    val carbsGramsPerServing: Double?,
    val fatGramsPerServing: Double?,
    val tags: List<String>,
    val ingredients: List<ParsedIngredientDraft>
)

object RecipeTextParser {

    private val hashtagRegex = Regex("""#(\w+)""")
    // Non-digit gap is restricted to same-line separators (space/colon) so "per serving" on one
    // line can't accidentally pick up a quantity from the next line (e.g. an ingredient list).
    private val servingsRegex = Regex("""(?i)(?:serves|servings?)[ :]{0,3}(\d+)""")
    private val caloriesRegex = Regex("""(?i)(\d+(?:\.\d+)?)\s*(?:kcal|calories|cal)\b""")
    private val proteinRegex = Regex("""(?i)protein[ :]{0,5}(\d+(?:\.\d+)?)\s*g""")
    private val carbsRegex = Regex("""(?i)carb(?:oh?ydrates?)?[ :]{0,5}(\d+(?:\.\d+)?)\s*g""")
    private val fatRegex = Regex("""(?i)\bfat[ :]{0,5}(\d+(?:\.\d+)?)\s*g""")

    private val sectionHeaderRegex = Regex("""(?i)^\s*ingredients\s*:?\s*$""")
    private val endOfIngredientsRegex =
        Regex("""(?i)^\s*(instructions|directions|steps|method|preparation|nutrition)\b""")
    private val bulletPrefixRegex = Regex("""^\s*(?:[-•*▪◦]|\d+[.)])\s*""")

    private val units = listOf(
        "cups?", "tbsp", "tablespoons?", "tsp", "teaspoons?", "g", "grams?", "kg", "kilograms?",
        "oz", "ounces?", "lbs?", "pounds?", "ml", "milliliters?", "l", "liters?",
        "pinch(?:es)?", "cloves?", "slices?", "cans?", "packages?", "sticks?"
    ).joinToString("|")
    private val quantityToken = """\d+\s+\d+/\d+|\d+/\d+|\d*\.\d+|\d+"""
    private val ingredientLineRegex =
        Regex("""(?i)^\s*($quantityToken)?\s*($units)?\s*(.+?)\s*$""")

    fun parse(rawText: String): ParsedRecipeDraft {
        val lines = rawText.lines().map { it.trim() }

        val tags = hashtagRegex.findAll(rawText).map { it.groupValues[1] }.distinct().toList()
        val servings = servingsRegex.find(rawText)?.groupValues?.get(1)?.toIntOrNull()
        val calories = caloriesRegex.find(rawText)?.groupValues?.get(1)?.toDoubleOrNull()
        val protein = proteinRegex.find(rawText)?.groupValues?.get(1)?.toDoubleOrNull()
        val carbs = carbsRegex.find(rawText)?.groupValues?.get(1)?.toDoubleOrNull()
        val fat = fatRegex.find(rawText)?.groupValues?.get(1)?.toDoubleOrNull()

        val name = lines.firstOrNull { line ->
            val stripped = hashtagRegex.replace(line, "").trim()
            stripped.isNotEmpty() && !sectionHeaderRegex.matches(line)
        }?.let { hashtagRegex.replace(it, "").trim() }?.takeIf { it.isNotEmpty() }

        val ingredients = extractIngredients(lines)

        return ParsedRecipeDraft(
            name = name,
            servings = servings,
            caloriesPerServing = calories,
            proteinGramsPerServing = protein,
            carbsGramsPerServing = carbs,
            fatGramsPerServing = fat,
            tags = tags,
            ingredients = ingredients
        )
    }

    private fun extractIngredients(lines: List<String>): List<ParsedIngredientDraft> {
        val headerIndex = lines.indexOfFirst { sectionHeaderRegex.matches(it) }
        val candidateLines = if (headerIndex >= 0) {
            lines.drop(headerIndex + 1).takeWhile { line ->
                line.isNotBlank() && !endOfIngredientsRegex.containsMatchIn(line)
            }
        } else {
            lines.filter { bulletPrefixRegex.containsMatchIn(it) && it.isNotBlank() }
        }

        return candidateLines
            .map { bulletPrefixRegex.replace(it, "") }
            .filter { it.isNotBlank() }
            .mapNotNull(::parseIngredientLine)
    }

    private fun parseIngredientLine(line: String): ParsedIngredientDraft? {
        val match = ingredientLineRegex.matchEntire(line) ?: return null
        val (quantityText, unitText, nameText) = match.destructured
        if (nameText.isBlank()) return null

        return ParsedIngredientDraft(
            name = nameText.trim(),
            quantity = quantityText.takeIf { it.isNotBlank() }?.let(::parseQuantity),
            unit = unitText.trim()
        )
    }

    private fun parseQuantity(token: String): Double? {
        val mixed = token.trim().split(Regex("""\s+"""))
        return when {
            mixed.size == 2 && mixed[1].contains('/') -> {
                val whole = mixed[0].toDoubleOrNull() ?: return null
                val frac = parseFraction(mixed[1]) ?: return null
                whole + frac
            }
            token.contains('/') -> parseFraction(token)
            else -> token.toDoubleOrNull()
        }
    }

    private fun parseFraction(token: String): Double? {
        val parts = token.split('/')
        if (parts.size != 2) return null
        val numerator = parts[0].toDoubleOrNull() ?: return null
        val denominator = parts[1].toDoubleOrNull() ?: return null
        if (denominator == 0.0) return null
        return numerator / denominator
    }
}
