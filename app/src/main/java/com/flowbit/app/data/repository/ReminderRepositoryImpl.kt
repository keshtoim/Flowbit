package com.flowbit.app.data.repository

import com.flowbit.app.data.database.dao.ReminderDao
import com.flowbit.app.data.database.entity.ReminderEntity
import com.flowbit.app.domain.model.HabitReminder
import com.flowbit.app.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ReminderRepositoryImpl @Inject constructor(
    private val dao: ReminderDao,
) : ReminderRepository {

    override fun getRemindersForHabit(habitId: Long): Flow<List<HabitReminder>> =
        dao.getRemindersForHabit(habitId).map { list -> list.map { it.toDomain() } }

    override suspend fun insertReminder(reminder: HabitReminder): Long =
        dao.insertReminder(ReminderEntity.fromDomain(reminder))

    override suspend fun updateReminder(reminder: HabitReminder) =
        dao.updateReminder(ReminderEntity.fromDomain(reminder))

    override suspend fun deleteReminder(reminder: HabitReminder) =
        dao.deleteReminder(ReminderEntity.fromDomain(reminder))

    override suspend fun deleteRemindersForHabit(habitId: Long) =
        dao.deleteRemindersForHabit(habitId)

    override suspend fun getAllActiveReminders(): List<HabitReminder> =
        dao.getAllActiveReminders().map { it.toDomain() }
}
