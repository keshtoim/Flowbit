package com.flowbit.app.domain.model

import java.time.LocalDate

data class HabitStats(
    val habitId: Long,
    val habitName: String,
    val habitEmoji: String,
    val completionRate: Float,
    val currentStreak: Int,
    val longestStreak: Int,
    val totalCompletions: Int,
    val completedDates: List<LocalDate>,
    val photoUri: String? = null,
    val audioUri: String? = null,
)

data class OverallStats(
    val totalHabits: Int,
    val activeHabits: Int,
    val averageCompletionRate: Float,
    val bestStreak: Int,
    val todayCompleted: Int,
    val todayTotal: Int,
)

data class WeekdayInsight(
    val weekdayRate: Float,
    val weekendRate: Float,
    val insightText: String,
)

data class PeriodComparison(
    val thisWeekRate: Float,
    val lastWeekRate: Float,
    val thisMonthRate: Float,
    val lastMonthRate: Float,
    val thisWeekLabel: String,
    val lastWeekLabel: String,
)

data class BestTimeData(
    val hourCounts: Map<Int, Int>,
    val peakHour: Int?,
)
