package com.flowbit.app.data.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.flowbit.app.widget.HabitsWidget
import com.flowbit.app.widget.HabitScrollWidget
import com.flowbit.app.widget.HeatmapWidget
import com.flowbit.app.widget.LockScreenWidget
import com.flowbit.app.widget.SingleHabitWidget
import com.flowbit.app.widget.TodaySummaryWidget
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        HabitsWidget().updateAll(applicationContext)
        HabitScrollWidget().updateAll(applicationContext)
        SingleHabitWidget().updateAll(applicationContext)
        TodaySummaryWidget().updateAll(applicationContext)
        HeatmapWidget().updateAll(applicationContext)
        LockScreenWidget().updateAll(applicationContext)
        return Result.success()
    }

    companion object {
        private const val PERIODIC_WORK_NAME = "widget_update_periodic"
        private const val ONE_SHOT_WORK_NAME = "widget_update_once"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                15, TimeUnit.MINUTES,
            ).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }

        fun enqueueOnce(context: Context) {
            val request = OneTimeWorkRequestBuilder<WidgetUpdateWorker>().build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                ONE_SHOT_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }
    }
}
