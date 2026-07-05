package com.flowbit.app.domain.usecase.habit

import com.flowbit.app.domain.repository.HabitRepository
import java.time.LocalDate
import javax.inject.Inject

class DecreaseHabitEntryUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    suspend operator fun invoke(habitId: Long, date: LocalDate) {
        val existing = repository.getEntryForDate(habitId, date) ?: return
        if (existing.completedCount <= 1) {
            repository.deleteEntry(existing)
        } else {
            repository.upsertEntry(existing.copy(completedCount = existing.completedCount - 1))
        }
    }
}
