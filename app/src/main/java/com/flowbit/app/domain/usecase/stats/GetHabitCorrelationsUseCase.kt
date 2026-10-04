package com.flowbit.app.domain.usecase.stats

import com.flowbit.app.domain.model.HabitCorrelation
import com.flowbit.app.domain.model.HabitStats
import javax.inject.Inject

class GetHabitCorrelationsUseCase @Inject constructor() {

    operator fun invoke(stats: List<HabitStats>): List<HabitCorrelation> {
        if (stats.size < 2) return emptyList()
        val result = mutableListOf<HabitCorrelation>()
        for (i in stats.indices) {
            for (j in i + 1 until stats.size) {
                val a = stats[i]
                val b = stats[j]
                val setA = a.completedDates.toHashSet()
                val setB = b.completedDates.toHashSet()
                val shared = setA.count { it in setB }
                val total = (setA + setB).size
                if (total >= 7 && shared > 0) {
                    result.add(
                        HabitCorrelation(
                            habitA = a.habitName, habitB = b.habitName,
                            emojiA = a.habitEmoji, emojiB = b.habitEmoji,
                            sharedDays = shared, totalDays = total,
                        )
                    )
                }
            }
        }
        return result.sortedByDescending { it.rate }.take(3)
    }
}
