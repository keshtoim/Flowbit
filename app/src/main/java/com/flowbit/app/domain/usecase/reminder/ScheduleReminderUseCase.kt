package com.flowbit.app.domain.usecase.reminder

import com.flowbit.app.domain.model.HabitReminder
import com.flowbit.app.domain.repository.ReminderRepository
import com.flowbit.app.data.scheduler.ReminderScheduler
import javax.inject.Inject

class ScheduleReminderUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val scheduler: ReminderScheduler,
) {
    suspend operator fun invoke(reminder: HabitReminder) {
        val id = reminderRepository.insertReminder(reminder)
        scheduler.schedule(reminder.copy(id = id))
    }

    suspend fun cancel(reminder: HabitReminder) {
        reminderRepository.deleteReminder(reminder)
        scheduler.cancel(reminder)
    }

    suspend fun rescheduleAll() {
        val reminders = reminderRepository.getAllActiveReminders()
        reminders.forEach { scheduler.schedule(it) }
    }
}
