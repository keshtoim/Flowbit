package com.flowbit.app.data.repository

import com.flowbit.app.data.database.dao.HabitDao
import com.flowbit.app.data.database.entity.HabitEntity
import com.flowbit.app.data.database.entity.HabitEntryEntity
import com.flowbit.app.domain.model.Habit
import com.flowbit.app.domain.model.HabitEntry
import com.flowbit.app.domain.model.HabitFrequency
import com.flowbit.app.domain.model.HabitStats
import com.flowbit.app.domain.model.HabitWithEntries
import com.flowbit.app.domain.model.OverallStats
import com.flowbit.app.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class HabitRepositoryImpl @Inject constructor(
    private val dao: HabitDao,
) : HabitRepository {

    override fun getAllHabits(): Flow<List<Habit>> =
        dao.getAllHabits().map { list -> list.map { it.toDomain() } }

    override fun getActiveHabits(): Flow<List<Habit>> =
        dao.getActiveHabits().map { list -> list.map { it.toDomain() } }

    override fun getWidgetHabits(): Flow<List<Habit>> =
        dao.getWidgetHabits().map { list -> list.map { it.toDomain() } }

    override suspend fun getHabitById(id: Long): Habit? =
        dao.getHabitById(id)?.toDomain()

    override suspend fun insertHabit(habit: Habit): Long =
        dao.insertHabit(HabitEntity.fromDomain(habit))

    override suspend fun updateHabit(habit: Habit) =
        dao.updateHabit(HabitEntity.fromDomain(habit))

    override suspend fun deleteHabit(habit: Habit) =
        dao.deleteHabit(HabitEntity.fromDomain(habit))

    override suspend fun archiveHabit(habitId: Long) =
        dao.archiveHabit(habitId)

    override fun getHabitWithEntries(habitId: Long): Flow<HabitWithEntries?> {
        return combine(
            dao.getActiveHabits(),
            dao.getEntriesForHabit(habitId),
        ) { habits, entries ->
            val habit = habits.find { it.id == habitId }?.toDomain() ?: return@combine null
            HabitWithEntries(
                habit = habit,
                entries = entries.map { it.toDomain() },
            )
        }
    }

    override fun getEntriesForDate(date: LocalDate): Flow<List<HabitEntry>> =
        dao.getEntriesForDate(date.toString()).map { list -> list.map { it.toDomain() } }

    override suspend fun getEntryForDate(habitId: Long, date: LocalDate): HabitEntry? =
        dao.getEntryForDate(habitId, date.toString())?.toDomain()

    override suspend fun upsertEntry(entry: HabitEntry) {
        dao.insertEntry(HabitEntryEntity.fromDomain(entry))
    }

    override suspend fun deleteEntry(entry: HabitEntry) {
        dao.deleteEntry(HabitEntryEntity.fromDomain(entry))
    }

    override suspend fun updateSortOrder(habitId: Long, order: Int) =
        dao.updateSortOrder(habitId, order)

    override suspend fun getEntriesForDateRange(start: LocalDate, end: LocalDate): List<HabitEntry> =
        dao.getEntriesForDateRange(start.toString(), end.toString()).map { it.toDomain() }

    override suspend fun getHabitStats(habitId: Long): HabitStats? {
        val habit = dao.getHabitById(habitId)?.toDomain() ?: return null
        val entries = dao.getAllEntriesForHabit(habitId).map { it.toDomain() }
        val completedDates = entries
            .filter { it.completedCount >= habit.targetCount }
            .map { it.date }
            .sorted()

        val totalDays = countScheduledDays(habit)
        val completionRate = if (totalDays > 0) completedDates.size.toFloat() / totalDays else 0f
        val currentStreak = calculateCurrentStreak(habit, completedDates)
        val longestStreak = calculateLongestStreak(habit, completedDates)

        return HabitStats(
            habitId = habitId,
            habitName = habit.name,
            habitEmoji = habit.emoji,
            completionRate = completionRate,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            totalCompletions = completedDates.size,
            completedDates = completedDates,
            photoUri = habit.photoUri,
            audioUri = habit.audioUri,
        )
    }

    override suspend fun getOverallStats(): OverallStats {
        // Simplified implementation
        return OverallStats(
            totalHabits = 0,
            activeHabits = 0,
            averageCompletionRate = 0f,
            bestStreak = 0,
            todayCompleted = 0,
            todayTotal = 0,
        )
    }

    private fun countScheduledDays(habit: Habit): Int {
        val today = LocalDate.now()
        val start = habit.startDate
        if (start > today) return 0
        var count = 0
        var current = start
        while (!current.isAfter(today)) {
            if (habit.frequency == HabitFrequency.DAILY || current.dayOfWeek in habit.scheduledDays) {
                count++
            }
            current = current.plusDays(1)
        }
        return count
    }

    private fun calculateCurrentStreak(habit: Habit, completedDates: List<LocalDate>): Int {
        val today = LocalDate.now()
        val datesSet = completedDates.toHashSet()
        var streak = 0
        var current = today
        while (true) {
            val isScheduled = habit.frequency == HabitFrequency.DAILY ||
                current.dayOfWeek in habit.scheduledDays
            if (!isScheduled) {
                current = current.minusDays(1)
                if (current.isBefore(habit.startDate)) break
                continue
            }
            if (current in datesSet) {
                streak++
                current = current.minusDays(1)
                if (current.isBefore(habit.startDate)) break
            } else {
                break
            }
        }
        return streak
    }

    private fun calculateLongestStreak(habit: Habit, completedDates: List<LocalDate>): Int {
        if (completedDates.isEmpty()) return 0
        var longest = 1
        var current = 1
        for (i in 1 until completedDates.size) {
            val diff = ChronoUnit.DAYS.between(completedDates[i - 1], completedDates[i])
            if (diff == 1L) {
                current++
                if (current > longest) longest = current
            } else {
                current = 1
            }
        }
        return longest
    }
}
