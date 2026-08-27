package com.example.mealplannerapp.data.repository

import com.example.mealplannerapp.data.local.SettingsDao
import com.example.mealplannerapp.data.local.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val settingsDao: SettingsDao) {

    fun observeDailyCalorieGoal(): Flow<Double> =
        settingsDao.observe().map { it?.dailyCalorieGoal ?: DEFAULT_DAILY_CALORIE_GOAL }

    suspend fun setDailyCalorieGoal(goal: Double) {
        settingsDao.upsert(UserSettings(dailyCalorieGoal = goal))
    }

    companion object {
        const val DEFAULT_DAILY_CALORIE_GOAL = 2000.0
    }
}
