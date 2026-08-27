package com.example.mealplannerapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.example.mealplannerapp.di.AppContainer
import com.example.mealplannerapp.di.LocalAppContainer
import com.example.mealplannerapp.ui.navigation.MealPlannerNavGraph
import com.example.mealplannerapp.ui.theme.MealPlannerAppTheme

@Composable
fun App(appContainer: AppContainer) {
    CompositionLocalProvider(LocalAppContainer provides appContainer) {
        MealPlannerAppTheme {
            MealPlannerNavGraph()
        }
    }
}
