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
            _uiState.update { it.copy(stats = stats, todayNote = entry?.note) }
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
}
