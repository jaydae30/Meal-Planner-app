package com.example.mealplannerapp.data.local

import androidx.room3.Room
import androidx.room3.RoomDatabase
import platform.Foundation.NSHomeDirectory

fun getDatabaseBuilder(): RoomDatabase.Builder<MealPlannerDatabase> {
    val dbFilePath = NSHomeDirectory() + "/$MEAL_PLANNER_DB_FILE_NAME"
    return Room.databaseBuilder<MealPlannerDatabase>(name = dbFilePath)
}
