package com.example.mealplannerapp.di

import androidx.compose.runtime.compositionLocalOf

val LocalAppContainer = compositionLocalOf<AppContainer> {
    error("No AppContainer provided — wrap the app in App(appContainer) at the platform entry point.")
}
