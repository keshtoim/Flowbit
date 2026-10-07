package com.flowbit.app.presentation.habits.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.flowbit.app.R
import kotlinx.coroutines.launch
import com.flowbit.app.domain.usecase.habit.HabitForDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitCard(
    habitForDate: HabitForDate,
    onToggle: () -> Unit,
    onDecrease: () -> Unit,
    onGiveUp: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSkip: () -> Unit = {},
    onUnSkipRequest: () -> Unit = {},
    onTimer: () -> Unit = {},
    onLongClick: () -> Unit = {},
    compact: Boolean = false,
) {
    val haptic = LocalHapticFeedback.current
    val onToggleWithHaptic = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onToggle()
    }

    val habit = habitForDate.habit
    val isStreakSafeSkipped = habitForDate.entry?.isStreakSafeSkip ?: false
    val isSkipped = habitForDate.entry?.isSkipped ?: false
    val completedCount = if (isSkipped || isStreakSafeSkipped) 0 else (habitForDate.entry?.completedCount ?: 0)
    val isRelapsed = habit.isBadHabit && completedCount > 0
    val isCompleted = if (habit.isBadHabit) !isRelapsed
                      else !isSkipped && !isStreakSafeSkipped && completedCount >= habit.targetCount

    val streak = habitForDate.entry?.let { _ ->
        // streak shown if habit has a positive series — approximated from recentDays
        habitForDate.recentDays.takeLastWhile { it }.size
    } ?: 0

    val habitColor = remember(habit.effectiveColorHex) {
        try { Color(android.graphics.Color.parseColor(habit.effectiveColorHex)) }
        catch (e: Exception) { Color(0xFF00E5C0) }
    }
    val surface = MaterialTheme.colorScheme.surface

    // Bounce + частицы при выполнении
    val cardScale = remember { Animatable(1f) }
    val particleProgress = remember { Animatable(0f) }
    val prevCompleted = remember { mutableStateOf(isCompleted) }
    LaunchedEffect(isCompleted) {
        val was = prevCompleted.value
        prevCompleted.value = isCompleted
        if (isCompleted && !was) {
            launch {
                cardScale.animateTo(0.97f, tween(70))
                cardScale.animateTo(1.04f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow))
                cardScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy))
            }
            launch {
                particleProgress.snapTo(0f)
                particleProgress.animateTo(1f, tween(550))
            }
        }
    }

    val skippedColor = MaterialTheme.colorScheme.errorContainer
    val tabooCleanColor = MaterialTheme.colorScheme.tertiary
    val streakSafeColor = MaterialTheme.colorScheme.tertiaryContainer
    val cardColor by animateColorAsState(
        targetValue = when {
            isRelapsed -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.40f)
            habit.isBadHabit -> tabooCleanColor.copy(alpha = 0.14f)
            isStreakSafeSkipped -> streakSafeColor.copy(alpha = 0.35f)
            isSkipped -> skippedColor
            isCompleted -> habitColor.copy(alpha = 0.10f)
            else -> surface
        },
        animationSpec = tween(380, easing = FastOutSlowInEasing),
        label = "cardColor",
    )
    val buttonColor by animateColorAsState(
        targetValue = if (isCompleted) habitColor else habitColor.copy(alpha = 0.12f),
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "buttonColor",
    )
    val buttonScale by animateFloatAsState(
        targetValue = if (isCompleted) 1f else 0.95f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "buttonScale",
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(cardScale.value)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 0.dp else 2.dp),
    ) {
        Column {
            // Фото-баннер (скрывается в компактном режиме)
            if (!compact && habit.photoUri != null && !habit.isPhotoHidden) {
                AsyncImage(
                    model = habit.photoUri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
                    contentScale = ContentScale.Crop,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 14.dp,
                        end = 14.dp,
                        top = if (compact) 8.dp else 11.dp,
                        bottom = if (compact) 8.dp else 10.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // ── Emoji-квадрат ──
                Box(
                    modifier = Modifier
                        .size(if (compact) 36.dp else 42.dp)
                        .clip(RoundedCornerShape(if (compact) 10.dp else 12.dp))
                        .background(habitColor.copy(alpha = if (isCompleted) 0.16f else 0.09f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = habit.emoji,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = if (compact) 17.sp else 20.sp,
                    )
                }

                Spacer(Modifier.width(12.dp))

                // ── Название + метаинфо ──
                Column(modifier = Modifier.weight(1f)) {
                    // Название + streak
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (habit.isBadHabit) {
                            Text(
                                text = habit.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp),
                            ) {
                                Text(
                                    text = "🚫 Табу",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                )
                            }
                        } else {
                            Text(
                                text = habit.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleted || isSkipped)
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                                else
                                    MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                            // Streak badge рядом с названием
                            if (streak > 0 && !isSkipped && !isStreakSafeSkipped) {
                                Text(
                                    text = "🔥$streak",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFF6B2B),
                                )
                            }
                        }
                    }

                    // Подстрока: статус / прогресс
                    Spacer(Modifier.height(2.dp))
                    when {
                        habit.isBadHabit -> Text(
                            text = if (isRelapsed) "Сорвался 😞" else "Чисто сегодня ✓",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isRelapsed) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Medium,
                        )
                        isStreakSafeSkipped -> Text(
                            text = "🛡 Пропущено (серия сохранена)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Medium,
                        )
                        isSkipped -> {
                            Text(
                                text = stringResource(R.string.skipped_label),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            )
                            TextButton(
                                onClick = onUnSkipRequest,
                                modifier = Modifier.height(26.dp),
                                contentPadding = PaddingValues(horizontal = 0.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.cancel_skip),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                )
                            }
                        }
                        habit.targetCount > 1 -> {
                            Text(
                                text = buildString {
                                    append("$completedCount / ${habit.targetCount}")
                                    if (!habit.unit.isNullOrEmpty()) append(" ${habit.unit}")
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isCompleted) habitColor
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Normal,
                            )
                            if (!compact) {
                                Spacer(Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { completedCount.toFloat() / habit.targetCount },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = habitColor,
                                    trackColor = habitColor.copy(alpha = 0.15f),
                                )
                            }
                        }
                        isCompleted -> Text(
                            text = stringResource(R.string.done_check),
                            style = MaterialTheme.typography.bodySmall,
                            color = habitColor,
                            fontWeight = FontWeight.Medium,
                        )
                        else -> Text(
                            text = "Не выполнено",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                // Прогресс дуги для multi-count
                val arcProgress by animateFloatAsState(
                    targetValue = if (habit.targetCount > 1) completedCount.toFloat() / habit.targetCount else 0f,
                    animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow),
                    label = "arcProgress",
                )

                // ── Кнопки справа ──
                if (habit.isBadHabit) {
                    if (isRelapsed) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .scale(buttonScale)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error)
                                .clickable(onClick = onToggleWithHaptic),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    } else {
                        TextButton(
                            onClick = onToggleWithHaptic,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = "Сорвался",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.70f),
                            )
                        }
                    }
                } else {
                    // Кнопка "−"
                    AnimatedVisibility(
                        visible = completedCount > 0 && !isSkipped,
                        enter = scaleIn(spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)) + fadeIn(tween(150)),
                        exit = scaleOut(tween(150)) + fadeOut(tween(150)),
                    ) {
                        IconButton(onClick = onDecrease, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Remove, null, tint = habitColor, modifier = Modifier.size(16.dp))
                        }
                    }

                    // Кнопка таймера
                    if (habit.timerSeconds > 0 && !isCompleted && !isSkipped) {
                        IconButton(onClick = onTimer, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Timer, null, tint = habitColor, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(Modifier.width(4.dp))

                    // Главная кнопка-галочка
                    if (!isSkipped && !isStreakSafeSkipped) {
                        Box(contentAlignment = Alignment.Center) {
                            if (habit.targetCount > 1 && !isCompleted) {
                                // Кружок с дугой прогресса
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .scale(buttonScale)
                                        .clickable(onClick = onToggleWithHaptic),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Canvas(modifier = Modifier.size(40.dp)) {
                                        drawCircle(color = habitColor.copy(alpha = 0.14f))
                                        if (arcProgress > 0f) {
                                            drawArc(
                                                color = habitColor,
                                                startAngle = -90f,
                                                sweepAngle = 360f * arcProgress,
                                                useCenter = true,
                                            )
                                        }
                                        drawCircle(
                                            color = habitColor.copy(alpha = 0.4f),
                                            style = Stroke(width = 1.5.dp.toPx()),
                                        )
                                    }
                                    if (completedCount > 0) {
                                        Text(
                                            text = "$completedCount",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (arcProgress >= 0.5f) Color.White else habitColor,
                                        )
                                    }
                                }
                            } else {
                                // Круглая кнопка с бордером / заливкой
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .scale(buttonScale)
                                        .clip(CircleShape)
                                        .background(buttonColor)
                                        .then(
                                            if (!isCompleted) Modifier.border(
                                                1.5.dp, habitColor.copy(alpha = 0.5f), CircleShape
                                            ) else Modifier
                                        )
                                        .clickable(onClick = onToggleWithHaptic),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (isCompleted) {
                                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }

                            // Частицы при выполнении
                            val prog = particleProgress.value
                            if (prog > 0f && prog < 1f) {
                                Canvas(modifier = Modifier.size(100.dp)) {
                                    val cx = size.width / 2f
                                    val cy = size.height / 2f
                                    val maxDist = 44.dp.toPx()
                                    repeat(8) { i ->
                                        val angle = i * 2.0 * Math.PI / 8.0
                                        val dist = maxDist * prog
                                        val alpha = (1f - prog * 1.3f).coerceIn(0f, 1f)
                                        val r = (3f + i % 3).dp.toPx()
                                        drawCircle(
                                            color = habitColor.copy(alpha = alpha),
                                            radius = r,
                                            center = Offset(
                                                cx + (kotlin.math.cos(angle) * dist).toFloat(),
                                                cy + (kotlin.math.sin(angle) * dist).toFloat(),
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── История 7 дней ──
            if (!compact && habitForDate.recentDays.size == 7) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val dayLabels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
                    // index 0 = 6 дней назад, index 6 = сегодня
                    // Вычислим смещение начала: 6 дней назад
                    val today = java.time.LocalDate.now()
                    val startDay = today.minusDays(6)
                    val startDow = startDay.dayOfWeek.value - 1 // 0=Пн

                    val errorColor = MaterialTheme.colorScheme.error
                    val tertiaryColor = MaterialTheme.colorScheme.tertiary

                    habitForDate.recentDays.forEachIndexed { i, done ->
                        val isToday = i == 6
                        val dotColor = if (habit.isBadHabit) when {
                            done -> errorColor
                            isToday -> tertiaryColor.copy(alpha = 0.25f)
                            else -> tertiaryColor.copy(alpha = 0.12f)
                        } else when {
                            done -> habitColor
                            isToday -> habitColor.copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        }
                        val labelColor = if (isToday) {
                            if (habit.isBadHabit) (if (done) errorColor else tertiaryColor)
                            else habitColor
                        } else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                        val label = dayLabels[(startDow + i) % 7]

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = labelColor,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            )
                            Spacer(Modifier.height(3.dp))
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(dotColor)
                                    .then(when {
                                        habit.isBadHabit && isToday && !done -> Modifier.border(
                                            1.5.dp, tertiaryColor.copy(alpha = 0.6f), RoundedCornerShape(5.dp)
                                        )
                                        !habit.isBadHabit && isToday && !done -> Modifier.border(
                                            1.5.dp, habitColor.copy(alpha = 0.5f), RoundedCornerShape(5.dp)
                                        )
                                        else -> Modifier
                                    }),
                            )
                        }
                    }
                }
            }

            // ── Нижняя строка действий (только для невыполненных / нескипнутых) ──
            if (!habit.isBadHabit && !isCompleted && !isSkipped && !isStreakSafeSkipped) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 14.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = onGiveUp,
                        contentPadding = PaddingValues(horizontal = 8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.cant_today),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                        )
                    }
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    )
                    TextButton(
                        onClick = onSkip,
                        contentPadding = PaddingValues(horizontal = 8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.skip),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                        )
                    }
                }
            }
        }
    }
}
