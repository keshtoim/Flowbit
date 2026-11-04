package com.flowbit.app.presentation.habits.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowbit.app.R
import com.flowbit.app.domain.model.Habit
import com.flowbit.app.domain.model.HabitFrequency
import com.flowbit.app.domain.usecase.habit.HabitForDate
import java.time.DayOfWeek
import java.time.format.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitActionsSheet(
    habitForDate: HabitForDate,
    onDismiss: () -> Unit,
    onToggle: () -> Unit,
    onDecrease: () -> Unit,
    onEnterCount: () -> Unit,
    onTimer: () -> Unit,
    onSkip: () -> Unit,
    onStreakSafeSkip: () -> Unit,
    onUnSkipRequest: () -> Unit,
    onCantToday: () -> Unit,
    onShowInWidgetChange: (Boolean) -> Unit,
    onOpenDetails: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
) {
    val habit = habitForDate.habit
    val entry = habitForDate.entry
    val skipped = habitForDate.isSkippedForDay
    val count = if (skipped) 0 else (entry?.completedCount ?: 0)
    val isRelapsed = habit.isBadHabit && count > 0
    val habitColor = remember(habit.effectiveColorHex) {
        runCatching { Color(android.graphics.Color.parseColor(habit.effectiveColorHex)) }
            .getOrDefault(Color(0xFF2DD4BF))
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val run: (() -> Unit) -> () -> Unit = { action -> { action(); onDismiss() } }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(habitColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(habit.emoji, fontSize = 22.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = frequencyLabel(habit, habitForDate.streak),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = run(onEdit)) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.cd_edit))
                }
            }

            if (!skipped) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (habit.isBadHabit) {
                        if (isRelapsed) {
                            QuickAction(Icons.AutoMirrored.Filled.Undo, stringResource(R.string.sheet_undo_relapse), habitColor, false, run(onDecrease))
                        } else {
                            QuickAction(Icons.Default.Close, stringResource(R.string.taboo_relapse_button), MaterialTheme.colorScheme.error, true, run(onToggle))
                        }
                    } else {
                        val done = count >= habit.targetCount
                        QuickAction(
                            icon = Icons.Default.Check,
                            label = stringResource(
                                when {
                                    habit.targetCount > 1 -> R.string.sheet_plus_one
                                    done -> R.string.sheet_unmark
                                    else -> R.string.sheet_mark
                                }
                            ),
                            color = habitColor,
                            primary = !done || habit.targetCount > 1,
                            onClick = run(onToggle),
                        )
                        if (habit.targetCount > 1) {
                            QuickAction(Icons.Default.Pin, stringResource(R.string.sheet_enter_count), habitColor, false, run(onEnterCount))
                        }
                        if (habit.targetCount > 1 && count > 0) {
                            QuickAction(Icons.Default.Remove, stringResource(R.string.sheet_minus_one), habitColor, false, run(onDecrease))
                        }
                        if (habit.timerSeconds > 0 && !done) {
                            QuickAction(Icons.Default.Timer, stringResource(R.string.sheet_timer), habitColor, false, run(onTimer))
                        }
                    }
                }
            }

            if (skipped || !habit.isBadHabit) SheetGroup {
                when {
                    skipped -> SheetItem(Icons.AutoMirrored.Filled.Undo, stringResource(R.string.cancel_skip), run(onUnSkipRequest))
                    else -> {
                        SheetItem(Icons.Outlined.SkipNext, stringResource(R.string.sheet_skip_today), run(onSkip))
                        if (habit.allowStreakSkip) {
                            SheetDivider()
                            SheetItem(Icons.Outlined.Shield, stringResource(R.string.sheet_streak_safe_skip), run(onStreakSafeSkip))
                        }
                        if (!habitForDate.isDone) {
                            SheetDivider()
                            SheetItem(Icons.Outlined.SentimentDissatisfied, stringResource(R.string.cant_today), run(onCantToday))
                        }
                    }
                }
            }

            SheetGroup {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clickable { onShowInWidgetChange(!habit.showInWidget) }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Icon(Icons.Outlined.Widgets, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.sheet_show_in_widget), modifier = Modifier.weight(1f))
                    Switch(checked = habit.showInWidget, onCheckedChange = onShowInWidgetChange)
                }
                SheetDivider()
                SheetItem(Icons.Outlined.BarChart, stringResource(R.string.sheet_open_details), run(onOpenDetails))
                SheetDivider()
                SheetItem(Icons.Outlined.Archive, stringResource(R.string.sheet_archive), run(onArchive))
            }
        }
    }
}

@Composable
private fun frequencyLabel(habit: Habit, streak: Int): String {
    val locale = LocalConfiguration.current.locales[0]
    val base = when (habit.frequency) {
        HabitFrequency.DAILY -> stringResource(R.string.freq_daily)
        HabitFrequency.INTERVAL -> stringResource(R.string.freq_interval, habit.intervalTimes, habit.intervalDays)
        HabitFrequency.WEEKLY, HabitFrequency.CUSTOM -> DayOfWeek.entries
            .filter { it in habit.scheduledDays }
            .joinToString(" · ") { it.getDisplayName(TextStyle.SHORT, locale).replaceFirstChar(Char::uppercase) }
    }
    return if (streak > 0 && !habit.isBadHabit) stringResource(R.string.freq_with_streak, base, streak) else base
}

@Composable
private fun RowScope.QuickAction(
    icon: ImageVector,
    label: String,
    color: Color,
    primary: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .height(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (primary) color else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val content = if (primary) Color(0xFF0F1115) else MaterialTheme.colorScheme.onSurface
        Icon(icon, null, tint = content, modifier = Modifier.size(20.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun SheetGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        content = content,
    )
}

@Composable
private fun SheetItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SheetDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
}
