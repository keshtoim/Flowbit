package com.flowbit.app.presentation.habits.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowbit.app.domain.model.HabitStats
import com.flowbit.app.domain.repository.HabitRepository
import com.flowbit.app.domain.usecase.stats.GetHabitStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
)

@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    private val getHabitStats: GetHabitStatsUseCase,
    private val repository: HabitRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitDetailUiState())
    val uiState: StateFlow<HabitDetailUiState> = _uiState.asStateFlow()

    private var currentHabitId: Long = 0

    fun load(habitId: Long) {
        currentHabitId = habitId
        viewModelScope.launch {
            val stats = getHabitStats.forHabit(habitId)
            val entry = repository.getEntryForDate(habitId, LocalDate.now())
            _uiState.update {
                it.copy(
                    stats = stats,
                    todayNote = entry?.note,
                    isTodaySkipped = entry?.isSkipped ?: false,
                )
            }
        }
    }

    fun openNoteDialog() {
        _uiState.update { it.copy(noteDialogOpen = true, noteInput = it.todayNote ?: "") }
    }

    fun dismissNoteDialog() {
        _uiState.update { it.copy(noteDialogOpen = false) }
    }

    fun onNoteInputChange(text: String) {
        _uiState.update { it.copy(noteInput = text) }
    }

    fun saveNote() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val note = _uiState.value.noteInput.trim().takeIf { it.isNotEmpty() }
            val existing = repository.getEntryForDate(currentHabitId, today)
            if (existing != null) {
                repository.upsertEntry(existing.copy(note = note))
            }
            _uiState.update { it.copy(todayNote = note, noteDialogOpen = false) }
        }
    }

    fun skipToday() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val existing = repository.getEntryForDate(currentHabitId, today)
            val entry = existing?.copy(completedCount = 0, isSkipped = true)
                ?: com.flowbit.app.domain.model.HabitEntry(
                    habitId = currentHabitId, date = today, completedCount = 0, isSkipped = true,
                )
            repository.upsertEntry(entry)
            _uiState.update { it.copy(isTodaySkipped = true) }
        }
    }

    fun requestUnSkip() {
        _uiState.update { it.copy(unSkipConfirmOpen = true) }
    }

    fun confirmUnSkip() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val existing = repository.getEntryForDate(currentHabitId, today)
            if (existing != null) {
                repository.upsertEntry(existing.copy(isSkipped = false))
            }
            _uiState.update { it.copy(isTodaySkipped = false, unSkipConfirmOpen = false) }
        }
    }

    fun dismissUnSkip() {
        _uiState.update { it.copy(unSkipConfirmOpen = false) }
    }

    fun openDeleteConfirm() {
        _uiState.update { it.copy(deleteConfirmOpen = true) }
    }

    fun dismissDeleteConfirm() {
        _uiState.update { it.copy(deleteConfirmOpen = false) }
    }

    fun confirmDelete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            val habit = repository.getHabitById(currentHabitId) ?: return@launch
            repository.deleteHabit(habit)
            onDeleted()
        }
    }
}
