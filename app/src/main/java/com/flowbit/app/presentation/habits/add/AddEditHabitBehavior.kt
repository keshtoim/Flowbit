package com.flowbit.app.presentation.habits.add

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.flowbit.app.domain.model.Habit
import com.flowbit.app.domain.model.HabitTag
import com.flowbit.app.domain.model.PeriodGoalType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodGoalSection(
    periodGoalType: PeriodGoalType,
    periodGoalCount: Int,
    onTypeChange: (PeriodGoalType) -> Unit,
    onCountChange: (Int) -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Цель на период", style = MaterialTheme.typography.titleMedium)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            PeriodGoalType.entries.forEachIndexed { idx, type ->
                SegmentedButton(
                    selected = periodGoalType == type,
                    onClick = { onTypeChange(type) },
                    shape = SegmentedButtonDefaults.itemShape(idx, PeriodGoalType.entries.size),
                ) { Text(type.label) }
            }
        }
        if (periodGoalType != PeriodGoalType.NONE) {
            val periodLabel = when (periodGoalType) {
                PeriodGoalType.WEEKLY -> "раз за неделю"
                PeriodGoalType.MONTHLY -> "раз за месяц"
                else -> ""
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconButton(
                    onClick = { onCountChange(periodGoalCount - 1) },
                    enabled = periodGoalCount > 1,
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Меньше")
                }
                Text(
                    text = "$periodGoalCount $periodLabel",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { onCountChange(periodGoalCount + 1) }) {
                    Icon(Icons.Default.Add, contentDescription = "Больше")
                }
            }
        }
    }
}

@Composable
fun TagSection(
    tags: List<HabitTag>,
    selectedTagId: Long?,
    onTagSelected: (Long?) -> Unit,
    onCreateTag: (name: String, colorHex: String) -> Unit,
    onDeleteTag: (HabitTag) -> Unit = {},
) {
    var showDialog by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }
    var newTagColor by remember { mutableStateOf("#4A90E2") }
    var tagToDelete by remember { mutableStateOf<HabitTag?>(null) }

    val tagColors = listOf(
        "#4A90E2", "#2ECC71", "#E74C3C", "#9B59B6",
        "#E67E22", "#00E5C0", "#FF69B4", "#F1C40F",
    )

    tagToDelete?.let { tag ->
        AlertDialog(
            onDismissRequest = { tagToDelete = null },
            title = { Text("Удалить тег") },
            text = { Text("Удалить тег «${tag.name}»? Привычки с этим тегом останутся, но тег будет снят.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteTag(tag)
                    if (selectedTagId == tag.id) onTagSelected(null)
                    tagToDelete = null
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { tagToDelete = null }) { Text("Отмена") }
            },
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Новый тег") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newTagName,
                        onValueChange = { newTagName = it },
                        label = { Text("Название тега") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text("Цвет", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        tagColors.forEach { hex ->
                            val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color.Gray }
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .then(if (newTagColor == hex) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
                                    .clickable { newTagColor = hex },
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTagName.isNotBlank()) {
                            onCreateTag(newTagName.trim(), newTagColor)
                            newTagName = ""
                            showDialog = false
                        }
                    }
                ) { Text("Создать") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Отмена") }
            },
        )
    }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Тег", style = MaterialTheme.typography.titleMedium)
        FilterChip(
            selected = selectedTagId == null,
            onClick = { onTagSelected(null) },
            label = { Text("Без тега") },
        )
        if (tags.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                tags.forEach { tag ->
                    val color = try { Color(android.graphics.Color.parseColor(tag.colorHex)) } catch (_: Exception) { Color.Gray }
                    FilterChip(
                        selected = selectedTagId == tag.id,
                        onClick = { onTagSelected(if (selectedTagId == tag.id) null else tag.id) },
                        label = { Text(tag.name) },
                        leadingIcon = {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Удалить тег",
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { tagToDelete = tag },
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                    )
                }
            }
        }
        OutlinedButton(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Создать тег")
        }
    }
}

@Composable
fun TimerSection(
    timerSeconds: Int,
    onTimerSecondsChange: (Int) -> Unit,
) {
    val presets = listOf(0 to "Нет", 300 to "5 мин", 600 to "10 мин", 900 to "15 мин", 1800 to "30 мин", 3600 to "1 ч")
    val isCustom = timerSeconds > 0 && presets.none { it.first == timerSeconds }
    var showCustom by remember(isCustom) { mutableStateOf(isCustom) }
    var customText by remember(timerSeconds) {
        mutableStateOf(if (isCustom) (timerSeconds / 60).toString() else "")
    }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Таймер привычки", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "При запуске таймер отсчитает время и автоматически отметит привычку выполненной",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            presets.forEach { (secs, label) ->
                FilterChip(
                    selected = timerSeconds == secs && !showCustom,
                    onClick = { onTimerSecondsChange(secs); showCustom = false; customText = "" },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                )
            }
            FilterChip(
                selected = showCustom,
                onClick = { showCustom = true },
                label = { Text("Своё", style = MaterialTheme.typography.labelSmall) },
            )
        }
        AnimatedVisibility(
            visible = showCustom,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = customText,
                    onValueChange = { raw ->
                        val digits = raw.filter { it.isDigit() }.take(4)
                        customText = digits
                        val mins = digits.toIntOrNull() ?: 0
                        if (mins in 1..360) onTimerSecondsChange(mins * 60)
                    },
                    label = { Text("Минуты") },
                    placeholder = { Text("25") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    suffix = { Text("мин", style = MaterialTheme.typography.bodySmall) },
                )
            }
        }
        if (timerSeconds > 0) {
            val h = timerSeconds / 3600
            val m = (timerSeconds % 3600) / 60
            val s = timerSeconds % 60
            val formatted = buildString {
                if (h > 0) append("${h} ч ")
                if (m > 0) append("${m} мин ")
                if (s > 0) append("${s} с")
            }.trim()
            Text(
                text = "Таймер: $formatted",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun TabooSection(
    isBadHabit: Boolean,
    onIsBadHabitChange: (Boolean) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isBadHabit)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🚫", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(8.dp))
                    Text("Табу", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Привычка считается выполненной по умолчанию. Нажми «Сорвался», если не удержался.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(12.dp))
            Switch(
                checked = isBadHabit,
                onCheckedChange = onIsBadHabitChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onError,
                    checkedTrackColor = MaterialTheme.colorScheme.error,
                ),
            )
        }
    }
}

@Composable
fun StreakSkipSection(
    allowStreakSkip: Boolean,
    onAllowStreakSkipChange: (Boolean) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (allowStreakSkip)
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.40f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🛡️", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Пропуск без потери серии",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Разрешает пропуск через меню привычки без обнуления серии.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(12.dp))
            Switch(
                checked = allowStreakSkip,
                onCheckedChange = onAllowStreakSkipChange,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StackingSection(
    allHabits: List<Habit>,
    stackAfterHabitId: Long?,
    currentHabitId: Long?,
    onStackAfterChange: (Long?) -> Unit,
) {
    val options = allHabits.filter { it.id != (currentHabitId ?: -1L) }
    if (options.isEmpty()) return

    var expanded by remember { mutableStateOf(false) }
    val selected = options.find { it.id == stackAfterHabitId }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (stackAfterHabitId != null)
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔗", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Habit Stacking",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Выполнять сразу после другой привычки",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
            ) {
                OutlinedTextField(
                    value = selected?.let { "${it.emoji} ${it.name}" } ?: "Не выбрано",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    label = { Text("После привычки") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("— Не привязывать") },
                        onClick = { onStackAfterChange(null); expanded = false },
                    )
                    options.forEach { habit ->
                        DropdownMenuItem(
                            text = { Text("${habit.emoji} ${habit.name}") },
                            onClick = { onStackAfterChange(habit.id); expanded = false },
                        )
                    }
                }
            }
        }
    }
}
