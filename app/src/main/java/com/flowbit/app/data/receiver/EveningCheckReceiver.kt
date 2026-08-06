package com.flowbit.app.data.receiver

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.flowbit.app.FlowbitApp
import com.flowbit.app.R
import com.flowbit.app.presentation.MainActivity
import com.flowbit.app.widget.WidgetEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class EveningCheckReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = EntryPointAccessors
                    .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
                    .database()
                val today = LocalDate.now().toString()
                val habits = db.habitDao().getActiveHabits().first()
                if (habits.isEmpty()) return@launch
                val entries = db.habitDao().getEntriesForDate(today).first()
                    .associateBy { it.habitId }
                val remaining = habits.count { habit ->
                    val entry = entries[habit.id]
                    entry?.isSkipped != true && (entry?.completedCount ?: 0) < habit.targetCount
                }
                if (remaining > 0) {
                    val openIntent = PendingIntent.getActivity(
                        context, 0,
                        Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        },
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                    )
                    val notification = NotificationCompat.Builder(context, FlowbitApp.REMINDER_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Не забудь о привычках!")
                        .setContentText("Осталось $remaining ${plural(remaining)} на сегодня")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setContentIntent(openIntent)
                        .build()
                    context.getSystemService(NotificationManager::class.java)
                        .notify(NOTIF_ID, notification)
                }
            } finally {
                schedule(context)
                pending.finish()
            }
        }
    }

    companion object {
        private const val NOTIF_ID = 10001

        const val PREFS_NAME = "flowbit_prefs"
        const val KEY_EVENING_ENABLED = "evening_enabled"
        const val KEY_EVENING_HOUR = "evening_hour"
        const val KEY_EVENING_MINUTE = "evening_minute"

        fun schedule(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val enabled = prefs.getBoolean(KEY_EVENING_ENABLED, true)
            if (!enabled) {
                cancel(context)
                return
            }
            val hour = prefs.getInt(KEY_EVENING_HOUR, 20)
            val minute = prefs.getInt(KEY_EVENING_MINUTE, 0)
            val now = LocalDateTime.now()
            var trigger = now.toLocalDate().atTime(hour, minute)
            if (!trigger.isAfter(now)) trigger = trigger.plusDays(1)
            val millis = trigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val am = context.getSystemService(AlarmManager::class.java)
            val pi = pendingIntent(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
            }
        }

        fun cancel(context: Context) {
            context.getSystemService(AlarmManager::class.java).cancel(pendingIntent(context))
        }

        private fun pendingIntent(context: Context) = PendingIntent.getBroadcast(
            context, NOTIF_ID,
            Intent(context, EveningCheckReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        private fun plural(n: Int) = when {
            n % 10 == 1 && n % 100 != 11 -> "привычка"
            n % 10 in 2..4 && n % 100 !in 12..14 -> "привычки"
            else -> "привычек"
        }
    }
}
