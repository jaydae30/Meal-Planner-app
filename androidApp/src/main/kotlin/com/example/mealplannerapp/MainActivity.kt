package com.example.mealplannerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.mealplannerapp.data.local.buildDatabase
import com.example.mealplannerapp.data.local.getDatabaseBuilder
import com.example.mealplannerapp.di.AppContainer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = buildDatabase(getDatabaseBuilder(applicationContext))
        val appContainer = AppContainer(database)

        setContent {
            App(appContainer)
        }
    }
}
