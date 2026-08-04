package com.flowbit.app.presentation.weekly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowbit.app.domain.model.Habit
import com.flowbit.app.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

data class HabitWeekData(
    val habit: Habit,
    val days: List<Boolean>,
    val doneDays: Int,
)

data class WeeklySummaryUiState(
    val weekStart: LocalDate = LocalDate.now(),
    val weekEnd: LocalDate = LocalDate.now(),
    val habitWeeks: List<HabitWeekData> = emptyList(),
    val totalDone: Int = 0,
    val totalPossible: Int = 0,
)

@HiltViewModel
class WeeklySummaryViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeeklySummaryUiState())
    val uiState: StateFlow<WeeklySummaryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val today = LocalDate.now()
            val dow = today.dayOfWeek.value
            val weekStart = today.minusDays((dow - DayOfWeek.MONDAY.value).toLong())
            val weekEnd = weekStart.plusDays(6)

            val habits = habitRepository.getActiveHabits().first()
                .filter { !it.isBadHabit }
            val entries = habitRepository.getEntriesForDateRange(weekStart, weekEnd)

            val weekDays = (0..6).map { weekStart.plusDays(it.toLong()) }

            val habitWeeks = habits.map { habit ->
                val days = weekDays.map { day ->
                    val entry = entries.find { it.habitId == habit.id && it.date == day }
                    (entry?.completedCount ?: 0) >= habit.targetCount
                }
                HabitWeekData(
                    habit = habit,
                    days = days,
                    doneDays = days.count { it },
                )
            }

            val totalDone = habitWeeks.sumOf { it.doneDays }
            val totalPossible = habitWeeks.sumOf { wk ->
                weekDays.count { day ->
                    !day.isAfter(today) &&
                        (wk.habit.frequency == com.flowbit.app.domain.model.HabitFrequency.DAILY ||
                         day.dayOfWeek in wk.habit.scheduledDays)
                }
            }

            _uiState.update {
                it.copy(
                    weekStart = weekStart,
                    weekEnd = weekEnd,
                    habitWeeks = habitWeeks,
                    totalDone = totalDone,
                    totalPossible = totalPossible,
                )
            }
        }
    }
}
