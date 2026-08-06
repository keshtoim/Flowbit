package com.flowbit.app.presentation.habits.list

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowbit.app.domain.model.GroupingMode
import com.flowbit.app.domain.model.HabitEntry
import com.flowbit.app.domain.model.HabitTag
import com.flowbit.app.domain.repository.HabitRepository
import com.flowbit.app.domain.repository.TagRepository
import com.flowbit.app.domain.usecase.habit.DecreaseHabitEntryUseCase
import com.flowbit.app.presentation.habits.list.randomQuote
import com.flowbit.app.domain.usecase.habit.GetHabitsForDateUseCase
import com.flowbit.app.domain.usecase.habit.HabitForDate
import com.flowbit.app.domain.usecase.habit.ToggleHabitEntryUseCase
import com.flowbit.app.presentation.ShortcutHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

data class HabitListUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val habits: List<HabitForDate> = emptyList(),
    val isLoading: Boolean = false,
    val motivationQuote: String? = null,
    val groupingMode: GroupingMode = GroupingMode.NONE,
    val unSkipRequestId: Long? = null,
    val timerHabitId: Long? = null,
    val timerRemaining: Int = 0,
    val showConfetti: Boolean = false,
    val confettiShownDates: Set<LocalDate> = emptySet(),
    val weekDelta: Int? = null,
)

@HiltViewModel
class HabitListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getHabitsForDate: GetHabitsForDateUseCase,
    private val toggleHabitEntry: ToggleHabitEntryUseCase,
    private val decreaseHabitEntry: DecreaseHabitEntryUseCase,
    private val tagRepository: TagRepository,
    private val habitRepository: HabitRepository,
) : ViewModel() {

    val allTags: StateFlow<List<HabitTag>> = tagRepository.getAllTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(HabitListUiState())
    val uiState: StateFlow<HabitListUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    private val selectedDate = MutableStateFlow(LocalDate.now())

    init {
        selectedDate
            .flatMapLatest { date -> getHabitsForDate(date) }
            .onEach { habits ->
                val date = selectedDate.value
                val isToday = date == LocalDate.now()
                val allDone = habits.isNotEmpty() && habits.all { hfd ->
                    hfd.entry?.isSkipped == true ||
                        (hfd.entry?.completedCount ?: 0) >= hfd.habit.targetCount
                }
                _uiState.update { state ->
                    val canTrigger = isToday && allDone && date !in state.confettiShownDates
                    state.copy(
                        habits = habits,
                        isLoading = false,
                        showConfetti = canTrigger,
                        confettiShownDates = if (canTrigger) state.confettiShownDates + date
                                             else state.confettiShownDates,
                    )
                }
                ShortcutHelper.updateShortcuts(context, habits.map { it.habit })
            }
            .launchIn(viewModelScope)

        computeWeekDelta()
    }

    private fun computeWeekDelta() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val dow = today.dayOfWeek.value  // Mon=1...Sun=7
            val thisWeekStart = today.minusDays((dow - DayOfWeek.MONDAY.value).toLong())
            val lastWeekEnd = thisWeekStart.minusDays(1)
            val lastWeekStart = lastWeekEnd.minusDays((dow - DayOfWeek.MONDAY.value).toLong())

            val thisEntries = habitRepository.getEntriesForDateRange(thisWeekStart, today)
            val lastEntries = habitRepository.getEntriesForDateRange(lastWeekStart, lastWeekEnd)

            val thisCount = thisEntries.count { it.completedCount > 0 && !it.isSkipped }
            val lastCount = lastEntries.count { it.completedCount > 0 && !it.isSkipped }

            val delta = if (lastCount > 0 || thisCount > 0) thisCount - lastCount else null
            _uiState.update { it.copy(weekDelta = delta) }
        }
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

    fun skipHabit(habitId: Long) {
        viewModelScope.launch {
            val date = _uiState.value.selectedDate
            val existing = habitRepository.getEntryForDate(habitId, date)
            val entry = existing?.copy(completedCount = 0, isSkipped = true)
                ?: HabitEntry(habitId = habitId, date = date, completedCount = 0, isSkipped = true)
            habitRepository.upsertEntry(entry)
        }
    }

    fun requestUnSkip(habitId: Long) {
        _uiState.update { it.copy(unSkipRequestId = habitId) }
    }

    fun confirmUnSkip() {
        val habitId = _uiState.value.unSkipRequestId ?: return
        viewModelScope.launch {
            val date = _uiState.value.selectedDate
            val existing = habitRepository.getEntryForDate(habitId, date)
            if (existing != null) {
                habitRepository.upsertEntry(existing.copy(isSkipped = false))
            }
            _uiState.update { it.copy(unSkipRequestId = null) }
        }
    }

    fun dismissUnSkip() {
        _uiState.update { it.copy(unSkipRequestId = null) }
    }

    fun hideConfetti() {
        _uiState.update { it.copy(showConfetti = false) }
    }

    fun startTimer(habitId: Long) {
        val habit = _uiState.value.habits.find { it.habit.id == habitId }?.habit ?: return
        if (habit.timerSeconds <= 0) return
        timerJob?.cancel()
        _uiState.update { it.copy(timerHabitId = habitId, timerRemaining = habit.timerSeconds) }
        timerJob = viewModelScope.launch {
            var remaining = habit.timerSeconds
            while (remaining > 0) {
                delay(1000L)
                remaining--
                _uiState.update { it.copy(timerRemaining = remaining) }
            }
            toggleHabit(habitId)
            _uiState.update { it.copy(timerHabitId = null, timerRemaining = 0) }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _uiState.update { it.copy(timerHabitId = null, timerRemaining = 0) }
    }

    fun setCount(habitId: Long, count: Int) {
        viewModelScope.launch {
            val date = _uiState.value.selectedDate
            val clamped = count.coerceAtLeast(0)
            val existing = habitRepository.getEntryForDate(habitId, date)
            val entry = existing?.copy(completedCount = clamped, isSkipped = false)
                ?: HabitEntry(habitId = habitId, date = date, completedCount = clamped, isSkipped = false)
            habitRepository.upsertEntry(entry)
        }
    }

    fun persistReorder(habits: List<HabitForDate>) {
        viewModelScope.launch {
            habits.forEachIndexed { index, habitForDate ->
                habitRepository.updateSortOrder(habitForDate.habit.id, index)
            }
        }
    }
}
