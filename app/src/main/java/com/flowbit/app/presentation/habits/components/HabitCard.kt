package com.flowbit.app.presentation.habits.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.flowbit.app.R
import com.flowbit.app.domain.usecase.habit.HabitForDate

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
) {
    val habit = habitForDate.habit
    val isSkipped = habitForDate.entry?.isSkipped ?: false
    val completedCount = if (isSkipped) 0 else (habitForDate.entry?.completedCount ?: 0)
    // Для Табу: "выполнено" = не сорвался (нет записи или count == 0)
    val isRelapsed = habit.isBadHabit && completedCount > 0
    val isCompleted = if (habit.isBadHabit) !isRelapsed else !isSkipped && completedCount >= habit.targetCount

    val habitColor = remember(habit.color.hex) {
        try { Color(android.graphics.Color.parseColor(habit.color.hex)) }
        catch (e: Exception) { Color(0xFF00E5C0) }
    }
    val surface = MaterialTheme.colorScheme.surface

    val skippedColor = MaterialTheme.colorScheme.errorContainer
    val cardColor by animateColorAsState(
        targetValue = when {
            isRelapsed -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.40f)
            isSkipped -> skippedColor
            isCompleted -> habitColor.copy(alpha = 0.22f)
            else -> surface
        },
        animationSpec = tween(300),
        label = "cardColor",
    )
    val buttonColor by animateColorAsState(
        targetValue = if (isCompleted) habitColor else habitColor.copy(alpha = 0.15f),
        animationSpec = tween(300),
        label = "buttonColor",
    )
    val buttonScale by animateFloatAsState(
        targetValue = if (isCompleted) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "buttonScale",
    )

    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 0.dp else 2.dp),
    ) {
        Column {
            // Фото-баннер — показывается если задано и не скрыто
            if (habit.photoUri != null && !habit.isPhotoHidden) {
                AsyncImage(
                    model = habit.photoUri,
                    contentDescription = "Фото привычки",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    contentScale = ContentScale.Crop,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = if (!isCompleted && !isSkipped) 4.dp else 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Emoji-кружок
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(habitColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = habit.emoji, style = MaterialTheme.typography.headlineSmall)
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Бейдж «Табу» рядом с названием
                    if (habit.isBadHabit) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = habit.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    text = "🚫 Табу",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                )
                            }
                        }
                    } else {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    if (habit.isBadHabit) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (isRelapsed) "Сорвался 😞" else "Чисто сегодня ✓",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isRelapsed) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Medium,
                        )
                    } else if (isSkipped) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.skipped_label),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                        TextButton(
                            onClick = onUnSkipRequest,
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.cancel_skip),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                            )
                        }
                    } else {
                        if (habit.targetCount > 1) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = buildString {
                                    append("$completedCount / ${habit.targetCount}")
                                    if (!habit.unit.isNullOrEmpty()) append(" ${habit.unit}")
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isCompleted) habitColor
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal,
                            )
                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { completedCount.toFloat() / habit.targetCount },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = habitColor,
                                trackColor = habitColor.copy(alpha = 0.2f),
                            )
                        } else if (isCompleted) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.done_check),
                                style = MaterialTheme.typography.bodySmall,
                                color = habitColor,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }

                Spacer(Modifier.width(8.dp))

                if (habit.isBadHabit) {
                    // Для Табу: кнопка «Сорвался» / «Отменить»
                    val tabooButtonColor by animateColorAsState(
                        targetValue = if (isRelapsed) MaterialTheme.colorScheme.error
                                      else MaterialTheme.colorScheme.error.copy(alpha = 0.18f),
                        animationSpec = tween(300), label = "tabooBtn",
                    )
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .scale(buttonScale)
                            .clip(CircleShape)
                            .background(tabooButtonColor)
                            .clickable(onClick = onToggle),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            if (isRelapsed) Icons.Default.Close else Icons.Default.Close,
                            contentDescription = if (isRelapsed) "Отменить срыв" else "Сорвался",
                            tint = if (isRelapsed) Color.White
                                   else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                } else {
                    // Кнопка "−" — появляется при completedCount > 0 и не пропущено
                    AnimatedVisibility(
                        visible = completedCount > 0 && !isSkipped,
                        enter = scaleIn(tween(180)) + fadeIn(tween(180)),
                        exit = scaleOut(tween(180)) + fadeOut(tween(180)),
                    ) {
                        IconButton(
                            onClick = onDecrease,
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Уменьшить",
                                tint = habitColor,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }

                    // Кнопка таймера — показывается если таймер задан и не выполнено
                    if (habit.timerSeconds > 0 && !isCompleted && !isSkipped) {
                        IconButton(
                            onClick = onTimer,
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = "Запустить таймер",
                                tint = habitColor,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }

                    Spacer(Modifier.width(4.dp))

                    // Главная кнопка-галочка (скрыта когда пропущено)
                    if (!isSkipped) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .scale(buttonScale)
                                .clip(CircleShape)
                                .background(buttonColor)
                                .clickable(onClick = onToggle),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isCompleted) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Выполнено",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }
                    }
                }
            }
            if (!habit.isBadHabit && !isCompleted && !isSkipped) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 16.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = onGiveUp,
                        contentPadding = PaddingValues(horizontal = 8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.cant_today),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        )
                    }
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                    )
                    TextButton(
                        onClick = onSkip,
                        contentPadding = PaddingValues(horizontal = 8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.skip),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        )
                    }
                }
            }
        }
    }
}
