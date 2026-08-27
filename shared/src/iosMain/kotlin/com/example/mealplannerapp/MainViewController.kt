package com.example.mealplannerapp

import androidx.compose.ui.window.ComposeUIViewController
import com.example.mealplannerapp.data.local.buildDatabase
import com.example.mealplannerapp.data.local.getDatabaseBuilder
import com.example.mealplannerapp.di.AppContainer
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    val database = buildDatabase(getDatabaseBuilder())
    val appContainer = AppContainer(database)
    return ComposeUIViewController {
        App(appContainer)
    }
}
