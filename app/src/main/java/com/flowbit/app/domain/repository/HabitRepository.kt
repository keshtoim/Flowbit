package com.flowbit.app.domain.repository

import com.flowbit.app.domain.model.Habit
import com.flowbit.app.domain.model.HabitEntry
import com.flowbit.app.domain.model.HabitStats
import com.flowbit.app.domain.model.HabitWithEntries
import com.flowbit.app.domain.model.OverallStats
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface HabitRepository {
    fun getAllHabits(): Flow<List<Habit>>
    fun getActiveHabits(): Flow<List<Habit>>
    fun getWidgetHabits(): Flow<List<Habit>>
    suspend fun getHabitById(id: Long): Habit?
    suspend fun insertHabit(habit: Habit): Long
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habit: Habit)
    suspend fun archiveHabit(habitId: Long)

    fun getHabitWithEntries(habitId: Long): Flow<HabitWithEntries?>
    fun getEntriesForDate(date: LocalDate): Flow<List<HabitEntry>>
    suspend fun getEntryForDate(habitId: Long, date: LocalDate): HabitEntry?
    suspend fun upsertEntry(entry: HabitEntry)
    suspend fun deleteEntry(entry: HabitEntry)

    suspend fun getHabitStats(habitId: Long): HabitStats?
    suspend fun getOverallStats(): OverallStats
}
