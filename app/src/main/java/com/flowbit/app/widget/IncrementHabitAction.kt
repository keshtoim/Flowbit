package com.flowbit.app.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.flowbit.app.data.database.entity.HabitEntryEntity
import dagger.hilt.android.EntryPointAccessors
import java.time.LocalDate

class IncrementHabitAction : ActionCallback {

    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val habitId = parameters[habitIdKey] ?: return
        val db = EntryPointAccessors
            .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
            .database()
        val habit = db.habitDao().getHabitById(habitId) ?: return
        val today = LocalDate.now().toString()
        val existing = db.habitDao().getEntryForDate(habitId, today)
        val currentCount = existing?.completedCount ?: 0
        if (currentCount >= habit.targetCount) return
        val newEntry = existing?.copy(completedCount = currentCount + 1)
            ?: HabitEntryEntity(habitId = habitId, date = today, completedCount = 1)
        db.habitDao().insertEntry(newEntry)
        SingleHabitWidget().update(context, glanceId)
    }

    companion object {
        val habitIdKey = ActionParameters.Key<Long>("habit_id")
    }
}
