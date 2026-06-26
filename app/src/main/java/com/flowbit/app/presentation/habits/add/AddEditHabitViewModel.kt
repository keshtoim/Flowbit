package com.flowbit.app.presentation.habits.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowbit.app.domain.model.Habit
import com.flowbit.app.domain.model.HabitColor
import com.flowbit.app.domain.model.HabitFrequency
import com.flowbit.app.domain.model.HabitReminder
import com.flowbit.app.domain.repository.HabitRepository
import com.flowbit.app.domain.repository.ReminderRepository
import com.flowbit.app.domain.usecase.reminder.ScheduleReminderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class AddEditHabitUiState(
    val name: String = "",
    val emoji: String = "✅",
    val color: HabitColor = HabitColor.TEAL,
    val targetCount: Int = 1,
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val scheduledDays: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
    val startDate: LocalDate = LocalDate.now(),
    val showInWidget: Boolean = false,
    val reminders: List<HabitReminder> = emptyList(),
    val isSaved: Boolean = false,
    val nameError: String? = null,
)

@HiltViewModel
class AddEditHabitViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val reminderRepository: ReminderRepository,
    private val scheduleReminder: ScheduleReminderUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditHabitUiState())
    val uiState: StateFlow<AddEditHabitUiState> = _uiState.asStateFlow()

    private var editingHabitId: Long? = null

    fun loadHabit(habitId: Long?) {
        if (habitId == null) return
        viewModelScope.launch {
            val habit = habitRepository.getHabitById(habitId) ?: return@launch
            editingHabitId = habit.id
            // Напоминания хранятся в отдельной таблице — загружаем явно
            val reminders = reminderRepository.getRemindersForHabit(habitId).first()
            _uiState.update {
                it.copy(
                    name = habit.name,
                    emoji = habit.emoji,
                    color = habit.color,
                    targetCount = habit.targetCount,
                    frequency = habit.frequency,
                    scheduledDays = habit.scheduledDays,
                    startDate = habit.startDate,
                    showInWidget = habit.showInWidget,
                    reminders = reminders,
                )
            }
        }
    }

    fun onNameChange(name: String) = _uiState.update { it.copy(name = name, nameError = null) }
    fun onEmojiChange(emoji: String) = _uiState.update { it.copy(emoji = emoji) }
    fun onColorChange(color: HabitColor) = _uiState.update { it.copy(color = color) }
    fun onTargetCountChange(count: Int) = _uiState.update { it.copy(targetCount = count.coerceIn(1, 20)) }
    fun onFrequencyChange(freq: HabitFrequency) = _uiState.update { it.copy(frequency = freq) }
    fun onShowInWidgetChange(show: Boolean) = _uiState.update { it.copy(showInWidget = show) }
    fun onStartDateChange(date: LocalDate) = _uiState.update { it.copy(startDate = date) }

    fun onDayToggle(day: DayOfWeek) {
        _uiState.update { state ->
            val days = state.scheduledDays.toMutableSet()
            if (day in days) days.remove(day) else days.add(day)
            state.copy(scheduledDays = days)
        }
    }

    fun onAddReminder(time: LocalTime) {
        val reminder = HabitReminder(time = time, habitId = editingHabitId ?: 0)
        _uiState.update { it.copy(reminders = it.reminders + reminder) }
    }

    fun onRemoveReminder(reminder: HabitReminder) {
        _uiState.update { it.copy(reminders = it.reminders - reminder) }
    }

    fun onToggleReminder(reminder: HabitReminder) {
        _uiState.update {
            it.copy(reminders = it.reminders.map { r ->
                if (r == reminder) r.copy(isEnabled = !r.isEnabled) else r
            })
        }
    }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Введите название") }
            return
        }

        viewModelScope.launch {
            val habit = Habit(
                id = editingHabitId ?: 0,
                name = state.name.trim(),
                emoji = state.emoji,
                color = state.color,
                targetCount = state.targetCount,
                frequency = state.frequency,
                scheduledDays = state.scheduledDays,
                startDate = state.startDate,
                showInWidget = state.showInWidget,
            )

            val savedId = if (editingHabitId == null) {
                habitRepository.insertHabit(habit)
            } else {
                habitRepository.updateHabit(habit)
                editingHabitId!!
            }

            reminderRepository.deleteRemindersForHabit(savedId)
            state.reminders.forEach { reminder ->
                scheduleReminder(reminder.copy(habitId = savedId))
            }

            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
