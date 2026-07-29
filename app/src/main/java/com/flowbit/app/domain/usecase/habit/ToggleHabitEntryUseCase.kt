package com.flowbit.app.domain.usecase.habit

import com.flowbit.app.domain.model.HabitEntry
import com.flowbit.app.domain.repository.HabitRepository
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ToggleHabitEntryUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    suspend operator fun invoke(habitId: Long, date: LocalDate, targetCount: Int) {
        val existing = repository.getEntryForDate(habitId, date)
        val now = LocalTime.now().format(timeFmt)
        if (existing == null) {
            val markedAt = if (targetCount == 1) now else null
            repository.upsertEntry(
                HabitEntry(habitId = habitId, date = date, completedCount = 1, markedAt = markedAt)
            )
        } else {
            val next = (existing.completedCount + 1) % (targetCount + 1)
            if (next == 0) {
                repository.deleteEntry(existing)
            } else {
                val markedAt = if (next >= targetCount) now else existing.markedAt
                repository.upsertEntry(existing.copy(completedCount = next, markedAt = markedAt))
            }
        }
    }
}
