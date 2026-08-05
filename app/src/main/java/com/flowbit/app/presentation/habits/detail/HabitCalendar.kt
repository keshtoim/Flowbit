package com.flowbit.app.presentation.habits.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

// ── Интерактивный календарь привычки ─────────────────────────────────────────
// Навигация по месяцам, статистика «X из Y дн.», подсветка выполненных дней

@Composable
internal fun HabitCalendar(
    completedDates: List<LocalDate>,
    modifier: Modifier = Modifier,
) {
    var displayMonth by remember { mutableStateOf(YearMonth.now()) }
    val completedSet = remember(completedDates) { completedDates.toHashSet() }
    val today = LocalDate.now()

    val monthFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru"))
    val dayNames = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

    // Считаем прогресс только по прошедшим дням текущего отображаемого месяца
    val monthStats = remember(displayMonth, completedSet) {
        val daysInMonth = displayMonth.lengthOfMonth()
        val lastDay = minOf(
            daysInMonth,
            if (displayMonth == YearMonth.now()) today.dayOfMonth else daysInMonth,
        )
        val doneDays = (1..lastDay).count { displayMonth.atDay(it) in completedSet }
        Triple(doneDays, lastDay, if (lastDay > 0) doneDays * 100 / lastDay else 0)
    }
    val (doneDays, passedDays, pct) = monthStats

    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    Column(modifier = modifier) {
        // ── Навигация по месяцу ───────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { displayMonth = displayMonth.minusMonths(1) },
                modifier = Modifier.size(36.dp),
            ) {
                Icon(Icons.Default.ChevronLeft, "Предыдущий месяц", modifier = Modifier.size(20.dp))
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = displayMonth.format(monthFormatter).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "$doneDays из $passedDays дн. • $pct%",
                    style = MaterialTheme.typography.labelSmall,
                    color = primaryColor,
                )
            }
            IconButton(
                onClick = { displayMonth = displayMonth.plusMonths(1) },
                modifier = Modifier.size(36.dp),
            ) {
                Icon(Icons.Default.ChevronRight, "Следующий месяц", modifier = Modifier.size(20.dp))
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Заголовки дней недели ─────────────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth()) {
            dayNames.forEachIndexed { i, name ->
                // Сб/Вс отображаются приглушённым красным
                val isWeekend = i >= 5
                Text(
                    text = name,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isWeekend)
                        MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        // ── Сетка дней ───────────────────────────────────────────────────────
        val firstDay = displayMonth.atDay(1)
        val offset = firstDay.dayOfWeek.value - 1   // Пн = 0 … Вс = 6
        val daysInMonth = displayMonth.lengthOfMonth()
        val weeks = (offset + daysInMonth + 6) / 7

        repeat(weeks) { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val dayNum = week * 7 + col - offset + 1
                    if (dayNum < 1 || dayNum > daysInMonth) {
                        Box(modifier = Modifier.weight(1f).height(44.dp))
                    } else {
                        val date = displayMonth.atDay(dayNum)
                        val isToday = date == today
                        val isFuture = date.isAfter(today)
                        val isCompleted = date in completedSet
                        val isWeekend = col >= 5

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .then(
                                        when {
                                            isCompleted -> Modifier.background(primaryColor)
                                            isToday -> Modifier.border(2.dp, primaryColor, CircleShape)
                                            else -> Modifier
                                        },
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = dayNum.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = when {
                                        isCompleted || isToday -> FontWeight.Bold
                                        else -> FontWeight.Normal
                                    },
                                    color = when {
                                        isCompleted -> onPrimaryColor
                                        isFuture -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                        isToday -> primaryColor
                                        isWeekend -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
