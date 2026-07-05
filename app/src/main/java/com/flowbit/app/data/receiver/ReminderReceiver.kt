package com.flowbit.app.data.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.flowbit.app.FlowbitApp
import com.flowbit.app.R
import com.flowbit.app.presentation.MainActivity
import com.flowbit.app.widget.WidgetEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L)
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        if (habitId == -1L) return

        val pending = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = EntryPointAccessors
                    .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
                    .database()
                val habit = db.habitDao().getHabitById(habitId)

                val title = if (habit != null) "${habit.emoji} ${habit.name}" else "Flowbit"
                val text = if (habit != null) "Время выполнить привычку" else "Не забудьте отметить привычку!"

                val openIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(EXTRA_HABIT_ID, habitId)
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    habitId.toInt(),
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

                val notification = NotificationCompat.Builder(context, FlowbitApp.REMINDER_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build()

                val manager = context.getSystemService(NotificationManager::class.java)
                manager.notify(reminderId.toInt(), notification)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val EXTRA_HABIT_ID = "extra_habit_id"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
    }
}
