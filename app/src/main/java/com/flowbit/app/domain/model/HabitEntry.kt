package com.flowbit.app.domain.model

import java.time.LocalDate

data class HabitEntry(
    val id: Long = 0,
    val habitId: Long,
    val date: LocalDate,
    val completedCount: Int = 0,
)

data class HabitWithEntries(
    val habit: Habit,
    val entries: List<HabitEntry>,
)
