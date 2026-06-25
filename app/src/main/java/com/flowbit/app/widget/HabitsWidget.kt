package com.flowbit.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
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
import androidx.glance.layout.wrapContentHeight
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

    // SizeMode.Exact — provideContent получает реальные размеры виджета
    override val sizeMode = SizeMode.Exact

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

        val entryMap: Map<Long, Map<LocalDate, HabitEntryEntity>> = allEntries
            .groupBy { it.habitId }
            .mapValues { (_, entries) -> entries.associateBy { LocalDate.parse(it.date) } }

        provideContent {
            // LocalSize.current — реальный размер виджета в данный момент
            val widgetSize = LocalSize.current
            GlanceTheme(colors = ColorProviders(light = LightColorScheme, dark = DarkColorScheme)) {
                WidgetContent(
                    context = context,
                    habits = habits,
                    weekDates = weekDates,
                    today = today,
                    entryMap = entryMap,
                    widgetSize = widgetSize,
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
    widgetSize: DpSize,
) {
    val openIntent = Intent(context, MainActivity::class.java)
    val dayNames = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val padding = 10.dp

    // --- Адаптивные размеры ---
    val availableWidth = widgetSize.width - padding * 2
    // Левая колонка: emoji (circle) + gap + имя
    val emojiCircle: Dp = (availableWidth * 0.10f).coerceIn(22.dp, 32.dp)
    val nameWidth: Dp = (availableWidth * 0.25f).coerceIn(48.dp, 100.dp)
    val leftWidth = emojiCircle + 4.dp + nameWidth
    // Оставшееся место — равномерно на 7 кружков
    val daysWidth = availableWidth - leftWidth
    val circleSlot: Dp = daysWidth / 7
    val circleSize: Dp = (circleSlot * 0.78f).coerceIn(14.dp, 30.dp)

    // Вертикальное распределение: равные строки под количество привычек
    val availableHeight = widgetSize.height - padding * 2
    val headerH: Dp = (availableHeight * 0.18f).coerceIn(16.dp, 24.dp)
    val maxRows = habits.size.coerceAtLeast(1)
    val rowH: Dp = ((availableHeight - headerH) / maxRows).coerceIn(28.dp, 56.dp)

    val nameFontSize = if (nameWidth < 60.dp) 9.sp else if (nameWidth < 80.dp) 10.sp else 11.sp
    val headerFontSize = if (circleSlot < 24.dp) 7.sp else 9.sp

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .padding(padding)
            .clickable(actionStartActivity(openIntent)),
    ) {
        // Заголовок: дни недели
        Row(
            modifier = GlanceModifier.fillMaxWidth().height(headerH),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(GlanceModifier.width(leftWidth))
            dayNames.forEachIndexed { i, name ->
                val isToday = weekDates[i] == today
                Box(
                    modifier = GlanceModifier.defaultWeight().fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = name,
                        style = TextStyle(
                            fontSize = headerFontSize,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isToday) GlanceTheme.colors.primary
                                    else GlanceTheme.colors.onSurfaceVariant,
                        ),
                    )
                }
            }
        }

        if (habits.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Нет привычек для виджета",
                    style = TextStyle(fontSize = 11.sp, color = GlanceTheme.colors.onSurfaceVariant),
                )
            }
        } else {
            habits.forEach { habit ->
                val habitEntries = entryMap[habit.id] ?: emptyMap()
                val habitColor = parseHabitColor(habit.color.hex)

                Row(
                    modifier = GlanceModifier.fillMaxWidth().height(rowH),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Emoji-кружок
                    Box(
                        modifier = GlanceModifier
                            .size(emojiCircle)
                            .cornerRadius((emojiCircle.value / 2).dp)
                            .background(ColorProvider(habitColor.copy(alpha = 0.22f))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = habit.emoji,
                            style = TextStyle(fontSize = (emojiCircle.value * 0.5f).sp),
                        )
                    }

                    Spacer(GlanceModifier.width(4.dp))

                    // Название
                    Box(
                        modifier = GlanceModifier.width(nameWidth).height(rowH),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        val maxChars = (nameWidth.value / 7f).toInt().coerceAtLeast(3)
                        Text(
                            text = if (habit.name.length > maxChars) habit.name.take(maxChars - 1) + "…"
                                   else habit.name,
                            style = TextStyle(
                                fontSize = nameFontSize,
                                color = GlanceTheme.colors.onSurface,
                            ),
                            maxLines = 1,
                        )
                    }

                    // 7 кружков дней
                    weekDates.forEach { date ->
                        val entry = habitEntries[date]
                        val completedCount = entry?.completedCount ?: 0
                        val isCompleted = completedCount >= habit.targetCount
                        val isToday = date == today
                        val isFuture = date.isAfter(today)

                        Box(
                            modifier = GlanceModifier.defaultWeight().height(rowH),
                            contentAlignment = Alignment.Center,
                        ) {
                            DayCircle(
                                isCompleted = isCompleted,
                                isToday = isToday,
                                isFuture = isFuture,
                                habitColor = habitColor,
                                count = completedCount,
                                target = habit.targetCount,
                                size = circleSize,
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
    size: Dp,
) {
    val bgColor = when {
        isCompleted -> ColorProvider(habitColor)
        isToday     -> ColorProvider(habitColor.copy(alpha = 0.40f))
        isFuture    -> ColorProvider(Color(0x08000000))
        else        -> ColorProvider(habitColor.copy(alpha = 0.20f))
    }
    val radius = (size.value / 2).dp

    Box(
        modifier = GlanceModifier
            .size(size)
            .cornerRadius(radius)
            .background(bgColor),
        contentAlignment = Alignment.Center,
    ) {
        if (target > 1 && !isCompleted && !isFuture) {
            Text(
                text = "$count",
                style = TextStyle(
                    fontSize = (size.value * 0.38f).sp,
                    color = ColorProvider(habitColor),
                ),
            )
        }
    }
}

private fun parseHabitColor(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (e: Exception) {
    Color(0xFF00E5C0)
}
