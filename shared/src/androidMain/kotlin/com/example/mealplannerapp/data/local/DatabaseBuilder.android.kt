package com.example.mealplannerapp.data.local

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<MealPlannerDatabase> {
    val dbFile = context.applicationContext.getDatabasePath(MEAL_PLANNER_DB_FILE_NAME)
    return Room.databaseBuilder<MealPlannerDatabase>(
        context = context.applicationContext,
        name = dbFile.absolutePath
    )
}
