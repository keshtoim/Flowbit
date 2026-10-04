package com.flowbit.app.presentation.habits.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

// ── Тепловая карта: 52 недели × 7 дней (GitHub-style) ────────────────────────

@Composable
internal fun HabitHeatmap(
    completedDates: List<LocalDate>,
    modifier: Modifier = Modifier,
) {
    val completedSet = remember(completedDates) { completedDates.toHashSet() }
    val today = LocalDate.now()

    // Начинаем с понедельника, ровно 52 недели назад
    val endDate = today
    val startDate = run {
        val weekStart = today.minusWeeks(51)
        // Сдвигаем на понедельник
        weekStart.minusDays((weekStart.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong()
            .let { if (it < 0) it + 7 else it })
    }

    // Метки месяцев: одна на каждый новый месяц в колонке недель
    val weeks = mutableListOf<LocalDate>() // понедельник каждой недели
    var w = startDate
    while (!w.isAfter(endDate)) {
        weeks.add(w)
        w = w.plusWeeks(1)
    }

    val primary = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    val cellSize = 11.dp
    val gap = 2.dp

    // Подписи месяцев
    val monthLabels = remember(weeks) {
        weeks.mapIndexed { i, monday ->
            val monthOfMonday = monday.month
            val prevMonday = if (i > 0) weeks[i - 1] else null
            if (prevMonday == null || prevMonday.month != monthOfMonday) {
                i to monthOfMonday.getDisplayName(TextStyle.SHORT, Locale("ru"))
                    .replaceFirstChar { it.uppercase() }
            } else {
                null
            }
        }.filterNotNull()
    }

    Column(modifier = modifier) {
        Text(
            text = "Активность за год",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
        Spacer(Modifier.height(6.dp))

        // Метки месяцев над сеткой
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap),
        ) {
            weeks.forEachIndexed { i, _ ->
                val label = monthLabels.firstOrNull { it.first == i }?.second
                Box(modifier = Modifier.size(cellSize)) {
                    if (label != null) {
                        Text(
                            text = label,
                            fontSize = 7.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(2.dp))

        // Сетка: 7 строк (дни недели), N колонок (недели)
        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
            for (dow in 0..6) {
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    weeks.forEach { monday ->
                        val date = monday.plusDays(dow.toLong())
                        val isFuture = date.isAfter(today)
                        val isCompleted = !isFuture && date in completedSet

                        val color = when {
                            isFuture -> surfaceVariant.copy(alpha = 0.3f)
                            isCompleted -> lerp(
                                primary.copy(alpha = 0.35f),
                                primary,
                                0.8f,
                            )
                            else -> surfaceVariant.copy(alpha = 0.6f)
                        }

                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .clip(RoundedCornerShape(2.dp))
                                .background(color),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        // Легенда
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                "Меньше",
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
            )
            listOf(0.1f, 0.3f, 0.55f, 0.8f, 1.0f).forEach { alpha ->
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(primary.copy(alpha = alpha)),
                )
            }
            Text(
                "Больше",
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
            )
        }
    }
}
