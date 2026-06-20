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
)

data class OverallStats(
    val totalHabits: Int,
    val activeHabits: Int,
    val averageCompletionRate: Float,
    val bestStreak: Int,
    val todayCompleted: Int,
    val todayTotal: Int,
)
