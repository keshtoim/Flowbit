package com.flowbit.app.domain.usecase.stats

import com.flowbit.app.domain.model.HabitStats
import com.flowbit.app.domain.model.PeriodComparison
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class GetPeriodComparisonUseCase @Inject constructor() {

    operator fun invoke(stats: List<HabitStats>): PeriodComparison? {
        if (stats.isEmpty()) return null
        val today = LocalDate.now()
        val thisMonStart = today.withDayOfMonth(1)
        val lastMonStart = thisMonStart.minusMonths(1)
        val lastMonEnd = thisMonStart.minusDays(1)

        val dayOfWeek = today.dayOfWeek.value
        val thisWeekStart = today.minusDays((dayOfWeek - DayOfWeek.MONDAY.value).toLong())
        val lastWeekStart = thisWeekStart.minusWeeks(1)
        val lastWeekEnd = thisWeekStart.minusDays(1)

        fun rate(start: LocalDate, end: LocalDate): Float {
            if (start.isAfter(end)) return 0f
            val days = (0..(end.toEpochDay() - start.toEpochDay()).toInt())
                .map { start.plusDays(it.toLong()) }
            val completions = days.sumOf { day -> stats.count { day in it.completedDates } }
            return completions.toFloat() / (days.size * stats.size)
        }

        val fmt = DateTimeFormatter.ofPattern("d MMM")
        return PeriodComparison(
            thisWeekRate = rate(thisWeekStart, today),
            lastWeekRate = rate(lastWeekStart, lastWeekEnd),
            thisMonthRate = rate(thisMonStart, today),
            lastMonthRate = rate(lastMonStart, lastMonEnd),
            thisWeekLabel = "${thisWeekStart.format(fmt)} – ${today.format(fmt)}",
            lastWeekLabel = "${lastWeekStart.format(fmt)} – ${lastWeekEnd.format(fmt)}",
        )
    }
}
