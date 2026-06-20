package com.flowbit.app.domain.usecase.habit

import com.flowbit.app.domain.model.HabitEntry
import com.flowbit.app.domain.repository.HabitRepository
import java.time.LocalDate
import javax.inject.Inject

class ToggleHabitEntryUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    suspend operator fun invoke(habitId: Long, date: LocalDate, targetCount: Int) {
        val existing = repository.getEntryForDate(habitId, date)
        if (existing == null) {
            repository.upsertEntry(HabitEntry(habitId = habitId, date = date, completedCount = 1))
        } else {
            val next = (existing.completedCount + 1) % (targetCount + 1)
            if (next == 0) {
                repository.deleteEntry(existing)
            } else {
                repository.upsertEntry(existing.copy(completedCount = next))
            }
        }
    }
}
