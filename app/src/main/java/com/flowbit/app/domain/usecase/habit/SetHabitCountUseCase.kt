package com.flowbit.app.domain.usecase.habit

import com.flowbit.app.domain.model.HabitEntry
import com.flowbit.app.domain.repository.HabitRepository
import java.time.LocalDate
import javax.inject.Inject

class SetHabitCountUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    suspend operator fun invoke(habitId: Long, date: LocalDate, count: Int) {
        val clamped = count.coerceAtLeast(0)
        val existing = repository.getEntryForDate(habitId, date)
        val entry = existing?.copy(completedCount = clamped, isSkipped = false)
            ?: HabitEntry(habitId = habitId, date = date, completedCount = clamped, isSkipped = false)
        repository.upsertEntry(entry)
    }
}
