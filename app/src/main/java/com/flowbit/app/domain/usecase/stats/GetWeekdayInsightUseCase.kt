package com.flowbit.app.domain.usecase.stats

import com.flowbit.app.domain.model.HabitStats
import com.flowbit.app.domain.model.WeekdayInsight
import java.time.LocalDate
import javax.inject.Inject

class GetWeekdayInsightUseCase @Inject constructor() {

    operator fun invoke(stats: List<HabitStats>): WeekdayInsight? {
        if (stats.isEmpty()) return null
        val today = LocalDate.now()
        val last30 = (0..29).map { today.minusDays(it.toLong()) }
        val weekdays = last30.filter { it.dayOfWeek.value <= 5 }
        val weekends = last30.filter { it.dayOfWeek.value > 5 }

        val habitCount = stats.size
        val wdCompletions = weekdays.sumOf { day -> stats.count { day in it.completedDates } }
        val weCompletions = weekends.sumOf { day -> stats.count { day in it.completedDates } }

        val wdRate = if (weekdays.isEmpty()) 0f
                     else wdCompletions.toFloat() / (weekdays.size * habitCount)
        val weRate = if (weekends.isEmpty()) 0f
                     else weCompletions.toFloat() / (weekends.size * habitCount)

        val wdPct = (wdRate * 100).toInt()
        val wePct = (weRate * 100).toInt()
        val text = when {
            wdRate - weRate > 0.15f ->
                "Вы выполняете $wdPct% привычек по будням и только $wePct% в выходные"
            weRate - wdRate > 0.15f ->
                "Вы активнее в выходные ($wePct%) чем по будням ($wdPct%)"
            else ->
                "Вы одинаково стабильны в будни ($wdPct%) и выходные ($wePct%)"
        }
        return WeekdayInsight(weekdayRate = wdRate, weekendRate = weRate, insightText = text)
    }
}
