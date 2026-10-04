package com.flowbit.app.presentation.habits.add

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flowbit.app.domain.model.HabitColor
import com.flowbit.app.domain.model.HabitFrequency
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

// ── Шаблоны привычек ─────────────────────────────────────────────────────────

data class HabitTemplate(
    val emoji: String,
    val name: String,
    val targetCount: Int = 1,
    val unit: String? = null,
    val color: HabitColor = HabitColor.TEAL,
    val category: String,
)

val HABIT_TEMPLATES = listOf(
    HabitTemplate("🏃", "Пробежка", 1, null, HabitColor.GREEN, "Спорт"),
    HabitTemplate("💪", "Тренировка", 1, null, HabitColor.RED, "Спорт"),
    HabitTemplate("🧘", "Медитация", 10, "мин", HabitColor.PURPLE, "Здоровье"),
    HabitTemplate("💧", "Вода", 8, "стак.", HabitColor.BLUE, "Здоровье"),
    HabitTemplate("📚", "Чтение", 20, "стр.", HabitColor.ORANGE, "Развитие"),
    HabitTemplate("✍️", "Дневник", 1, null, HabitColor.YELLOW, "Развитие"),
    HabitTemplate("🛌", "Сон до 23:00", 1, null, HabitColor.INDIGO, "Режим"),
    HabitTemplate("🥗", "Овощи в рационе", 2, "порц.", HabitColor.GREEN, "Питание"),
    HabitTemplate("📵", "Без соц.сетей", 1, null, HabitColor.TEAL, "Цифровой детокс"),
    HabitTemplate("🧹", "Уборка", 1, null, HabitColor.ORANGE, "Дом"),
    HabitTemplate("💊", "Витамины", 1, null, HabitColor.RED, "Здоровье"),
    HabitTemplate("🚶", "10 000 шагов", 10000, "шаг.", HabitColor.GREEN, "Спорт"),
    HabitTemplate("🌍", "Иностранный язык", 15, "мин", HabitColor.BLUE, "Развитие"),
    HabitTemplate("🎸", "Музыкальный инструмент", 20, "мин", HabitColor.PURPLE, "Хобби"),
    HabitTemplate("🚭", "Не курить", 1, null, HabitColor.TEAL, "Здоровье"),
)

@Composable
fun TemplatePickerDialog(
    onDismiss: () -> Unit,
    onSelect: (HabitTemplate) -> Unit,
) {
    val categories = HABIT_TEMPLATES.map { it.category }.distinct()
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val filtered = if (selectedCategory == null) HABIT_TEMPLATES
                   else HABIT_TEMPLATES.filter { it.category == selectedCategory }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выбрать шаблон") },
        text = {
            Column {
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text("Все") },
                        )
                    }
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                filtered.forEach { template ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(template); onDismiss() }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(template.emoji, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(template.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            val hint = buildString {
                                append(template.category)
                                if (template.targetCount > 1) append(" · ${template.targetCount}${template.unit?.let { " $it" } ?: ""}")
                            }
                            Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

private val PRESET_EMOJIS = listOf(
    // Спорт и здоровье
    "🏃", "💪", "🧘", "🏊", "🚴", "🤸", "🏋️", "⚽",
    "🏀", "🎾", "🏃‍♀️", "🧗", "🤾", "🥊", "🛹", "🏄",
    // Питание и вода
    "💧", "🥗", "🍎", "☕", "🥤", "🫖", "🥦", "🥑",
    "🍋", "🫐", "🥕", "🍳", "🥜", "🫚", "🍇", "🥝",
    // Ум и продуктивность
    "📚", "✍️", "🎯", "🧠", "💡", "📝", "📊", "⏰",
    "🔬", "🎓", "📖", "🗂️", "🖊️", "💻", "📐", "🧩",
    // Забота о себе
    "😴", "🛁", "🪥", "💊", "🌿", "💆", "🪞", "🫧",
    "🌡️", "🧴", "🪑", "🌬️", "🫁", "🧖", "💅", "🛌",
    // Творчество
    "🎨", "🎵", "🎸", "🎭", "📸", "🎬", "🎤", "🎻",
    "🖌️", "✏️", "🎹", "🎷", "🪗", "🎺", "🎲", "🖼️",
    // Финансы и жизнь
    "💰", "📱", "🌍", "🚶", "🧹", "🏠", "🛒", "📦",
    "🔑", "📫", "🏡", "🚿", "🌅", "🌄", "🚗", "✈️",
    // Природа и вдохновение
    "🌱", "☀️", "🌙", "🌸", "🌊", "🔥", "⚡", "🌈",
    "🍀", "🦋", "🌺", "❄️", "🌻", "🍂", "🌴", "🦉",
    // Эмоции и цели
    "✅", "🎯", "🏆", "🥇", "💎", "⭐", "🌟", "🎉",
    "❤️", "🙏", "💪", "🔥", "🫶", "😊", "🥰", "🎁",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiAndNameSection(
    name: String,
    emoji: String,
    onNameChange: (String) -> Unit,
    onEmojiChange: (String) -> Unit,
) {
    var showEmojiPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Название и иконка", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { showEmojiPicker = true },
                contentAlignment = Alignment.Center,
            ) {
                Text(text = emoji, style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.width(12.dp))
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Название привычки") },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
        }
        if (showEmojiPicker) {
            EmojiPickerDialog(
                currentEmoji = emoji,
                onEmojiSelected = {
                    onEmojiChange(it)
                    showEmojiPicker = false
                },
                onDismiss = { showEmojiPicker = false },
            )
        }
    }
}

@Composable
fun EmojiPickerDialog(
    currentEmoji: String,
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var customEmoji by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите эмодзи") },
        text = {
            Column {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.height(200.dp),
                ) {
                    items(PRESET_EMOJIS) { e ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (e == currentEmoji) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { onEmojiSelected(e) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = e, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = customEmoji,
                    onValueChange = { if (it.length <= 2) customEmoji = it },
                    label = { Text("Свой эмодзи") },
                    trailingIcon = {
                        if (customEmoji.isNotBlank()) {
                            TextButton(onClick = { onEmojiSelected(customEmoji) }) {
                                Text("ОК")
                            }
                        }
                    },
                    singleLine = true,
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } },
    )
}

@Composable
fun ColorPickerSection(
    selectedColor: HabitColor,
    customColorHex: String?,
    onColorSelected: (HabitColor) -> Unit,
    onCustomColorHexChange: (String) -> Unit,
) {
    var showCustomDialog by remember { mutableStateOf(false) }
    var hexInput by remember(customColorHex) { mutableStateOf(customColorHex ?: "#") }

    val presetColors = HabitColor.entries.filterNot { it == HabitColor.CUSTOM }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Цвет", style = MaterialTheme.typography.titleMedium)
        // Скроллируемый ряд цветов + кнопка «своё»
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            presetColors.forEach { color ->
                val isSelected = selectedColor == color
                val parsedColor = remember(color.hex) {
                    Color(android.graphics.Color.parseColor(color.hex))
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(parsedColor)
                        .then(
                            if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                            else Modifier
                        )
                        .clickable { onColorSelected(color) },
                )
            }
            // Кнопка «🎨 Своё»
            val isCustomSelected = selectedColor == HabitColor.CUSTOM
            val displayColor = if (isCustomSelected && customColorHex != null) {
                runCatching { Color(android.graphics.Color.parseColor(customColorHex)) }
                    .getOrElse { Color.Gray }
            } else Color.Gray
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isCustomSelected) displayColor else MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        2.dp,
                        if (isCustomSelected) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.outline,
                        CircleShape,
                    )
                    .clickable { showCustomDialog = true },
                contentAlignment = Alignment.Center,
            ) {
                Text("🎨", style = MaterialTheme.typography.labelMedium)
            }
        }
        // Показываем выбранный кастомный цвет
        if (selectedColor == HabitColor.CUSTOM && customColorHex != null) {
            Text(
                text = "Свой цвет: $customColorHex",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }

    // Диалог выбора произвольного цвета
    if (showCustomDialog) {
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = { Text("Свой цвет") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = hexInput,
                        onValueChange = { raw ->
                            val clean = raw.trim()
                            hexInput = if (clean.startsWith("#")) clean.take(7) else "#${clean.take(6)}"
                        },
                        label = { Text("HEX-код") },
                        placeholder = { Text("#FF5733") },
                        singleLine = true,
                    )
                    // Превью
                    val previewColor = runCatching {
                        if (hexInput.length == 7) Color(android.graphics.Color.parseColor(hexInput))
                        else null
                    }.getOrNull()
                    if (previewColor != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(previewColor),
                            )
                            Text(hexInput, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    // Быстрые пресеты из расширенной палитры
                    val quickColors = listOf(
                        "#FF5733","#C70039","#900C3F","#581845",
                        "#1ABC9C","#2980B9","#8E44AD","#F39C12",
                        "#D35400","#27AE60","#2C3E50","#7F8C8D",
                    )
                    Text("Быстрый выбор", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        modifier = Modifier.height(80.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(quickColors.size) { idx ->
                            val qc = quickColors[idx]
                            val qParsed = runCatching { Color(android.graphics.Color.parseColor(qc)) }
                                .getOrElse { Color.Gray }
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(qParsed)
                                    .clickable { hexInput = qc },
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (hexInput.length == 7) {
                        onCustomColorHexChange(hexInput)
                        showCustomDialog = false
                    }
                }) { Text("Выбрать") }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) { Text("Отмена") }
            },
        )
    }
}

@Composable
fun TargetCountSection(
    targetCount: Int,
    onTargetCountChange: (Int) -> Unit,
    unit: String = "",
    onUnitChange: (String) -> Unit = {},
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Количество в день", style = MaterialTheme.typography.titleMedium)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(onClick = { onTargetCountChange(targetCount - 1) }) {
                Icon(Icons.Default.Remove, "Уменьшить")
            }
            Text(
                text = targetCount.toString(),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            IconButton(onClick = { onTargetCountChange(targetCount + 1) }) {
                Icon(Icons.Default.Add, "Увеличить")
            }
            OutlinedTextField(
                value = unit,
                onValueChange = onUnitChange,
                placeholder = { Text("км, мл…") },
                label = { Text("Единица") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                supportingText = { Text("необяз.") },
            )
        }
        Text(
            text = if (unit.isNotBlank()) "Отображение: $targetCount $unit в день"
                   else "Необязательно — напр.: км, стаканов, страниц",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrequencySection(
    frequency: HabitFrequency,
    scheduledDays: Set<DayOfWeek>,
    onFrequencyChange: (HabitFrequency) -> Unit,
    onDayToggle: (DayOfWeek) -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Частота", style = MaterialTheme.typography.titleMedium)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = frequency == HabitFrequency.DAILY,
                onClick = { onFrequencyChange(HabitFrequency.DAILY) },
                shape = SegmentedButtonDefaults.itemShape(0, 2),
            ) { Text("Каждый день") }
            SegmentedButton(
                selected = frequency == HabitFrequency.CUSTOM,
                onClick = { onFrequencyChange(HabitFrequency.CUSTOM) },
                shape = SegmentedButtonDefaults.itemShape(1, 2),
            ) { Text("По дням") }
        }
        if (frequency == HabitFrequency.CUSTOM) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                DayOfWeek.entries.forEach { day ->
                    FilterChip(
                        selected = day in scheduledDays,
                        onClick = { onDayToggle(day) },
                        label = { Text(day.getDisplayName(TextStyle.NARROW, Locale("ru"))) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartDateSection(
    startDate: LocalDate,
    onStartDateChange: (LocalDate) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Дата начала", style = MaterialTheme.typography.titleMedium)
        OutlinedButton(onClick = { showPicker = true }) {
            Text(startDate.toString())
        }
        if (showPicker) {
            val state = rememberDatePickerState(
                initialSelectedDateMillis = startDate.toEpochDay() * 86_400_000L
            )
            DatePickerDialog(
                onDismissRequest = { showPicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        state.selectedDateMillis?.let {
                            onStartDateChange(LocalDate.ofEpochDay(it / 86_400_000L))
                        }
                        showPicker = false
                    }) { Text("ОК") }
                },
            ) {
                DatePicker(state = state)
            }
        }
    }
}

@Composable
fun WidgetSection(
    showInWidget: Boolean,
    onShowInWidgetChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text("Показывать в виджете", style = MaterialTheme.typography.titleMedium)
            Text(
                "Привычка появится на экране телефона",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = showInWidget, onCheckedChange = onShowInWidgetChange)
    }
}

