package com.flowbit.app.presentation.habits.list

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flowbit.app.R
import com.flowbit.app.domain.usecase.habit.HabitForDate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TodaySummaryCard(
    habits: List<HabitForDate>,
    selectedDate: LocalDate,
    modifier: Modifier = Modifier,
    weekDelta: Int? = null,
) {
    // Табу-привычки не включаются в счётчик дня — у них отдельная секция
    val normalHabits = habits.filter { !it.habit.isBadHabit }
    val activeHabits = normalHabits.filter { !it.isSkippedForDay }
    val total = activeHabits.size
    val done = activeHabits.count { it.isDone }
    val allSkipped = total == 0 && normalHabits.isNotEmpty()
    val progress = if (total > 0) done.toFloat() / total else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600),
        label = "progress",
    )

    val isToday = selectedDate == LocalDate.now()
    val locale = LocalConfiguration.current.locales[0]
    val dateLabel = if (isToday) {
        stringResource(R.string.summary_today)
    } else {
        selectedDate.format(DateTimeFormatter.ofPattern("d MMMM", locale))
    }

    val emoji = when {
        allSkipped -> "⏭"
        total == 0 -> "🌱"
        done == total -> "🎉"
        progress >= 0.5f -> "💪"
        else -> "🔥"
    }

    val message = when {
        selectedDate.isAfter(LocalDate.now()) && total > 0 -> stringResource(R.string.summary_future)
        allSkipped -> stringResource(R.string.summary_all_skipped)
        total == 0 -> stringResource(R.string.no_habits_title)
        done == total -> stringResource(R.string.summary_all_done)
        done == 0 -> stringResource(R.string.summary_start)
        else -> stringResource(R.string.summary_left, total - done, total)
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val greenColor = MaterialTheme.colorScheme.tertiary
    val redColor = MaterialTheme.colorScheme.error

    // Градиентный фон: серый → зелёный по мере выполнения
    val cardContainerColor by animateColorAsState(
        targetValue = when {
            total == 0 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
            done == total -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
            progress >= 0.5f -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
            else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        },
        animationSpec = tween(600),
        label = "cardBg",
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp)) {
                Canvas(modifier = Modifier.size(72.dp)) {
                    val stroke = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = stroke,
                        topLeft = Offset(3.dp.toPx(), 3.dp.toPx()),
                        size = Size(size.width - 6.dp.toPx(), size.height - 6.dp.toPx()),
                    )
                    drawArc(
                        color = primaryColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = stroke,
                        topLeft = Offset(3.dp.toPx(), 3.dp.toPx()),
                        size = Size(size.width - 6.dp.toPx(), size.height - 6.dp.toPx()),
                    )
                }
                Text(text = emoji, style = MaterialTheme.typography.titleLarge)
            }

            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.summary_done_count, done, total),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (weekDelta != null && isToday) {
                    val sign = if (weekDelta >= 0) "+" else ""
                    Text(
                        text = stringResource(R.string.summary_week_delta, "$sign$weekDelta"),
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            weekDelta > 0 -> greenColor
                            weekDelta < 0 -> redColor
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
fun DaySummaryBar(
    habits: List<HabitForDate>,
    selectedDate: LocalDate,
    modifier: Modifier = Modifier,
    weekDelta: Int? = null,
) {
    val active = habits.filter { !it.habit.isBadHabit && !it.isSkippedForDay }
    val total = active.size
    val done = active.count { it.isDone }
    val progress by animateFloatAsState(
        targetValue = if (total > 0) done.toFloat() / total else 0f,
        animationSpec = tween(600),
        label = "barProgress",
    )
    val primary = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceVariant

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(6.dp),
        ) {
            val radius = CornerRadius(size.height / 2)
            drawRoundRect(color = track, cornerRadius = radius)
            if (progress > 0f) {
                drawRoundRect(color = primary, size = Size(size.width * progress, size.height), cornerRadius = radius)
            }
        }
        Text(
            text = "$done / $total",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
        if (weekDelta != null && selectedDate == LocalDate.now()) {
            Text(
                text = (if (weekDelta >= 0) "+" else "") + weekDelta,
                style = MaterialTheme.typography.labelMedium,
                color = when {
                    weekDelta > 0 -> MaterialTheme.colorScheme.tertiary
                    weekDelta < 0 -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}
