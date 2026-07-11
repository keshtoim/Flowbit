package com.flowbit.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.flowbit.app.presentation.MainActivity
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class SingleHabitWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val ep = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        val db = ep.database()
        val today = LocalDate.now()
        val todayStr = today.toString()

        // Читаем выбранную привычку из state, иначе fallback на первую с showInWidget или первую активную
        val state = androidx.glance.appwidget.state.getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
        val pinnedId = state[PINNED_HABIT_KEY]

        val habit = if (pinnedId != null) {
            db.habitDao().getHabitById(pinnedId)
        } else {
            db.habitDao().getWidgetHabits().first().firstOrNull()
                ?: db.habitDao().getActiveHabits().first().firstOrNull()
        }

        val entry = habit?.let { db.habitDao().getEntryForDate(it.id, todayStr) }
        val completedCount = entry?.completedCount ?: 0
        val isDone = habit != null && completedCount >= habit.targetCount
        val allEntries = habit?.let { db.habitDao().getAllEntriesForHabit(it.id) } ?: emptyList()
        val completedDates = allEntries
            .filter { it.completedCount >= (habit?.targetCount ?: 1) }
            .map { LocalDate.parse(it.date) }
            .toHashSet()
        var streak = 0
        var cur = today
        while (habit != null && (cur in completedDates || (cur == today && isDone))) {
            if (cur in completedDates) { streak++; cur = cur.minusDays(1) }
            else break
        }

        provideContent {
            GlanceTheme {
                val bg = GlanceTheme.colors.widgetBackground
                val primary = GlanceTheme.colors.primary
                val onSurface = GlanceTheme.colors.onSurface
                val onSurfaceVariant = GlanceTheme.colors.onSurfaceVariant

                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(bg)
                        .cornerRadius(20.dp)
                        .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
                    contentAlignment = Alignment.Center,
                ) {
                    if (habit == null) {
                        Column(
                            modifier = GlanceModifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("🌱", style = TextStyle(fontSize = 28.sp))
                            Spacer(GlanceModifier.height(8.dp))
                            Text(
                                text = "Нет привычек",
                                style = TextStyle(fontSize = 13.sp, color = onSurfaceVariant),
                            )
                        }
                    } else {
                        Column(
                            modifier = GlanceModifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(habit.emoji, style = TextStyle(fontSize = 28.sp))
                            Spacer(GlanceModifier.height(6.dp))
                            Text(
                                text = habit.name,
                                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = onSurface),
                                maxLines = 1,
                            )
                            Spacer(GlanceModifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isDone) "✓" else "$completedCount/${habit.targetCount}",
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDone) primary else onSurfaceVariant,
                                    ),
                                )
                                if (streak > 0) {
                                    Spacer(GlanceModifier.width(8.dp))
                                    Text("🔥$streak", style = TextStyle(fontSize = 14.sp, color = onSurface))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        val PINNED_HABIT_KEY = longPreferencesKey("pinned_habit_id")
    }
}

class SingleHabitWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SingleHabitWidget()
}
