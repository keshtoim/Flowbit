package com.flowbit.app.presentation.habits.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowbit.app.domain.model.HabitStats
import com.flowbit.app.domain.usecase.stats.GetHabitStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HabitDetailUiState(val stats: HabitStats? = null)

@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    private val getHabitStats: GetHabitStatsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitDetailUiState())
    val uiState: StateFlow<HabitDetailUiState> = _uiState.asStateFlow()

    fun load(habitId: Long) {
        viewModelScope.launch {
            val stats = getHabitStats.forHabit(habitId)
            _uiState.update { it.copy(stats = stats) }
        }
    }
}
