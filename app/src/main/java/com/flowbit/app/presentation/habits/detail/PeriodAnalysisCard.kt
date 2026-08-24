package com.flowbit.app.presentation.habits.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// ── Карточка «Анализ периода» ─────────────────────────────────────────────────
// Переключатель 3 дня / Неделя / Месяц + connected-dots график

@Composable
internal fun PeriodAnalysisCard(completedDates: List<LocalDate>) {
    val periods = listOf("3 дня", "Неделя", "Месяц")
    var selectedPeriod by rememberSaveable { mutableIntStateOf(1) }
    val today = LocalDate.now()
    val completedSet = completedDates.toHashSet()

    val days = remember(selectedPeriod, today) {
        when (selectedPeriod) {
            0 -> (2 downTo 0).map { today.minusDays(it.toLong()) }
            1 -> (6 downTo 0).map { today.minusDays(it.toLong()) }
            else -> (29 downTo 0).map { today.minusDays(it.toLong()) }
        }
    }
    val doneDays = days.count { it in completedSet }
    val pct = if (days.isEmpty()) 0 else (doneDays * 100 / days.size)

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
                    text = "Анализ периода",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "$doneDays / ${days.size} дн. • $pct%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(10.dp))

            // Переключатель периодов — pill-кнопки
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                periods.forEachIndexed { idx, label ->
                    val selected = idx == selectedPeriod
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                            )
                            .clickable { selectedPeriod = idx }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            ConnectedDotsChart(days = days, completedSet = completedSet)
        }
    }
}

// ── Connected-dots график ──────────────────────────────────────────────────────
// Линии + кружки: заполненный = выполнено, пустой = пропущено, обводка = сегодня

@Composable
internal fun ConnectedDotsChart(
    days: List<LocalDate>,
    completedSet: Set<LocalDate>,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outline
    val today = LocalDate.now()
    val fmt = remember { DateTimeFormatter.ofPattern("d MMM", Locale("ru")) }

    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
        ) {
            if (days.isEmpty()) return@Canvas
            val step = size.width / days.size.coerceAtLeast(1)
            // Радиус не больше половины шага минус зазор, и не меньше 3dp
            val dotR = (step / 2f - 2.dp.toPx()).coerceIn(3.dp.toPx(), 10.dp.toPx())
            val cy = size.height / 2f

            // Соединительные линии — рисуем первыми, чтобы кружки перекрывали их
            for (i in 0 until days.size - 1) {
                val x1 = step * i + step / 2f
                val x2 = step * (i + 1) + step / 2f
                drawLine(
                    color = outlineColor.copy(alpha = 0.4f),
                    start = Offset(x1, cy),
                    end = Offset(x2, cy),
                    strokeWidth = 2.dp.toPx(),
                )
            }

            // Кружки
            days.forEachIndexed { i, date ->
                val cx = step * i + step / 2f
                val done = date in completedSet
                val isToday = date == today
                if (done) {
                    drawCircle(color = primaryColor, radius = dotR, center = Offset(cx, cy))
                } else {
                    drawCircle(color = surfaceVariant, radius = dotR, center = Offset(cx, cy))
                    drawCircle(
                        color = outlineColor.copy(alpha = 0.6f),
                        radius = dotR,
                        center = Offset(cx, cy),
                        style = Stroke(width = 1.5.dp.toPx()),
                    )
                }
                // Дополнительная обводка для текущего дня
                if (isToday) {
                    drawCircle(
                        color = primaryColor,
                        radius = dotR + 3.dp.toPx(),
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.dp.toPx()),
                    )
                }
            }
        }

        // Подписи дат: для месяца показываем каждую 5-ю
        val showEvery = when {
            days.size <= 7 -> 1
            else -> 5
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            days.forEachIndexed { i, date ->
                if (i % showEvery == 0 || i == days.size - 1) {
                    Text(
                        text = date.format(fmt),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (date == today) primaryColor
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(showEvery.toFloat()),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                } else if (i % showEvery != 0) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
