package com.flowbit.app.presentation.habits.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowbit.app.domain.model.HabitStats
import com.flowbit.app.domain.repository.HabitRepository
import com.flowbit.app.domain.repository.PreferencesRepository
import com.flowbit.app.domain.usecase.habit.SkipHabitUseCase
import com.flowbit.app.domain.usecase.habit.UnskipHabitUseCase
import com.flowbit.app.domain.usecase.stats.GetHabitStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

data class HabitDetailUiState(
    val stats: HabitStats? = null,
    val todayNote: String? = null,
    val noteDialogOpen: Boolean = false,
    val noteInput: String = "",
    val isTodaySkipped: Boolean = false,
    val deleteConfirmOpen: Boolean = false,
    val unSkipConfirmOpen: Boolean = false,
    val noteHistory: List<Pair<LocalDate, String>> = emptyList(),
    val isFrozenToday: Boolean = false,
    val freezeCountThisWeek: Int = 0,
    val hourlyCompletions: Map<Int, Int> = emptyMap(),
)

@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getHabitStats: GetHabitStatsUseCase,
    private val repository: HabitRepository,
    private val skipHabit: SkipHabitUseCase,
    private val unskipHabit: UnskipHabitUseCase,
    prefs: PreferencesRepository,
) : ViewModel() {

    val analyticsStyle: StateFlow<String> = prefs.analyticsStyle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "A")

    private val _uiState = MutableStateFlow(HabitDetailUiState())
    val uiState: StateFlow<HabitDetailUiState> = _uiState.asStateFlow()

    private val currentHabitId: Long = savedStateHandle["habitId"] ?: 0L

    init {
        viewModelScope.launch { loadData() }
    }

    private suspend fun loadData() {
        val habitId = currentHabitId
        val stats = getHabitStats.forHabit(habitId)
        val today = LocalDate.now()
        val entry = repository.getEntryForDate(habitId, today)
        val allEntries = repository.getAllEntriesForHabit(habitId)

        val history = allEntries
            .filter { !it.note.isNullOrBlank() }
            .sortedByDescending { it.date }
            .map { it.date to it.note!! }

        val hourlyCompletions = buildMap<Int, Int> {
            allEntries.forEach { e ->
                val hour = e.markedAt?.substringBefore(":")?.toIntOrNull() ?: return@forEach
                if (e.completedCount > 0) put(hour, (get(hour) ?: 0) + 1)
            }
        }

        val dow = today.dayOfWeek.value
        val weekStart = today.minusDays((dow - DayOfWeek.MONDAY.value).toLong())
        val freezeCountThisWeek = allEntries.count {
            it.isFrozen && !it.date.isBefore(weekStart) && !it.date.isAfter(today)
        }

        _uiState.update {
            it.copy(
                stats = stats,
                todayNote = entry?.note,
                isTodaySkipped = entry?.isSkipped ?: false,
                noteHistory = history,
                isFrozenToday = entry?.isFrozen ?: false,
                freezeCountThisWeek = freezeCountThisWeek,
                hourlyCompletions = hourlyCompletions,
            )
        }
    }

    fun openNoteDialog() {
        _uiState.update { it.copy(noteDialogOpen = true, noteInput = it.todayNote ?: "") }
    }

    fun dismissNoteDialog() { _uiState.update { it.copy(noteDialogOpen = false) } }
    fun onNoteInputChange(text: String) { _uiState.update { it.copy(noteInput = text) } }

    fun saveNote() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val note = _uiState.value.noteInput.trim().takeIf { it.isNotEmpty() }
            val existing = repository.getEntryForDate(currentHabitId, today)
            if (existing != null) repository.upsertEntry(existing.copy(note = note))
            val history = repository.getAllEntriesForHabit(currentHabitId)
                .filter { !it.note.isNullOrBlank() }
                .sortedByDescending { it.date }
                .map { it.date to it.note!! }
            _uiState.update { it.copy(todayNote = note, noteDialogOpen = false, noteHistory = history) }
        }
    }

    fun skipToday() {
        viewModelScope.launch {
            skipHabit(currentHabitId, LocalDate.now())
            _uiState.update { it.copy(isTodaySkipped = true) }
        }
    }

    fun requestUnSkip() { _uiState.update { it.copy(unSkipConfirmOpen = true) } }

    fun confirmUnSkip() {
        viewModelScope.launch {
            unskipHabit(currentHabitId, LocalDate.now())
            _uiState.update { it.copy(isTodaySkipped = false, unSkipConfirmOpen = false) }
        }
    }

    fun dismissUnSkip() { _uiState.update { it.copy(unSkipConfirmOpen = false) } }

    fun freezeStreak() {
        viewModelScope.launch {
            repository.freezeStreak(currentHabitId, LocalDate.now())
            loadData()
        }
    }

    fun openDeleteConfirm() { _uiState.update { it.copy(deleteConfirmOpen = true) } }
    fun dismissDeleteConfirm() { _uiState.update { it.copy(deleteConfirmOpen = false) } }

    fun confirmDelete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            val habit = repository.getHabitById(currentHabitId) ?: return@launch
            repository.deleteHabit(habit)
            onDeleted()
        }
    }
}
