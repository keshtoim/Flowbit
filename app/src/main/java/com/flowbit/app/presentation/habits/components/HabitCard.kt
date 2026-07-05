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
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.flowbit.app.domain.usecase.habit.HabitForDate

@Composable
fun HabitCard(
    habitForDate: HabitForDate,
    onToggle: () -> Unit,
    onDecrease: () -> Unit,
    onGiveUp: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val habit = habitForDate.habit
    val completedCount = habitForDate.entry?.completedCount ?: 0
    val isCompleted = completedCount >= habit.targetCount

    val habitColor = remember(habit.color.hex) {
        try { Color(android.graphics.Color.parseColor(habit.color.hex)) }
        catch (e: Exception) { Color(0xFF00E5C0) }
    }
    val surface = MaterialTheme.colorScheme.surface

    val cardColor by animateColorAsState(
        targetValue = if (isCompleted) habitColor.copy(alpha = 0.12f) else surface,
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
            // Фото-баннер — показывается если задано
            if (habit.photoUri != null) {
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
                    .padding(horizontal = 16.dp, vertical = 14.dp),
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
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (habit.targetCount > 1) {
                        Spacer(Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "$completedCount / ${habit.targetCount}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isCompleted) habitColor
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
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
                            text = "Выполнено ✓",
                            style = MaterialTheme.typography.bodySmall,
                            color = habitColor,
                            fontWeight = FontWeight.Medium,
                        )
                    }

                    // Кнопка подбадривания — только когда не выполнено
                    if (!isCompleted) {
                        TextButton(
                            onClick = onGiveUp,
                            modifier = Modifier.height(28.dp),
                            contentPadding = PaddingValues(horizontal = 0.dp),
                        ) {
                            Text(
                                text = "Не могу сегодня...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            )
                        }
                    }
                }

                Spacer(Modifier.width(8.dp))

                // Кнопка "−" — появляется при completedCount > 0
                AnimatedVisibility(
                    visible = completedCount > 0,
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

                Spacer(Modifier.width(4.dp))

                // Главная кнопка-галочка
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
}
