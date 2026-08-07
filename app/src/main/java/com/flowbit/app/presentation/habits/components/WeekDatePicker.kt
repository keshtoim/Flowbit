package com.flowbit.app.presentation.habits.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeekDatePicker(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()

    fun mondayOf(date: LocalDate): LocalDate {
        val dow = date.dayOfWeek.value  // Mon=1 … Sun=7
        return date.minusDays((dow - 1).toLong())
    }

    // Фиксированная Пн–Вс неделя (не центрируется под выбранную дату)
    var weekStart by remember { mutableStateOf(mondayOf(selectedDate)) }

    // Если selectedDate ушла за пределы показанной недели — смещаем окно
    LaunchedEffect(selectedDate) {
        if (selectedDate < weekStart || selectedDate >= weekStart.plusDays(7)) {
            weekStart = mondayOf(selectedDate)
        }
    }

    val days = (0..6).map { weekStart.plusDays(it.toLong()) }
    val selectedIndex = days.indexOf(selectedDate)  // -1 если вне текущей недели

    // Последняя известная позиция пилюли — чтобы при уходе на другую неделю она не прыгала
    var lastValidIndex by remember { mutableIntStateOf(selectedIndex.coerceAtLeast(0)) }
    LaunchedEffect(selectedIndex) {
        if (selectedIndex >= 0) lastValidIndex = selectedIndex
    }

    val primary = MaterialTheme.colorScheme.primary
    val density = LocalDensity.current

    // Альфа пилюли: плавно скрывается, когда selected-дата уходит за пределы недели
    val pillAlphaState = animateFloatAsState(
        targetValue = if (selectedIndex >= 0) 1f else 0f,
        animationSpec = tween(200),
        label = "weekPillAlpha",
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp)) {
            // ── Заголовок с месяцем и навигацией ────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { weekStart = weekStart.minusDays(7) },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Предыдущая неделя",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
                val monthLabel = run {
                    val startMonth = weekStart.month
                    val endMonth = weekStart.plusDays(6).month
                    val name = startMonth.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
                        .replaceFirstChar { it.uppercase() }
                    val yearSuffix = if (weekStart.year != today.year) " ${weekStart.year}" else ""
                    if (startMonth != endMonth) {
                        val name2 = endMonth.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
                            .replaceFirstChar { it.uppercase() }
                        "$name / $name2$yearSuffix"
                    } else {
                        "$name$yearSuffix"
                    }
                }
                Text(
                    text = monthLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
                IconButton(
                    onClick = { weekStart = weekStart.plusDays(7) },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Следующая неделя",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // ── Дни недели со скользящей пилюлей ────────────────────────────
            BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                val itemWidth = maxWidth / 7

                // Реальная анимация позиции пилюли живёт здесь, внутри BoxWithConstraints
                val pillOffset by animateDpAsState(
                    targetValue = itemWidth * lastValidIndex.toFloat(),
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                    label = "weekPillOffset",
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            val px = with(density) { pillOffset.toPx() }
                            val wPx = with(density) { itemWidth.toPx() }
                            val cornerPx = with(density) { 12.dp.toPx() }
                            drawRoundRect(
                                color = primary.copy(alpha = pillAlphaState.value),
                                topLeft = Offset(px, 0f),
                                size = Size(wPx, size.height),
                                cornerRadius = CornerRadius(cornerPx),
                            )
                        },
                ) {
                    days.forEach { date ->
                        val isSelected = date == selectedDate
                        val isToday = date == today

                        val textColor by animateColorAsState(
                            targetValue = when {
                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                isToday -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            animationSpec = tween(200),
                            label = "dayText_${date.dayOfWeek}",
                        )
                        val labelColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                         else MaterialTheme.colorScheme.onSurfaceVariant,
                            animationSpec = tween(200),
                            label = "dayLabel_${date.dayOfWeek}",
                        )
                        val dotColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                         else MaterialTheme.colorScheme.primary,
                            animationSpec = tween(200),
                            label = "dayDot_${date.dayOfWeek}",
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onDateSelected(date) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("ru")),
                                style = MaterialTheme.typography.labelMedium,
                                color = labelColor,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = date.dayOfMonth.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = textColor,
                                textAlign = TextAlign.Center,
                            )
                            if (isToday) {
                                Spacer(Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(dotColor),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
