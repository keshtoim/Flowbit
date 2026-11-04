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

    override suspend fun insertHabit(habit: Habit): Long {
        val nextOrder = (dao.getMaxSortOrder() ?: -1) + 1
        return dao.insertHabit(HabitEntity.fromDomain(habit.copy(sortOrder = nextOrder)))
    }

    override suspend fun updateHabit(habit: Habit) =
        dao.updateHabit(HabitEntity.fromDomain(habit))

    override suspend fun deleteHabit(habit: Habit) =
        dao.deleteHabit(HabitEntity.fromDomain(habit))

    override suspend fun archiveHabit(habitId: Long) =
        dao.archiveHabit(habitId)

    override suspend fun unarchiveHabit(habitId: Long) =
        dao.unarchiveHabit(habitId)

    override fun getArchivedHabits(): Flow<List<Habit>> =
        dao.getArchivedHabits().map { list -> list.map { it.toDomain() } }

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
        val completedDates = completedDates(habit, entries, LocalDate.now())
        val frozenDates = frozenDates(entries)

        val totalDays = countScheduledDays(habit)
        val completionRate = if (totalDays > 0) (completedDates.size.toFloat() / totalDays).coerceAtMost(1f) else 0f
        val currentStreak = calculateCurrentStreak(habit, completedDates, frozenDates, LocalDate.now())
        val longestStreak = calculateLongestStreak(habit, completedDates, frozenDates)

        return HabitStats(
            habitId = habitId,
            habitName = habit.name,
            habitEmoji = habit.emoji,
            completionRate = completionRate,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            totalCompletions = completedDates.size,
            completedDates = completedDates,
            startDate = habit.startDate,
            photoUri = habit.photoUri,
            audioUri = habit.audioUri,
        )
    }

    override suspend fun getCurrentStreaks(habits: List<Habit>, asOf: LocalDate): Map<Long, Int> {
        if (habits.isEmpty()) return emptyMap()
        val start = habits.minOf { it.startDate }
        val entriesByHabit = dao.getEntriesForDateRange(start.toString(), asOf.toString())
            .map { it.toDomain() }
            .groupBy { it.habitId }
        return habits.associate { habit ->
            val entries = entriesByHabit[habit.id].orEmpty()
            habit.id to calculateCurrentStreak(habit, completedDates(habit, entries, asOf), frozenDates(entries), asOf)
        }
    }

    override suspend fun freezeStreak(habitId: Long, date: LocalDate) {
        val existing = dao.getEntryForDate(habitId, date.toString())?.toDomain()
        val entry = existing?.copy(isFrozen = true)
            ?: HabitEntry(habitId = habitId, date = date, completedCount = 0, isFrozen = true)
        dao.insertEntry(HabitEntryEntity.fromDomain(entry))
    }

    override suspend fun streakSafeSkip(habitId: Long, date: LocalDate) {
        val existing = dao.getEntryForDate(habitId, date.toString())?.toDomain()
        val entry = existing?.copy(isStreakSafeSkip = true, completedCount = 0, isSkipped = false)
            ?: HabitEntry(habitId = habitId, date = date, completedCount = 0, isStreakSafeSkip = true)
        dao.insertEntry(HabitEntryEntity.fromDomain(entry))
    }

    override suspend fun getOverallStats(): OverallStats {
        val habits = dao.getAllHabitsList().filter { !it.isArchived }.map { it.toDomain() }
        if (habits.isEmpty()) return OverallStats(0, 0, 0f, 0, 0, 0)

        val today = LocalDate.now()
        val todayStr = today.toString()

        val todayTotal = habits.count { h ->
            h.isScheduledOn(today)
        }

        val lookbackDays = habits.maxOf { it.intervalDays }
        val recentEntries = dao.getEntriesForDateRange(today.minusDays(lookbackDays - 1L).toString(), todayStr)
            .map { it.toDomain() }
        val todayEntries = recentEntries.filter { it.date == today }.associateBy { it.habitId }

        val todayCompleted = habits.count { h ->
            val e = todayEntries[h.id]
            if (h.usesIntervalStreak() && e?.isSkipped != true &&
                h.intervalCreditsBefore(today, recentEntries) >= h.intervalTimes
            ) return@count true
            e ?: return@count false
            !e.isSkipped && if (h.isBadHabit) e.completedCount == 0
                           else e.completedCount >= h.targetCount
        }

        var totalRate = 0f
        var bestStreak = 0
        habits.forEach { h ->
            val s = getHabitStats(h.id) ?: return@forEach
            totalRate += s.completionRate
            if (s.currentStreak > bestStreak) bestStreak = s.currentStreak
        }

        return OverallStats(
            totalHabits = habits.size,
            activeHabits = habits.size,
            averageCompletionRate = totalRate / habits.size,
            bestStreak = bestStreak,
            todayCompleted = todayCompleted,
            todayTotal = todayTotal,
        )
    }

    override suspend fun getAllMarkedAtTimes(): List<String?> =
        dao.getAllMarkedAtTimes()

    override suspend fun getAllEntriesForHabit(habitId: Long): List<HabitEntry> =
        dao.getAllEntriesForHabit(habitId).map { it.toDomain() }

    private fun countScheduledDays(habit: Habit): Int {
        val today = LocalDate.now()
        val start = habit.startDate
        if (start > today) return 0
        if (habit.frequency == HabitFrequency.INTERVAL) {
            val elapsed = ChronoUnit.DAYS.between(start, today) + 1
            return ((elapsed * habit.intervalTimes + habit.intervalDays - 1) / habit.intervalDays).toInt()
        }
        var count = 0
        var current = start
        while (!current.isAfter(today)) {
            if (habit.isScheduledOn(current)) {
                count++
            }
            current = current.plusDays(1)
        }
        return count
    }

    private fun completedDates(habit: Habit, entries: List<HabitEntry>, until: LocalDate): List<LocalDate> =
        if (habit.isBadHabit) {
            // Для табу «выполненный» день = запланированный день БЕЗ срыва
            val relapsedDates = entries.filter { it.completedCount > 0 }.map { it.date }.toHashSet()
            buildList {
                var d = habit.startDate
                while (!d.isAfter(until)) {
                    val scheduled = habit.isScheduledOn(d)
                    if (scheduled && d !in relapsedDates) add(d)
                    d = d.plusDays(1)
                }
            }
        } else {
            entries
                .filter { it.completedCount >= habit.targetCount }
                .map { it.date }
                .sorted()
        }

    private fun frozenDates(entries: List<HabitEntry>): Set<LocalDate> =
        entries.filter { it.isFrozen || it.isStreakSafeSkip }.map { it.date }.toHashSet()

    private fun calculateCurrentStreak(
        habit: Habit,
        completedDates: List<LocalDate>,
        frozenDates: Set<LocalDate>,
        asOf: LocalDate,
    ): Int {
        if (habit.usesIntervalStreak()) return intervalStreaks(habit, completedDates.toHashSet(), frozenDates, asOf).first
        val datesSet = completedDates.toHashSet()
        var streak = 0
        // День asOf ещё может быть отмечен: если он пуст, серия считается с предыдущего дня
        var current = if (asOf in datesSet || asOf in frozenDates) asOf else asOf.minusDays(1)
        if (current.isBefore(habit.startDate)) return 0
        while (true) {
            val isScheduled = habit.isScheduledOn(current)
            if (!isScheduled) {
                current = current.minusDays(1)
                if (current.isBefore(habit.startDate)) break
                continue
            }
            when {
                current in datesSet -> {
                    streak++
                    current = current.minusDays(1)
                    if (current.isBefore(habit.startDate)) break
                }
                // Замороженный день не разрывает серию, но и не увеличивает счётчик
                current in frozenDates -> {
                    current = current.minusDays(1)
                    if (current.isBefore(habit.startDate)) break
                }
                else -> break
            }
        }
        return streak
    }

    // Те же правила, что в calculateCurrentStreak: дни вне расписания, заморозки и неотмеченный сегодняшний день серию не рвут
    private fun calculateLongestStreak(
        habit: Habit,
        completedDates: List<LocalDate>,
        frozenDates: Set<LocalDate>,
    ): Int {
        if (habit.usesIntervalStreak()) return intervalStreaks(habit, completedDates.toHashSet(), frozenDates, LocalDate.now()).second
        val datesSet = completedDates.toHashSet()
        val today = LocalDate.now()
        var longest = 0
        var current = 0
        var d = habit.startDate
        while (!d.isAfter(today)) {
            val isScheduled = habit.isScheduledOn(d)
            if (isScheduled) {
                when {
                    d in datesSet -> {
                        current++
                        if (current > longest) longest = current
                    }
                    d in frozenDates || d == today -> Unit
                    else -> current = 0
                }
            }
            d = d.plusDays(1)
        }
        return longest
    }

    private fun Habit.usesIntervalStreak() = frequency == HabitFrequency.INTERVAL && !isBadHabit

    // Возвращает (текущая, лучшая) серию в отметках. Серия рвётся в день без зачёта, если в окне из
    // intervalDays дней, заканчивающемся этим днём, меньше intervalTimes зачётов. Первые дни после
    // startDate, пока окно не заполнено, и ещё не закрытый день until не проверяются.
    private fun intervalStreaks(
        habit: Habit,
        completed: Set<LocalDate>,
        frozen: Set<LocalDate>,
        until: LocalDate,
    ): Pair<Int, Int> {
        var current = 0
        var longest = 0
        var d = habit.startDate
        while (!d.isAfter(until)) {
            when {
                d in completed -> {
                    current++
                    if (current > longest) longest = current
                }
                d in frozen || d == until -> Unit
                else -> {
                    val windowStart = d.minusDays(habit.intervalDays - 1L)
                    if (!windowStart.isBefore(habit.startDate)) {
                        val credits = (0 until habit.intervalDays).count { offset ->
                            val day = windowStart.plusDays(offset.toLong())
                            day in completed || day in frozen
                        }
                        if (credits < habit.intervalTimes) current = 0
                    }
                }
            }
            d = d.plusDays(1)
        }
        return current to longest
    }
}
