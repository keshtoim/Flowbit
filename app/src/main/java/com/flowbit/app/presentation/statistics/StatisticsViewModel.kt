package com.flowbit.app.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowbit.app.domain.model.BestTimeData
import com.flowbit.app.domain.model.HabitStats
import com.flowbit.app.domain.model.OverallStats
import com.flowbit.app.domain.model.PeriodComparison
import com.flowbit.app.domain.model.WeekdayInsight
import com.flowbit.app.domain.repository.HabitRepository
import com.flowbit.app.domain.usecase.stats.GetHabitStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class StatisticsUiState(
    val habitStats: List<HabitStats> = emptyList(),
    val overallStats: OverallStats? = null,
    val weekdayInsight: WeekdayInsight? = null,
    val periodComparison: PeriodComparison? = null,
    val bestTimeData: BestTimeData? = null,
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val getHabitStats: GetHabitStatsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val habits = habitRepository.getActiveHabits().first()
            val stats = habits.mapNotNull { getHabitStats.forHabit(it.id) }
            val overall = getHabitStats.overall()
            val markedTimes = habitRepository.getAllMarkedAtTimes()

            val insight = computeWeekdayInsight(stats)
            val comparison = computePeriodComparison(stats)
            val bestTime = computeBestTime(markedTimes)

            _uiState.update {
                it.copy(
                    habitStats = stats,
                    overallStats = overall,
                    weekdayInsight = insight,
                    periodComparison = comparison,
                    bestTimeData = bestTime,
                )
            }
        }
    }

    private fun computeWeekdayInsight(stats: List<HabitStats>): WeekdayInsight? {
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

    private fun computePeriodComparison(stats: List<HabitStats>): PeriodComparison? {
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

    private fun computeBestTime(times: List<String?>): BestTimeData? {
        if (times.isEmpty()) return null
        val hourCounts = mutableMapOf<Int, Int>()
        times.forEach { t ->
            val hour = t?.substringBefore(":")?.toIntOrNull() ?: return@forEach
            hourCounts[hour] = (hourCounts[hour] ?: 0) + 1
        }
        val peak = hourCounts.maxByOrNull { it.value }?.key
        return BestTimeData(hourCounts = hourCounts, peakHour = peak)
    }
}
