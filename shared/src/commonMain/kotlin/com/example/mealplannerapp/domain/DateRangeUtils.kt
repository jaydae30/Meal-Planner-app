package com.example.mealplannerapp.domain

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

/** kotlinx.datetime's DayOfWeek enum is ordered MONDAY..SUNDAY, so ordinal == days since Monday. */
fun LocalDate.startOfWeek(): LocalDate = this.minus(this.dayOfWeek.ordinal, DateTimeUnit.DAY)

fun LocalDate.endOfWeek(): LocalDate = this.startOfWeek().plus(6, DateTimeUnit.DAY)

fun LocalDate.datesUntilInclusive(end: LocalDate): List<LocalDate> {
    val dayCount = end.toEpochDays() - this.toEpochDays()
    return (0..dayCount).map { offset -> LocalDate.fromEpochDays(this.toEpochDays() + offset) }
}

@OptIn(ExperimentalTime::class)
fun todayLocalDate(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
