package com.flowbit.app.presentation.habits.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowbit.app.R
import com.flowbit.app.domain.model.HabitFrequency
import com.flowbit.app.domain.usecase.habit.HabitForDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitRow(
    habitForDate: HabitForDate,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onUnSkipRequest: () -> Unit,
    onTimer: () -> Unit,
    modifier: Modifier = Modifier,
    mini: Boolean = false,
    showHistory: Boolean = true,
) {
    val haptic = LocalHapticFeedback.current
    val toggle = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onToggle()
    }

    val habit = habitForDate.habit
    val entry = habitForDate.entry
    val isStreakSafeSkipped = entry?.isStreakSafeSkip == true
    val isSkipped = entry?.isSkipped == true
    val skippedAny = isSkipped || isStreakSafeSkipped
    val completedCount = if (skippedAny) 0 else (entry?.completedCount ?: 0)
    val isRelapsed = habit.isBadHabit && completedCount > 0
    val isCompleted = !habit.isBadHabit && !skippedAny && completedCount >= habit.targetCount
    val isInterval = habit.frequency == HabitFrequency.INTERVAL

    val habitColor = remember(habit.effectiveColorHex) {
        runCatching { Color(android.graphics.Color.parseColor(habit.effectiveColorHex)) }
            .getOrDefault(Color(0xFF2DD4BF))
    }
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val rowColor by animateColorAsState(
        targetValue = when {
            isRelapsed -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
            isStreakSafeSkipped -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)
            isCompleted -> habitColor.copy(alpha = 0.10f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        },
        animationSpec = tween(300),
        label = "rowColor",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(if (mini) 48.dp else 60.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(rowColor)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(start = 10.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(if (mini) 30.dp else 38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(habitColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = habit.emoji, fontSize = if (mini) 15.sp else 19.sp)
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isCompleted || isSkipped) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.onSurface,
            )
            if (!mini) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (habitForDate.streak > 0 && !skippedAny && !habit.isBadHabit) {
                        Text(
                            text = "🔥${habitForDate.streak}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFB923C),
                        )
                    }
                    val (meta, metaColor) = when {
                        habit.isBadHabit && isRelapsed -> stringResource(R.string.taboo_relapsed_status) to MaterialTheme.colorScheme.error
                        habit.isBadHabit -> stringResource(R.string.taboo_clean_status) to MaterialTheme.colorScheme.tertiary
                        isStreakSafeSkipped -> stringResource(R.string.streak_safe_skipped_label) to MaterialTheme.colorScheme.tertiary
                        isSkipped -> stringResource(R.string.status_skipped) to muted
                        isInterval && habit.targetCount == 1 -> stringResource(
                            R.string.interval_progress,
                            habitForDate.intervalProgress.coerceAtMost(habit.intervalTimes),
                            habit.intervalTimes,
                            habit.intervalDays,
                        ) to if (habitForDate.isDone) habitColor else muted
                        habit.targetCount > 1 -> buildString {
                            append("$completedCount / ${habit.targetCount}")
                            if (!habit.unit.isNullOrEmpty()) append(" ${habit.unit}")
                        } to if (isCompleted) habitColor else muted
                        habit.timerSeconds > 0 && !isCompleted ->
                            "%d:%02d".format(habit.timerSeconds / 60, habit.timerSeconds % 60) to muted
                        else -> "" to muted
                    }
                    if (meta.isNotEmpty()) {
                        Text(
                            text = meta,
                            style = MaterialTheme.typography.labelMedium,
                            color = metaColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                    }
                    if (habit.showInWidget) {
                        Icon(
                            imageVector = Icons.Outlined.Widgets,
                            contentDescription = stringResource(R.string.cd_in_widget),
                            modifier = Modifier.size(12.dp),
                            tint = muted.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }

        if (!mini && showHistory && habitForDate.recentDays.size == 7) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                val doneColor = if (habit.isBadHabit) MaterialTheme.colorScheme.error else habitColor
                habitForDate.recentDays.forEachIndexed { i, done ->
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                when {
                                    done -> doneColor
                                    i == 6 -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)
                                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                }
                            ),
                    )
                }
            }
        }

        RowAction(
            habitForDate = habitForDate,
            habitColor = habitColor,
            completedCount = completedCount,
            isCompleted = isCompleted,
            isRelapsed = isRelapsed,
            skipped = skippedAny,
            onToggle = toggle,
            onUnSkipRequest = onUnSkipRequest,
            onTimer = onTimer,
        )
    }
}

@Composable
private fun RowAction(
    habitForDate: HabitForDate,
    habitColor: Color,
    completedCount: Int,
    isCompleted: Boolean,
    isRelapsed: Boolean,
    skipped: Boolean,
    onToggle: () -> Unit,
    onUnSkipRequest: () -> Unit,
    onTimer: () -> Unit,
) {
    val habit = habitForDate.habit
    when {
        skipped -> IconButton(onClick = onUnSkipRequest) {
            Icon(
                Icons.AutoMirrored.Filled.Undo,
                contentDescription = stringResource(R.string.cancel_skip),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        habit.isBadHabit && !isRelapsed -> TextButton(
            onClick = onToggle,
            contentPadding = PaddingValues(horizontal = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.taboo_relapse_button),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
            )
        }
        habit.timerSeconds > 0 && completedCount == 0 -> IconButton(onClick = onTimer) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .border(2.dp, habitColor, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = stringResource(R.string.cd_start_timer),
                    tint = habitColor,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        else -> {
            val filled = isCompleted || isRelapsed
            val fillColor = if (isRelapsed) MaterialTheme.colorScheme.error else habitColor
            val trackColor = habitColor.copy(alpha = 0.25f)
            IconButton(onClick = onToggle) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .then(if (filled) Modifier.background(fillColor) else Modifier),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!filled) {
                        val progress = if (habit.targetCount > 1) completedCount.toFloat() / habit.targetCount else 0f
                        Canvas(modifier = Modifier.size(38.dp)) {
                            val stroke = 2.dp.toPx()
                            val inset = stroke / 2
                            val arcSize = Size(size.width - stroke, size.height - stroke)
                            drawArc(trackColor, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
                            if (progress > 0f) {
                                drawArc(
                                    habitColor, -90f, 360f * progress, false, Offset(inset, inset), arcSize,
                                    style = Stroke(stroke, cap = StrokeCap.Round),
                                )
                            }
                        }
                    }
                    when {
                        isRelapsed -> Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        filled -> Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        habit.targetCount > 1 -> Text(
                            text = "$completedCount",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = habitColor,
                        )
                        // Норма интервала набрана заранее — отметка сегодня не обязательна
                        habitForDate.isCoveredByInterval -> Icon(
                            Icons.Default.Check, null,
                            tint = habitColor.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}
