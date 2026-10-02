package com.flowbit.app.presentation.habits.add

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowbit.app.domain.model.HabitColor
import com.flowbit.app.domain.model.HabitTag

// ── Вспомогательные ───────────────────────────────────────────────────────────

private fun habitColorToCompose(color: HabitColor, customHex: String?): Color = try {
    Color(android.graphics.Color.parseColor(
        if (color == HabitColor.CUSTOM) customHex ?: "#FF6B4A" else color.hex
    ))
} catch (e: Exception) { Color(0xFFFF6B4A) }

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.08.sp,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp),
    )
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column { content() }
    }
}

// ── Предпросмотр карточки (Variant A) ────────────────────────────────────────

@Composable
private fun HabitPreviewCard(uiState: AddEditHabitUiState) {
    val habitColor = habitColorToCompose(uiState.color, uiState.customColorHex)
    val displayName = uiState.name.ifBlank { "Название привычки" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                // Emoji
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(habitColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(uiState.emoji, style = MaterialTheme.typography.titleMedium, fontSize = 20.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.name.isBlank()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                                else MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(2.dp))
                    if (uiState.targetCount > 1) {
                        Text(
                            text = "0 / ${uiState.targetCount}${uiState.unit.let { if (it.isNotBlank()) " $it" else "" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Text(
                            text = "Не выполнено",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                }
                // Check button preview
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, habitColor.copy(alpha = 0.5f), CircleShape),
                )
            }
            if (uiState.targetCount > 1) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { 0f },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = habitColor,
                    trackColor = habitColor.copy(alpha = 0.15f),
                )
            }
            // 7-day dots
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                val days = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
                days.forEachIndexed { i, label ->
                    val isToday = i == 6
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = label,
                            fontSize = 9.sp,
                            color = if (isToday) habitColor
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        )
                        Spacer(Modifier.height(3.dp))
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(
                                    if (isToday) habitColor.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                )
                                .then(
                                    if (isToday) Modifier.border(1.5.dp, habitColor.copy(alpha = 0.5f), RoundedCornerShape(5.dp))
                                    else Modifier
                                ),
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// LAYOUT A · Live Preview
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun FormLayoutA(
    uiState: AddEditHabitUiState,
    allTags: List<HabitTag>,
    viewModel: AddEditHabitViewModel,
    paddingValues: PaddingValues,
    habitId: Long?,
    showTemplatePicker: Boolean,
    onShowTemplatePicker: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Spacer(Modifier.height(4.dp))

        // Live preview
        SectionLabel("ПРЕДПРОСМОТР")
        HabitPreviewCard(uiState)
        Spacer(Modifier.height(4.dp))

        // Основное
        SectionLabel("ОСНОВНОЕ")
        SectionCard {
            EmojiAndNameSection(uiState.name, uiState.emoji, viewModel::onNameChange, viewModel::onEmojiChange)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(8.dp))
            ColorPickerSection(uiState.color, uiState.customColorHex, viewModel::onColorChange, viewModel::onCustomColorHexChange)
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            TargetCountSection(uiState.targetCount, viewModel::onTargetCountChange, uiState.unit, viewModel::onUnitChange)
        }

        // Расписание
        SectionLabel("РАСПИСАНИЕ")
        SectionCard {
            FrequencySection(uiState.frequency, uiState.scheduledDays, viewModel::onFrequencyChange, viewModel::onDayToggle)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            StartDateSection(uiState.startDate, viewModel::onStartDateChange)
        }

        // Уведомления
        SectionLabel("УВЕДОМЛЕНИЯ")
        SectionCard {
            RemindersSection(uiState.reminders, viewModel::onAddReminder, viewModel::onRemoveReminder, viewModel::onToggleReminder)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            RecurringReminderSection(
                uiState.recurringEnabled, uiState.recurringStartHour, uiState.recurringEndHour,
                uiState.recurringIntervalHours, viewModel::onRecurringToggle,
                viewModel::onRecurringStartHour, viewModel::onRecurringEndHour, viewModel::onRecurringInterval,
            )
        }

        // Дополнительно
        SectionLabel("ДОПОЛНИТЕЛЬНО")
        SectionCard {
            WidgetSection(uiState.showInWidget, viewModel::onShowInWidgetChange)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            PhotoSection(uiState.photoUri, uiState.isPhotoHidden, viewModel::onPhotoSelected, viewModel::onIsPhotoHiddenChange)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            AudioSection(uiState.audioUri, viewModel::onAudioSelected)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            PeriodGoalSection(uiState.periodGoalType, uiState.periodGoalCount, viewModel::onPeriodGoalTypeChange, viewModel::onPeriodGoalCountChange)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            TimerSection(uiState.timerSeconds, viewModel::onTimerSecondsChange)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            TagSection(allTags, uiState.tagId, viewModel::onTagSelected, viewModel::createTag, viewModel::deleteTag)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            TabooSection(uiState.isBadHabit, viewModel::onIsBadHabitChange)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            StreakSkipSection(uiState.allowStreakSkip, viewModel::onAllowStreakSkipChange)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            StackingSection(uiState.allHabits, uiState.stackAfterHabitId, habitId, viewModel::onStackAfterHabitChange)
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// LAYOUT B · Секции-аккордеон
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun FormLayoutB(
    uiState: AddEditHabitUiState,
    allTags: List<HabitTag>,
    viewModel: AddEditHabitViewModel,
    paddingValues: PaddingValues,
    habitId: Long?,
) {
    var coreExpanded      by rememberSaveable { mutableStateOf(true) }
    var schedExpanded     by rememberSaveable { mutableStateOf(true) }
    var notifExpanded     by rememberSaveable { mutableStateOf(false) }
    var advancedExpanded  by rememberSaveable { mutableStateOf(false) }

    val habitColor = habitColorToCompose(uiState.color, uiState.customColorHex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState()),
    ) {
        // Hero: большое эмодзи + имя
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(habitColor.copy(alpha = 0.18f))
                    .clickable { /* opens emoji picker — handled inside EmojiAndNameSection */ },
                contentAlignment = Alignment.Center,
            ) {
                Text(uiState.emoji, fontSize = 34.sp)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = uiState.name.ifBlank { "Название привычки" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (uiState.name.isBlank()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                        else MaterialTheme.colorScheme.onSurface,
            )
        }

        // Accordion sections
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Основное
            AccordionSection(
                title = "🎨  Основное",
                expanded = coreExpanded,
                onToggle = { coreExpanded = !coreExpanded },
            ) {
                EmojiAndNameSection(uiState.name, uiState.emoji, viewModel::onNameChange, viewModel::onEmojiChange)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(8.dp))
                ColorPickerSection(uiState.color, uiState.customColorHex, viewModel::onColorChange, viewModel::onCustomColorHexChange)
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                TargetCountSection(uiState.targetCount, viewModel::onTargetCountChange, uiState.unit, viewModel::onUnitChange)
            }

            // Расписание
            AccordionSection(
                title = "📅  Расписание",
                expanded = schedExpanded,
                onToggle = { schedExpanded = !schedExpanded },
            ) {
                FrequencySection(uiState.frequency, uiState.scheduledDays, viewModel::onFrequencyChange, viewModel::onDayToggle)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                StartDateSection(uiState.startDate, viewModel::onStartDateChange)
            }

            // Уведомления
            AccordionSection(
                title = "🔔  Уведомления",
                expanded = notifExpanded,
                onToggle = { notifExpanded = !notifExpanded },
            ) {
                RemindersSection(uiState.reminders, viewModel::onAddReminder, viewModel::onRemoveReminder, viewModel::onToggleReminder)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                RecurringReminderSection(
                    uiState.recurringEnabled, uiState.recurringStartHour, uiState.recurringEndHour,
                    uiState.recurringIntervalHours, viewModel::onRecurringToggle,
                    viewModel::onRecurringStartHour, viewModel::onRecurringEndHour, viewModel::onRecurringInterval,
                )
            }

            // Дополнительно
            AccordionSection(
                title = "⚙️  Дополнительно",
                expanded = advancedExpanded,
                onToggle = { advancedExpanded = !advancedExpanded },
            ) {
                WidgetSection(uiState.showInWidget, viewModel::onShowInWidgetChange)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                PhotoSection(uiState.photoUri, uiState.isPhotoHidden, viewModel::onPhotoSelected, viewModel::onIsPhotoHiddenChange)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                AudioSection(uiState.audioUri, viewModel::onAudioSelected)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                PeriodGoalSection(uiState.periodGoalType, uiState.periodGoalCount, viewModel::onPeriodGoalTypeChange, viewModel::onPeriodGoalCountChange)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                TimerSection(uiState.timerSeconds, viewModel::onTimerSecondsChange)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                TagSection(allTags, uiState.tagId, viewModel::onTagSelected, viewModel::createTag, viewModel::deleteTag)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                TabooSection(uiState.isBadHabit, viewModel::onIsBadHabitChange)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                StreakSkipSection(uiState.allowStreakSkip, viewModel::onAllowStreakSkipChange)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                StackingSection(uiState.allHabits, uiState.stackAfterHabitId, habitId, viewModel::onStackAfterHabitChange)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AccordionSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    content()
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// LAYOUT C · Мастер (2 шага)
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun FormLayoutC(
    uiState: AddEditHabitUiState,
    allTags: List<HabitTag>,
    viewModel: AddEditHabitViewModel,
    paddingValues: PaddingValues,
    habitId: Long?,
    onSave: () -> Unit,
) {
    var step by rememberSaveable { mutableStateOf(1) }
    val habitColor = habitColorToCompose(uiState.color, uiState.customColorHex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
    ) {
        // Step bar
        WizardStepBar(step = step, totalSteps = 2)

        if (step == 1) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Spacer(Modifier.height(8.dp))

                // Большое эмодзи по центру
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(habitColor.copy(alpha = 0.2f))
                            .border(2.dp, habitColor.copy(alpha = 0.35f), RoundedCornerShape(22.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(uiState.emoji, fontSize = 38.sp)
                    }
                }

                SectionCard {
                    EmojiAndNameSection(uiState.name, uiState.emoji, viewModel::onNameChange, viewModel::onEmojiChange)
                }
                SectionCard {
                    Spacer(Modifier.height(8.dp))
                    ColorPickerSection(uiState.color, uiState.customColorHex, viewModel::onColorChange, viewModel::onCustomColorHexChange)
                    Spacer(Modifier.height(8.dp))
                }
                SectionCard {
                    TargetCountSection(uiState.targetCount, viewModel::onTargetCountChange, uiState.unit, viewModel::onUnitChange)
                }

                Spacer(Modifier.height(4.dp))

                // Кнопка Далее
                Button(
                    onClick = { step = 2 },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Text("Далее →", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Spacer(Modifier.height(16.dp))
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Spacer(Modifier.height(8.dp))

                SectionLabel("РАСПИСАНИЕ")
                SectionCard {
                    FrequencySection(uiState.frequency, uiState.scheduledDays, viewModel::onFrequencyChange, viewModel::onDayToggle)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    StartDateSection(uiState.startDate, viewModel::onStartDateChange)
                }

                SectionLabel("УВЕДОМЛЕНИЯ")
                SectionCard {
                    RemindersSection(uiState.reminders, viewModel::onAddReminder, viewModel::onRemoveReminder, viewModel::onToggleReminder)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    RecurringReminderSection(
                        uiState.recurringEnabled, uiState.recurringStartHour, uiState.recurringEndHour,
                        uiState.recurringIntervalHours, viewModel::onRecurringToggle,
                        viewModel::onRecurringStartHour, viewModel::onRecurringEndHour, viewModel::onRecurringInterval,
                    )
                }

                SectionLabel("ДОПОЛНИТЕЛЬНО")
                SectionCard {
                    WidgetSection(uiState.showInWidget, viewModel::onShowInWidgetChange)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    PhotoSection(uiState.photoUri, uiState.isPhotoHidden, viewModel::onPhotoSelected, viewModel::onIsPhotoHiddenChange)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    AudioSection(uiState.audioUri, viewModel::onAudioSelected)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    PeriodGoalSection(uiState.periodGoalType, uiState.periodGoalCount, viewModel::onPeriodGoalTypeChange, viewModel::onPeriodGoalCountChange)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    TimerSection(uiState.timerSeconds, viewModel::onTimerSecondsChange)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    TagSection(allTags, uiState.tagId, viewModel::onTagSelected, viewModel::createTag, viewModel::deleteTag)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    TabooSection(uiState.isBadHabit, viewModel::onIsBadHabitChange)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    StreakSkipSection(uiState.allowStreakSkip, viewModel::onAllowStreakSkipChange)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    StackingSection(uiState.allHabits, uiState.stackAfterHabitId, habitId, viewModel::onStackAfterHabitChange)
                }

                Spacer(Modifier.height(4.dp))

                Button(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Сохранить привычку", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                TextButton(
                    onClick = { step = 1 },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("← Назад", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun WizardStepBar(step: Int, totalSteps: Int) {
    val primary = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalSteps) { i ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (i < step) primary else muted),
            )
        }
    }
}
