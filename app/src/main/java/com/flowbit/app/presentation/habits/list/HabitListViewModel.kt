package com.flowbit.app.presentation.habits.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowbit.app.domain.model.GroupingMode
import com.flowbit.app.domain.model.HabitTag
import com.flowbit.app.domain.repository.TagRepository
import com.flowbit.app.domain.usecase.habit.DecreaseHabitEntryUseCase
import com.flowbit.app.presentation.habits.list.randomQuote
import com.flowbit.app.domain.usecase.habit.GetHabitsForDateUseCase
import com.flowbit.app.domain.usecase.habit.HabitForDate
import com.flowbit.app.domain.usecase.habit.ToggleHabitEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HabitListUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val habits: List<HabitForDate> = emptyList(),
    val isLoading: Boolean = false,
    val motivationQuote: String? = null,
    val groupingMode: GroupingMode = GroupingMode.NONE,
)

@HiltViewModel
class HabitListViewModel @Inject constructor(
    private val getHabitsForDate: GetHabitsForDateUseCase,
    private val toggleHabitEntry: ToggleHabitEntryUseCase,
    private val decreaseHabitEntry: DecreaseHabitEntryUseCase,
    private val tagRepository: TagRepository,
) : ViewModel() {

    val allTags: StateFlow<List<HabitTag>> = tagRepository.getAllTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(HabitListUiState())
    val uiState: StateFlow<HabitListUiState> = _uiState.asStateFlow()

    private val selectedDate = MutableStateFlow(LocalDate.now())

    init {
        selectedDate
            .flatMapLatest { date -> getHabitsForDate(date) }
            .onEach { habits ->
                _uiState.update { it.copy(habits = habits, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun onDateSelected(date: LocalDate) {
        selectedDate.value = date
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun toggleHabit(habitId: Long) {
        viewModelScope.launch {
            val habit = _uiState.value.habits.find { it.habit.id == habitId } ?: return@launch
            toggleHabitEntry(habitId, _uiState.value.selectedDate, habit.habit.targetCount)
        }
    }

    fun decreaseHabit(habitId: Long) {
        viewModelScope.launch {
            decreaseHabitEntry(habitId, _uiState.value.selectedDate)
        }
    }

    fun showMotivation() {
        _uiState.update { it.copy(motivationQuote = randomQuote()) }
    }

    fun dismissMotivation() {
        _uiState.update { it.copy(motivationQuote = null) }
    }

    fun setGroupingMode(mode: GroupingMode) {
        _uiState.update { it.copy(groupingMode = mode) }
    }
}
