package com.flowbit.app.domain.usecase.habit

import com.flowbit.app.domain.model.HabitEntry
import com.flowbit.app.domain.repository.HabitRepository
import java.time.LocalDate
import javax.inject.Inject

class SkipHabitUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    suspend operator fun invoke(habitId: Long, date: LocalDate) {
        val existing = repository.getEntryForDate(habitId, date)
        val entry = existing?.copy(completedCount = 0, isSkipped = true)
            ?: HabitEntry(habitId = habitId, date = date, completedCount = 0, isSkipped = true)
        repository.upsertEntry(entry)
    }
}
