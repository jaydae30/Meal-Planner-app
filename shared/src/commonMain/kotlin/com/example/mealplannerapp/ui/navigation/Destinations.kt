package com.example.mealplannerapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable
sealed class Destination {
    @Serializable data object Planner : Destination()
    @Serializable data object Recipes : Destination()
    @Serializable data object Grocery : Destination()
    @Serializable data object Nutrition : Destination()
    @Serializable data class RecipeEdit(val recipeId: Long? = null) : Destination()
    @Serializable data object IngredientCatalog : Destination()
}

data class BottomNavItem(
    val destination: Destination,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Destination.Planner, "Planner", Icons.Filled.CalendarMonth),
    BottomNavItem(Destination.Recipes, "Recipes", Icons.AutoMirrored.Filled.MenuBook),
    BottomNavItem(Destination.Grocery, "Grocery", Icons.Filled.ShoppingCart),
    BottomNavItem(Destination.Nutrition, "Nutrition", Icons.Filled.MonitorHeart)
)
