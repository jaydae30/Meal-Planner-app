package com.example.mealplannerapp.data.local

import androidx.room3.ColumnTypeConverters
import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [
        Recipe::class, Ingredient::class, PlannedMeal::class, UserSettings::class,
        IngredientNutrition::class
    ],
    version = 3,
    exportSchema = true
)
@ColumnTypeConverters(Converters::class)
@ConstructedBy(MealPlannerDatabaseConstructor::class)
abstract class MealPlannerDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun plannedMealDao(): PlannedMealDao
    abstract fun settingsDao(): SettingsDao
    abstract fun ingredientNutritionDao(): IngredientNutritionDao
}

@Suppress("KotlinNoActualForExpect")
expect object MealPlannerDatabaseConstructor : RoomDatabaseConstructor<MealPlannerDatabase> {
    override fun initialize(): MealPlannerDatabase
}

const val MEAL_PLANNER_DB_FILE_NAME = "meal_planner.db"

fun buildDatabase(builder: RoomDatabase.Builder<MealPlannerDatabase>): MealPlannerDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        // Pre-release app, no real user data yet: simplest path across schema changes.
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
