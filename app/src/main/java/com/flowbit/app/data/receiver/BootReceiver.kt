package com.flowbit.app.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.flowbit.app.domain.usecase.reminder.ScheduleReminderUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var scheduleReminderUseCase: ScheduleReminderUseCase

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        CoroutineScope(Dispatchers.IO).launch {
            scheduleReminderUseCase.rescheduleAll()
        }
    }
}
