package com.example.mealplannerapp.data.local

import androidx.room3.ColumnTypeConverter
import kotlinx.datetime.LocalDate

class Converters {
    @ColumnTypeConverter
    fun fromEpochDay(value: Long): LocalDate = LocalDate.fromEpochDays(value.toInt())

    @ColumnTypeConverter
    fun toEpochDay(date: LocalDate): Long = date.toEpochDays()

    @ColumnTypeConverter
    fun fromMealSlotName(value: String): MealSlot = MealSlot.valueOf(value)

    @ColumnTypeConverter
    fun toMealSlotName(slot: MealSlot): String = slot.name
}
