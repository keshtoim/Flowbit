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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

class WeeklyReportReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = EntryPointAccessors
                    .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
                    .database()
                val today = LocalDate.now()
                val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val habits = db.habitDao().getActiveHabits().first()
                if (habits.isEmpty()) return@launch
                val entries = db.habitDao().getEntriesForDateRange(weekStart.toString(), today.toString())
                val totalPossible = habits.size * 7
                val done = entries.count { entry ->
                    habits.find { it.id == entry.habitId }
                        ?.let { entry.completedCount >= it.targetCount } == true
                }
                val pct = if (totalPossible > 0) done * 100 / totalPossible else 0
                val emoji = when {
                    pct >= 80 -> "🏆"
                    pct >= 60 -> "💪"
                    pct >= 40 -> "📈"
                    else -> "💡"
                }
                val openIntent = PendingIntent.getActivity(
                    context, 0,
                    Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    },
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
                val notification = NotificationCompat.Builder(context, FlowbitApp.REMINDER_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("$emoji Итоги недели")
                    .setContentText("Выполнено $done из $totalPossible задач ($pct%)")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setContentIntent(openIntent)
                    .build()
                context.getSystemService(NotificationManager::class.java).notify(NOTIF_ID, notification)
            } finally {
                schedule(context)
                pending.finish()
            }
        }
    }

    companion object {
        private const val NOTIF_ID = 10002

        fun schedule(context: Context) {
            val now = LocalDateTime.now()
            var trigger = now.toLocalDate()
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                .atTime(20, 0)
            if (!trigger.isAfter(now)) trigger = trigger.plusWeeks(1)
            val millis = trigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val am = context.getSystemService(AlarmManager::class.java)
            val pi = pendingIntent(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
            }
        }

        private fun pendingIntent(context: Context) = PendingIntent.getBroadcast(
            context, NOTIF_ID,
            Intent(context, WeeklyReportReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
