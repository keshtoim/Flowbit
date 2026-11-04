package com.flowbit.app.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class HabitsWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = EntryPointAccessors
            .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
            .database()
        val today = LocalDate.now()
        val habits = db.habitDao().getWidgetHabits().first().map { it.toDomain() }
        val items = loadWidgetHabitItems(db, habits, today)
        val glanceColors = buildGlanceColors(context)

        provideContent {
            GlanceTheme(colors = glanceColors) {
                WidgetHabitList(
                    context = context,
                    items = items,
                    today = today,
                    emptyText = "Включите «Показывать в виджете» у нужных привычек",
                )
            }
        }
    }
}
