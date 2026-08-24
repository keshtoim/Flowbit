package com.flowbit.app.presentation.habits.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

// ── Гистограмма активности за 30 дней ─────────────────────────────────────────

@Composable
internal fun ProgressBarChart(
    completedDates: List<LocalDate>,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()
    val days = (29 downTo 0).map { today.minusDays(it.toLong()) }
    val completedSet = completedDates.toHashSet()
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = modifier) {
        val barCount = days.size
        val gap = 2.dp.toPx()
        val barWidth = (size.width - gap * (barCount - 1)) / barCount
        val maxH = size.height

        days.forEachIndexed { i, date ->
            val x = i * (barWidth + gap)
            val isCompleted = date in completedSet
            // Невыполненные дни — 15% высоты, чтобы «пустые» столбики всё равно были видны
            val barH = if (isCompleted) maxH else maxH * 0.15f
            drawRoundRect(
                color = if (isCompleted) primaryColor else surfaceVariant,
                topLeft = Offset(x, maxH - barH),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(3.dp.toPx()),
            )
        }
    }
}

// ── Тепловая карта GitHub-style за последние 52 недели ────────────────────────

@Composable
internal fun YearHeatmapCard(completedDates: List<LocalDate>) {
    if (completedDates.isEmpty()) return
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    val today = LocalDate.now()
    val cols = 53
    val rows = 7
    val cellSize = 13.dp
    val gap = 3.dp
    val colWidth = cellSize + gap
    val totalWidth = colWidth * cols - gap   // ширина всей сетки

    val startDate = remember(today) {
        today.minusWeeks(52).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }
    val completedSet = remember(completedDates) { completedDates.toHashSet() }

    // Метки месяцев: col → аббревиатура месяца, только при смене
    val monthLabels = remember(startDate) {
        buildList {
            var lastMonth = -1
            for (col in 0 until cols) {
                val weekStart = startDate.plusDays((col * 7).toLong())
                if (weekStart.monthValue != lastMonth) {
                    lastMonth = weekStart.monthValue
                    add(col to weekStart.month.getDisplayName(TextStyle.SHORT, Locale("ru"))
                        .replaceFirstChar { it.uppercase() })
                } else {
                    add(col to null)
                }
            }
        }
    }

    val scrollState = rememberScrollState()
    LaunchedEffect(Unit) { scrollState.scrollTo(scrollState.maxValue) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Год активности",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(12.dp))

            Box(modifier = Modifier.horizontalScroll(scrollState)) {
                Column {
                    // Метки месяцев
                    Row(modifier = Modifier.width(totalWidth)) {
                        monthLabels.forEach { (_, label) ->
                            Box(modifier = Modifier.width(colWidth)) {
                                if (label != null) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))

                    // Сетка ячеек
                    Canvas(
                        modifier = Modifier
                            .width(totalWidth)
                            .height(cellSize * rows + gap * (rows - 1)),
                    ) {
                        val cellPx = cellSize.toPx()
                        val gapPx = gap.toPx()
                        val radius = cellPx * 0.3f
                        for (col in 0 until cols) {
                            for (row in 0 until rows) {
                                val date = startDate.plusDays((col * 7 + row).toLong())
                                if (date.isAfter(today)) continue
                                val isDone = date in completedSet
                                val alpha = if (isDone) 1f else 0.18f
                                drawRoundRect(
                                    color = if (isDone) primaryColor
                                            else surfaceVariantColor,
                                    topLeft = Offset(
                                        col * (cellPx + gapPx),
                                        row * (cellPx + gapPx),
                                    ),
                                    size = Size(cellPx, cellPx),
                                    cornerRadius = CornerRadius(radius),
                                    alpha = alpha,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Паттерн по дням недели ────────────────────────────────────────────────────
// Показывает относительную активность за каждый из 7 дней; лучший день — выделен

@Composable
internal fun WeeklyPatternCard(completedDates: List<LocalDate>) {
    if (completedDates.isEmpty()) return
    val dayLabels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val counts = (1..7).map { dow ->
        completedDates.count { it.dayOfWeek == DayOfWeek.of(dow) }
    }
    val maxCount = counts.maxOrNull()?.takeIf { it > 0 } ?: return
    val bestIndex = counts.indexOf(maxCount)

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Паттерн по дням",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Лучший: ${dayLabels[bestIndex]}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(16.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
            ) {
                val gap = 8.dp.toPx()
                val barWidth = (size.width - gap * 6) / 7f
                counts.forEachIndexed { i, count ->
                    val x = i * (barWidth + gap)
                    val barH = (count.toFloat() / maxCount) * size.height
                    // Серый фон-заглушка по всей высоте
                    drawRoundRect(
                        color = surfaceVariantColor,
                        topLeft = Offset(x, 0f),
                        size = Size(barWidth, size.height),
                        cornerRadius = CornerRadius(6.dp.toPx()),
                    )
                    if (barH > 0f) {
                        // Лучший день — полная непрозрачность, остальные — 45%
                        drawRoundRect(
                            color = if (i == bestIndex) primaryColor else primaryColor.copy(alpha = 0.45f),
                            topLeft = Offset(x, size.height - barH),
                            size = Size(barWidth, barH),
                            cornerRadius = CornerRadius(6.dp.toPx()),
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                dayLabels.forEachIndexed { i, label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (i == bestIndex) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (i == bestIndex) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}
