package com.flowbit.app.presentation.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowbit.app.domain.model.Habit
import com.flowbit.app.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val repository: HabitRepository,
) : ViewModel() {

    val archivedHabits: StateFlow<List<Habit>> = repository.getArchivedHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun unarchive(habitId: Long) {
        viewModelScope.launch { repository.unarchiveHabit(habitId) }
    }

    fun delete(habit: Habit) {
        viewModelScope.launch { repository.deleteHabit(habit) }
    }
}
