package com.flowbit.app.domain.usecase.habit

import com.flowbit.app.domain.model.Habit
import com.flowbit.app.domain.model.HabitEntry
import com.flowbit.app.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

data class HabitForDate(
    val habit: Habit,
    val entry: HabitEntry?,
    val isScheduledForDate: Boolean,
)

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
                .filter { habit -> habit.isScheduledFor(date) }
                .map { habit ->
                    HabitForDate(
                        habit = habit,
                        entry = entryMap[habit.id],
                        isScheduledForDate = true,
                    )
                }
        }
    }

    private fun Habit.isScheduledFor(date: LocalDate): Boolean {
        return when (frequency) {
            com.flowbit.app.domain.model.HabitFrequency.DAILY -> true
            com.flowbit.app.domain.model.HabitFrequency.WEEKLY,
            com.flowbit.app.domain.model.HabitFrequency.CUSTOM -> date.dayOfWeek in scheduledDays
        }
    }
}
