package com.flowbit.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
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
import com.flowbit.app.data.database.FlowbitDatabase
import com.flowbit.app.domain.model.Habit
import com.flowbit.app.domain.model.HabitFrequency
import com.flowbit.app.presentation.MainActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class WidgetHabitItem(
    val habit: Habit,
    val count: Int,
    val isDone: Boolean,
    val isSkipped: Boolean,
    val history: List<Boolean>,
)

suspend fun loadWidgetHabitItems(db: FlowbitDatabase, habits: List<Habit>, today: LocalDate): List<WidgetHabitItem> {
    val scheduled = habits.filter { it.startDate <= today && it.isScheduledOn(today) }
    val lookback = maxOf(7, scheduled.maxOfOrNull { it.intervalDays } ?: 0)
    val entries = db.habitDao()
        .getEntriesForDateRange(today.minusDays(lookback - 1L).toString(), today.toString())
        .map { it.toDomain() }
        .groupBy { it.habitId }
    return scheduled.map { habit ->
        val byDate = entries[habit.id].orEmpty().associateBy { it.date }
        val todayEntry = byDate[today]
        val skipped = todayEntry?.isSkipped == true || todayEntry?.isStreakSafeSkip == true
        val count = if (skipped) 0 else (todayEntry?.completedCount ?: 0)
        val covered = habit.frequency == HabitFrequency.INTERVAL &&
            habit.intervalCreditsBefore(today, entries[habit.id].orEmpty()) >= habit.intervalTimes
        WidgetHabitItem(
            habit = habit,
            count = count,
            isDone = !skipped && if (habit.isBadHabit) count == 0 else (count >= habit.targetCount || covered),
            isSkipped = skipped,
            history = (6 downTo 0).map { back ->
                val e = byDate[today.minusDays(back.toLong())]
                e != null && !e.isSkipped && !e.isStreakSafeSkip && e.completedCount >= habit.targetCount
            },
        )
    }
}

@Composable
fun WidgetHabitList(context: Context, items: List<WidgetHabitItem>, today: LocalDate, emptyText: String) {
    val openIntent = Intent(context, MainActivity::class.java)
    val counted = items.filter { !it.habit.isBadHabit && !it.isSkipped }
    val done = counted.count { it.isDone }
    val showHistory = LocalSize.current.width >= 250.dp

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(24.dp)
            .background(GlanceTheme.colors.background)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .clickable(actionStartActivity(openIntent)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Сегодня",
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GlanceTheme.colors.onBackground),
            )
            Spacer(GlanceModifier.width(8.dp))
            Text(
                text = today.format(DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault())),
                style = TextStyle(fontSize = 12.sp, color = GlanceTheme.colors.onSurfaceVariant),
                modifier = GlanceModifier.defaultWeight(),
                maxLines = 1,
            )
            Text(
                text = "$done / ${counted.size}",
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GlanceTheme.colors.onBackground),
            )
        }
        Spacer(GlanceModifier.height(8.dp))
        LinearProgressIndicator(
            progress = if (counted.isEmpty()) 0f else done.toFloat() / counted.size,
            modifier = GlanceModifier.fillMaxWidth().height(4.dp).padding(horizontal = 4.dp),
            color = GlanceTheme.colors.primary,
            backgroundColor = GlanceTheme.colors.surfaceVariant,
        )
        Spacer(GlanceModifier.height(8.dp))

        if (items.isEmpty()) {
            Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = emptyText,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = GlanceTheme.colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    ),
                )
            }
        } else {
            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                items(items, itemId = { it.habit.id }) { item ->
                    Column {
                        WidgetHabitRow(item, showHistory, openIntent)
                        Spacer(GlanceModifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetHabitRow(item: WidgetHabitItem, showHistory: Boolean, openIntent: Intent) {
    val habit = item.habit
    val habitColor = runCatching { Color(android.graphics.Color.parseColor(habit.effectiveColorHex)) }
        .getOrDefault(Color(0xFF2DD4BF))
    val dimmed = (item.isDone && !habit.isBadHabit) || item.isSkipped

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(48.dp)
            .cornerRadius(14.dp)
            .background(GlanceTheme.colors.surfaceVariant)
            .padding(start = 8.dp, end = 6.dp)
            .clickable(actionStartActivity(openIntent)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = GlanceModifier
                .size(32.dp)
                .cornerRadius(10.dp)
                .background(ColorProvider(habitColor.copy(alpha = 0.2f))),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = habit.emoji, style = TextStyle(fontSize = 16.sp))
        }
        Spacer(GlanceModifier.width(10.dp))
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = habit.name,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (dimmed) GlanceTheme.colors.onSurfaceVariant else GlanceTheme.colors.onSurface,
                ),
                maxLines = 1,
            )
            val meta = when {
                item.isSkipped -> "Пропущено"
                habit.isBadHabit -> if (item.isDone) "Чисто" else "Срыв"
                habit.frequency == HabitFrequency.INTERVAL -> "${habit.intervalTimes} за ${habit.intervalDays} дн."
                habit.targetCount > 1 -> "${item.count} / ${habit.targetCount}" + (habit.unit?.let { " $it" } ?: "")
                else -> null
            }
            if (meta != null) {
                Text(
                    text = meta,
                    style = TextStyle(fontSize = 11.sp, color = GlanceTheme.colors.onSurfaceVariant),
                    maxLines = 1,
                )
            }
        }
        if (showHistory) {
            item.history.forEachIndexed { i, done ->
                Box(
                    modifier = GlanceModifier
                        .width(5.dp)
                        .height(14.dp)
                        .cornerRadius(2.dp)
                        .background(
                            if (done) ColorProvider(habitColor)
                            else ColorProvider(habitColor.copy(alpha = if (i == 6) 0.3f else 0.14f))
                        ),
                ) {}
                Spacer(GlanceModifier.width(3.dp))
            }
            Spacer(GlanceModifier.width(4.dp))
        }
        WidgetCheckButton(item, habitColor)
    }
}

@Composable
private fun WidgetCheckButton(item: WidgetHabitItem, habitColor: Color) {
    val habit = item.habit
    val canIncrement = !habit.isBadHabit && !item.isSkipped && item.count < habit.targetCount
    val filled = item.isDone && !habit.isBadHabit && item.count >= habit.targetCount
    // Glance не рисует обводку, поэтому кольцо — это круг цвета привычки с вложенным кругом фона строки
    Box(
        modifier = GlanceModifier
            .size(32.dp)
            .cornerRadius(16.dp)
            .background(
                when {
                    item.isSkipped || habit.isBadHabit -> GlanceTheme.colors.surfaceVariant
                    else -> ColorProvider(habitColor)
                }
            )
            .then(
                if (canIncrement) GlanceModifier.clickable(
                    actionRunCallback<IncrementHabitAction>(actionParametersOf(IncrementHabitAction.habitIdKey to habit.id))
                ) else GlanceModifier
            ),
        contentAlignment = Alignment.Center,
    ) {
        when {
            item.isSkipped -> Text("—", style = TextStyle(fontSize = 14.sp, color = GlanceTheme.colors.onSurfaceVariant))
            habit.isBadHabit -> Text(
                text = if (item.isDone) "✓" else "✕",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isDone) GlanceTheme.colors.tertiary else GlanceTheme.colors.error,
                ),
            )
            filled -> Text("✓", style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White)))
            else -> Box(
                modifier = GlanceModifier
                    .size(26.dp)
                    .cornerRadius(13.dp)
                    .background(GlanceTheme.colors.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                val label = when {
                    habit.targetCount > 1 -> "${item.count}"
                    item.isDone -> "✓"
                    else -> ""
                }
                Text(
                    text = label,
                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorProvider(habitColor)),
                )
            }
        }
    }
}
