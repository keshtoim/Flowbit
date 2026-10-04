package com.flowbit.app.domain.usecase.stats

import com.flowbit.app.domain.model.BestTimeData
import javax.inject.Inject

class GetBestTimeUseCase @Inject constructor() {

    operator fun invoke(markedAtTimes: List<String?>): BestTimeData? {
        if (markedAtTimes.isEmpty()) return null
        val hourCounts = mutableMapOf<Int, Int>()
        markedAtTimes.forEach { t ->
            val hour = t?.substringBefore(":")?.toIntOrNull() ?: return@forEach
            hourCounts[hour] = (hourCounts[hour] ?: 0) + 1
        }
        if (hourCounts.isEmpty()) return null
        val peak = hourCounts.maxByOrNull { it.value }?.key
        return BestTimeData(hourCounts = hourCounts, peakHour = peak)
    }
}
