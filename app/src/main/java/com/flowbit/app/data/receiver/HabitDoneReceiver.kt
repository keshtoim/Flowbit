package com.flowbit.app.data.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.flowbit.app.data.database.entity.HabitEntryEntity
import com.flowbit.app.widget.WidgetEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class HabitDoneReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        if (habitId == -1L) return

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = EntryPointAccessors
                    .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
                    .database()
                val habit = db.habitDao().getHabitById(habitId) ?: return@launch
                val today = LocalDate.now().toString()
                val existing = db.habitDao().getEntryForDate(habitId, today)
                val newEntry = existing?.copy(completedCount = habit.targetCount, isSkipped = false)
                    ?: HabitEntryEntity(
                        habitId = habitId,
                        date = today,
                        completedCount = habit.targetCount,
                    )
                db.habitDao().insertEntry(newEntry)

                if (notificationId != -1) {
                    context.getSystemService(NotificationManager::class.java).cancel(notificationId)
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val EXTRA_HABIT_ID = "extra_habit_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
