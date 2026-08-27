package com.example.mealplannerapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.mealplannerapp.ui.grocery.GroceryListScreen
import com.example.mealplannerapp.ui.nutrition.NutritionScreen
import com.example.mealplannerapp.ui.planner.PlannerScreen
import com.example.mealplannerapp.ui.recipes.IngredientCatalogScreen
import com.example.mealplannerapp.ui.recipes.RecipeEditScreen
import com.example.mealplannerapp.ui.recipes.RecipeListScreen

@Composable
fun MealPlannerNavGraph() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { MealPlannerBottomBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Planner,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Destination.Planner> { PlannerScreen() }
            composable<Destination.Recipes> {
                RecipeListScreen(
                    onAddRecipe = { navController.navigate(Destination.RecipeEdit(recipeId = null)) },
                    onEditRecipe = { recipeId -> navController.navigate(Destination.RecipeEdit(recipeId = recipeId)) },
                    onOpenIngredientCatalog = { navController.navigate(Destination.IngredientCatalog) }
                )
            }
            composable<Destination.RecipeEdit> { backStackEntry ->
                val route: Destination.RecipeEdit = backStackEntry.toRoute()
                RecipeEditScreen(recipeId = route.recipeId, onDone = { navController.popBackStack() })
            }
            composable<Destination.IngredientCatalog> {
                IngredientCatalogScreen(onDone = { navController.popBackStack() })
            }
            composable<Destination.Grocery> { GroceryListScreen() }
            composable<Destination.Nutrition> { NutritionScreen() }
        }
    }
}

@Composable
private fun MealPlannerBottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()

    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = isCurrentDestination(backStackEntry?.destination, item.destination)
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.destination) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

private fun isCurrentDestination(current: NavDestination?, target: Destination): Boolean {
    if (current == null) return false
    return when (target) {
        is Destination.Planner -> current.hasRoute<Destination.Planner>()
        is Destination.Recipes -> current.hasRoute<Destination.Recipes>()
        is Destination.Grocery -> current.hasRoute<Destination.Grocery>()
        is Destination.Nutrition -> current.hasRoute<Destination.Nutrition>()
        is Destination.RecipeEdit -> current.hasRoute<Destination.RecipeEdit>()
        is Destination.IngredientCatalog -> current.hasRoute<Destination.IngredientCatalog>()
    }
}
