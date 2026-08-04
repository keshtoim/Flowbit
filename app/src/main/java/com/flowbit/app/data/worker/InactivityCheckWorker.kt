package com.flowbit.app.data.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.flowbit.app.FlowbitApp
import com.flowbit.app.R
import com.flowbit.app.presentation.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class InactivityCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastOpen = prefs.getLong(KEY_LAST_OPEN, 0L)
        val now = System.currentTimeMillis()
        val hoursSinceOpen = (now - lastOpen) / (1000L * 60 * 60)

        if (lastOpen > 0 && hoursSinceOpen >= INACTIVITY_THRESHOLD_HOURS) {
            sendNotification()
        }
        return Result.success()
    }

    private fun sendNotification() {
        val openIntent = PendingIntent.getActivity(
            applicationContext, 0,
            Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(applicationContext, FlowbitApp.REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Скучаешь по привычкам? 👋")
            .setContentText("Ты не заходил уже больше дня — самое время проверить прогресс!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(openIntent)
            .build()
        applicationContext.getSystemService(NotificationManager::class.java)
            .notify(NOTIF_ID, notification)
    }

    companion object {
        const val PREFS_NAME = "flowbit_prefs"
        const val KEY_LAST_OPEN = "last_open_ms"
        private const val INACTIVITY_THRESHOLD_HOURS = 25L
        private const val NOTIF_ID = 10003
        private const val WORK_NAME = "inactivity_check"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<InactivityCheckWorker>(
                12, TimeUnit.HOURS,
            ).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
