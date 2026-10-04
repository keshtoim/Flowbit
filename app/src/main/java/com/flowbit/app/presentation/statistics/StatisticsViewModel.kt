package com.flowbit.app.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowbit.app.domain.model.BestTimeData
import com.flowbit.app.domain.model.HabitCorrelation
import com.flowbit.app.domain.model.HabitStats
import com.flowbit.app.domain.model.OverallStats
import com.flowbit.app.domain.model.PeriodComparison
import com.flowbit.app.domain.model.WeekdayInsight
import com.flowbit.app.domain.repository.HabitRepository
import com.flowbit.app.domain.usecase.stats.GetBestTimeUseCase
import com.flowbit.app.domain.usecase.stats.GetHabitCorrelationsUseCase
import com.flowbit.app.domain.usecase.stats.GetHabitStatsUseCase
import com.flowbit.app.domain.usecase.stats.GetPeriodComparisonUseCase
import com.flowbit.app.domain.usecase.stats.GetWeekdayInsightUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatisticsUiState(
    val habitStats: List<HabitStats> = emptyList(),
    val overallStats: OverallStats? = null,
    val weekdayInsight: WeekdayInsight? = null,
    val periodComparison: PeriodComparison? = null,
    val bestTimeData: BestTimeData? = null,
    val popularHabit: HabitStats? = null,
    val rareHabit: HabitStats? = null,
    val averageCompletionPct: Int = 0,
    val topCorrelations: List<HabitCorrelation> = emptyList(),
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val getHabitStats: GetHabitStatsUseCase,
    private val getWeekdayInsight: GetWeekdayInsightUseCase,
    private val getPeriodComparison: GetPeriodComparisonUseCase,
    private val getHabitCorrelations: GetHabitCorrelationsUseCase,
    private val getBestTime: GetBestTimeUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val habits = habitRepository.getActiveHabits().first()
            val stats = habits.mapNotNull { getHabitStats.forHabit(it.id) }
            val overall = getHabitStats.overall()
            val markedTimes = habitRepository.getAllMarkedAtTimes()

            val insight = getWeekdayInsight(stats)
            val comparison = getPeriodComparison(stats)
            val bestTime = getBestTime(markedTimes)

            val activeStats = stats.filter { it.completionRate > 0f || it.totalCompletions >= 0 }
            val popular = activeStats.maxByOrNull { it.completionRate }
            val rare = activeStats.filter { it.totalCompletions > 0 }.minByOrNull { it.completionRate }
            val avgPct = if (activeStats.isEmpty()) 0
            else (activeStats.map { it.completionRate }.average() * 100).toInt()

            val correlations = getHabitCorrelations(stats)

            _uiState.update {
                it.copy(
                    habitStats = stats,
                    overallStats = overall,
                    weekdayInsight = insight,
                    periodComparison = comparison,
                    bestTimeData = bestTime,
                    popularHabit = popular,
                    rareHabit = rare,
                    averageCompletionPct = avgPct,
                    topCorrelations = correlations,
                )
            }
        }
    }
}
