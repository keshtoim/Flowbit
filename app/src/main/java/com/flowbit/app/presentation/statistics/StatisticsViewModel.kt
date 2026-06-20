package com.flowbit.app.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowbit.app.domain.model.HabitStats
import com.flowbit.app.domain.model.OverallStats
import com.flowbit.app.domain.repository.HabitRepository
import com.flowbit.app.domain.usecase.stats.GetHabitStatsUseCase
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
            _uiState.update { it.copy(habitStats = stats, overallStats = overall) }
        }
    }
}
