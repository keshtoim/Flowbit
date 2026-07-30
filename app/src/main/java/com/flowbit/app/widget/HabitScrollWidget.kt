package com.flowbit.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.flowbit.app.data.database.entity.HabitEntryEntity
import com.flowbit.app.domain.model.Habit
import com.flowbit.app.presentation.MainActivity
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class HabitScrollWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = EntryPointAccessors
            .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
            .database()

        val today = LocalDate.now()
        val habits = db.habitDao().getActiveHabits().first().map { it.toDomain() }
        val entriesForToday = db.habitDao().getEntriesForDate(today.toString()).first()
            .associateBy { it.habitId }

        provideContent {
            GlanceTheme {
                ScrollWidgetContent(context, habits, entriesForToday, today)
            }
        }
    }
}

@Composable
private fun ScrollWidgetContent(
    context: Context,
    habits: List<Habit>,
    entriesForToday: Map<Long, HabitEntryEntity>,
    today: LocalDate,
) {
    val openIntent = Intent(context, MainActivity::class.java)
    val done = habits.count { habit ->
        val entry = entriesForToday[habit.id]
        (entry?.completedCount ?: 0) >= habit.targetCount
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .clickable(actionStartActivity(openIntent)),
    ) {
        // ── Шапка ────────────────────────────────────────────────────────────
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "📅",
                style = TextStyle(fontSize = 16.sp),
            )
            Spacer(GlanceModifier.width(6.dp))
            Text(
                text = "Сегодня",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface,
                ),
                modifier = GlanceModifier.defaultWeight(),
            )
            Box(
                modifier = GlanceModifier
                    .cornerRadius(10.dp)
                    .background(GlanceTheme.colors.primary)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "$done/${habits.size}",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onPrimary,
                    ),
                )
            }
        }

        Spacer(GlanceModifier.height(2.dp))

        // ── Список привычек ───────────────────────────────────────────────────
        if (habits.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Нет привычек",
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = GlanceTheme.colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    ),
                )
            }
        } else {
            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                items(habits, itemId = { it.id }) { habit ->
                    val entry = entriesForToday[habit.id]
                    val count = entry?.completedCount ?: 0
                    val isCompleted = count >= habit.targetCount
                    val isSkipped = entry?.isSkipped == true
                    val habitColor = parseScrollWidgetColor(habit.color.hex)

                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = GlanceModifier
                                .size(26.dp)
                                .cornerRadius(13.dp)
                                .background(ColorProvider(habitColor.copy(alpha = 0.18f))),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = habit.emoji,
                                style = TextStyle(fontSize = 13.sp),
                            )
                        }
                        Spacer(GlanceModifier.width(8.dp))
                        Text(
                            text = habit.name,
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = if (isCompleted || isSkipped)
                                    GlanceTheme.colors.onSurfaceVariant
                                else
                                    GlanceTheme.colors.onSurface,
                            ),
                            modifier = GlanceModifier.defaultWeight(),
                            maxLines = 1,
                        )
                        Spacer(GlanceModifier.width(4.dp))
                        Text(
                            text = when {
                                isSkipped -> "—"
                                isCompleted -> "✓"
                                habit.targetCount > 1 -> "$count/${habit.targetCount}"
                                else -> "○"
                            },
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCompleted) GlanceTheme.colors.primary
                                        else GlanceTheme.colors.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        }
    }
}

private fun parseScrollWidgetColor(hex: String): androidx.compose.ui.graphics.Color = try {
    androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(hex))
} catch (e: Exception) {
    androidx.compose.ui.graphics.Color(0xFF00E5C0)
}

class HabitScrollWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HabitScrollWidget()
}
