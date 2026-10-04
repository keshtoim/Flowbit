package com.flowbit.app.domain.usecase.habit

import com.flowbit.app.domain.repository.HabitRepository
import java.time.LocalDate
import javax.inject.Inject

class UnskipHabitUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    suspend operator fun invoke(habitId: Long, date: LocalDate) {
        val existing = repository.getEntryForDate(habitId, date) ?: return
        repository.upsertEntry(existing.copy(isSkipped = false))
    }
}
