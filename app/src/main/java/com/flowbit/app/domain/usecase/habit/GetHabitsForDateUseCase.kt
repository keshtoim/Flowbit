package com.flowbit.app.domain.usecase.habit

import com.flowbit.app.domain.model.Habit
import com.flowbit.app.domain.model.HabitEntry
import com.flowbit.app.domain.model.HabitFrequency
import com.flowbit.app.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

data class HabitForDate(
    val habit: Habit,
    val date: LocalDate,
    val entry: HabitEntry?,
    val isScheduledForDate: Boolean,
    val recentDays: List<Boolean> = emptyList(),
    val streak: Int = 0,
    val intervalCredits: Int = 0,
) {
    val isSkippedForDay: Boolean
        get() = entry?.isSkipped == true || entry?.isStreakSafeSkip == true

    val isCompletedToday: Boolean
        get() = (entry?.completedCount ?: 0) >= habit.targetCount

    val isCoveredByInterval: Boolean
        get() = habit.frequency == HabitFrequency.INTERVAL && intervalCredits >= habit.intervalTimes

    val intervalProgress: Int
        get() = intervalCredits + if (isCompletedToday) 1 else 0

    val isDone: Boolean
        get() = !isSkippedForDay && (isCompletedToday || isCoveredByInterval)
}

class GetHabitsForDateUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    operator fun invoke(date: LocalDate): Flow<List<HabitForDate>> {
        return combine(
            repository.getActiveHabits(),
            repository.getEntriesForDate(date),
        ) { habits, entries ->
            val entryMap = entries.associateBy { it.habitId }
            habits
                .filter { habit -> habit.startDate <= date }
                .filter { habit -> habit.isScheduledOn(date) }
                .map { habit ->
                    HabitForDate(
                        habit = habit,
                        date = date,
                        entry = entryMap[habit.id],
                        isScheduledForDate = true,
                    )
                }
        }
    }
}
