package com.flowbit.app.domain.repository

import com.flowbit.app.domain.model.HabitReminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getRemindersForHabit(habitId: Long): Flow<List<HabitReminder>>
    suspend fun insertReminder(reminder: HabitReminder): Long
    suspend fun updateReminder(reminder: HabitReminder)
    suspend fun deleteReminder(reminder: HabitReminder)
    suspend fun deleteRemindersForHabit(habitId: Long)
    suspend fun getAllActiveReminders(): List<HabitReminder>
}
