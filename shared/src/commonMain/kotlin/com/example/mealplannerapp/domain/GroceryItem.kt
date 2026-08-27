package com.example.mealplannerapp.domain

data class GroceryItem(
    val key: String,
    val displayName: String,
    val unit: String,
    val totalQuantity: Double,
    val sourceRecipeNames: Set<String>
)
