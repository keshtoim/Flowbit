package com.flowbit.app.domain.usecase.stats

import com.flowbit.app.domain.model.HabitStats
import com.flowbit.app.domain.model.OverallStats
import com.flowbit.app.domain.repository.HabitRepository
import javax.inject.Inject

class GetHabitStatsUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    suspend fun forHabit(habitId: Long): HabitStats? = repository.getHabitStats(habitId)
    suspend fun overall(): OverallStats = repository.getOverallStats()
}
