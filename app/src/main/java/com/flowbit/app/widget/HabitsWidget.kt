package com.flowbit.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
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
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.material3.ColorProviders
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.flowbit.app.data.database.entity.HabitEntryEntity
import com.flowbit.app.domain.model.Habit
import com.flowbit.app.presentation.MainActivity
import com.flowbit.app.presentation.theme.DarkColorScheme
import com.flowbit.app.presentation.theme.LightColorScheme
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class HabitsWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = EntryPointAccessors
            .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
            .database()

        val today = LocalDate.now()
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekEnd = weekStart.plusDays(6)
        val weekDates = (0..6).map { weekStart.plusDays(it.toLong()) }

        val habits = db.habitDao().getWidgetHabits().first().map { it.toDomain() }
        val allEntries = db.habitDao().getEntriesForDateRange(weekStart.toString(), weekEnd.toString())

        // habitId -> date -> entry
        val entryMap: Map<Long, Map<LocalDate, HabitEntryEntity>> = allEntries
            .groupBy { it.habitId }
            .mapValues { (_, entries) ->
                entries.associateBy { LocalDate.parse(it.date) }
            }

        provideContent {
            GlanceTheme(colors = ColorProviders(light = LightColorScheme, dark = DarkColorScheme)) {
                WidgetContent(
                    context = context,
                    habits = habits,
                    weekDates = weekDates,
                    today = today,
                    entryMap = entryMap,
                )
            }
        }
    }
}

@Composable
private fun WidgetContent(
    context: Context,
    habits: List<Habit>,
    weekDates: List<LocalDate>,
    today: LocalDate,
    entryMap: Map<Long, Map<LocalDate, HabitEntryEntity>>,
) {
    val openIntent = Intent(context, MainActivity::class.java)
    val dayNames = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .padding(10.dp)
            .clickable(actionStartActivity(openIntent)),
    ) {
        // Header row: days of week
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left column placeholder (emoji + name area)
            Spacer(GlanceModifier.width(110.dp))
            dayNames.forEachIndexed { i, name ->
                val isToday = weekDates[i] == today
                Box(
                    modifier = GlanceModifier.defaultWeight().height(22.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = name,
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isToday)
                                GlanceTheme.colors.primary
                            else
                                GlanceTheme.colors.onSurfaceVariant,
                        ),
                    )
                }
            }
        }

        if (habits.isEmpty()) {
            Spacer(GlanceModifier.height(8.dp))
            Text(
                text = "Нет привычек для виджета",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.onSurfaceVariant,
                ),
            )
        } else {
            habits.forEach { habit ->
                val habitEntries = entryMap[habit.id] ?: emptyMap()
                val habitColor = parseHabitColor(habit.color.hex)

                Spacer(GlanceModifier.height(6.dp))
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Emoji circle
                    Box(
                        modifier = GlanceModifier
                            .size(26.dp)
                            .cornerRadius(13.dp)
                            .background(ColorProvider(habitColor.copy(alpha = 0.2f))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = habit.emoji,
                            style = TextStyle(fontSize = 14.sp),
                        )
                    }

                    Spacer(GlanceModifier.width(4.dp))

                    // Habit name (truncated)
                    Box(
                        modifier = GlanceModifier.width(72.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(
                            text = if (habit.name.length > 9) habit.name.take(8) + "…" else habit.name,
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = GlanceTheme.colors.onSurface,
                            ),
                            maxLines = 1,
                        )
                    }

                    // Day circles
                    weekDates.forEach { date ->
                        val entry = habitEntries[date]
                        val completedCount = entry?.completedCount ?: 0
                        val isCompleted = completedCount >= habit.targetCount
                        val isToday = date == today
                        val isFuture = date.isAfter(today)

                        Box(
                            modifier = GlanceModifier.defaultWeight().height(22.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            DayCircle(
                                isCompleted = isCompleted,
                                isToday = isToday,
                                isFuture = isFuture,
                                habitColor = habitColor,
                                count = completedCount,
                                target = habit.targetCount,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCircle(
    isCompleted: Boolean,
    isToday: Boolean,
    isFuture: Boolean,
    habitColor: Color,
    count: Int,
    target: Int,
) {
    val bgColor = when {
        isCompleted -> ColorProvider(habitColor)
        isToday -> ColorProvider(habitColor.copy(alpha = 0.3f))
        isFuture -> ColorProvider(Color(0x0F000000))
        else -> ColorProvider(habitColor.copy(alpha = 0.15f))
    }

    Box(
        modifier = GlanceModifier
            .size(20.dp)
            .cornerRadius(10.dp)
            .background(bgColor),
        contentAlignment = Alignment.Center,
    ) {
        if (target > 1 && !isCompleted && !isFuture) {
            Text(
                text = "$count",
                style = TextStyle(
                    fontSize = 8.sp,
                    color = ColorProvider(habitColor),
                ),
            )
        }
    }
}

private fun parseHabitColor(hex: String): Color = try {
    val colorInt = android.graphics.Color.parseColor(hex)
    Color(colorInt)
} catch (e: Exception) {
    Color(0xFF00E5C0)
}
