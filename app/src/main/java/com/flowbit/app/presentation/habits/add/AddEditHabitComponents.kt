package com.flowbit.app.presentation.habits.add

import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.flowbit.app.domain.model.HabitColor
import com.flowbit.app.domain.model.HabitFrequency
import com.flowbit.app.domain.model.HabitReminder
import com.flowbit.app.domain.model.HabitTag
import com.flowbit.app.domain.model.PeriodGoalType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

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

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = unit,
                onValueChange = onUnitChange,
                placeholder = { Text("ед.") },
                label = { Text("Единица") },
                modifier = Modifier.width(110.dp),
                singleLine = true,
            )
        }
        if (unit.isNotBlank()) {
            Text(
                text = "Отображение: $targetCount $unit в день",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun FrequencySection(
    frequency: HabitFrequency,
    scheduledDays: Set<DayOfWeek>,
    onFrequencyChange: (HabitFrequency) -> Unit,
    onDayToggle: (DayOfWeek) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Частота", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = frequency == HabitFrequency.DAILY,
                onClick = { onFrequencyChange(HabitFrequency.DAILY) },
                label = { Text("Каждый день") },
            )
            FilterChip(
                selected = frequency == HabitFrequency.CUSTOM,
                onClick = { onFrequencyChange(HabitFrequency.CUSTOM) },
                label = { Text("По дням") },
            )
        }
        if (frequency == HabitFrequency.CUSTOM) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                DayOfWeek.entries.forEach { day ->
                    FilterChip(
                        selected = day in scheduledDays,
                        onClick = { onDayToggle(day) },
                        label = {
                            Text(day.getDisplayName(TextStyle.NARROW, Locale("ru")))
                        },
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

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
        modifier = Modifier.fillMaxWidth(),
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

@Composable
fun RemindersSection(
    reminders: List<HabitReminder>,
    onAddReminder: (LocalTime) -> Unit,
    onRemoveReminder: (HabitReminder) -> Unit,
    onToggleReminder: (HabitReminder) -> Unit,
) {
    var showTimePicker by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Напоминания", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { showTimePicker = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Добавить")
            }
        }
        reminders.forEach { reminder ->
            ReminderItem(
                reminder = reminder,
                onToggle = { onToggleReminder(reminder) },
                onDelete = { onRemoveReminder(reminder) },
            )
        }
        if (showTimePicker) {
            TimePickerDialog(
                onTimeSelected = { onAddReminder(it); showTimePicker = false },
                onDismiss = { showTimePicker = false },
            )
        }
    }
}

@Composable
fun ReminderItem(
    reminder: HabitReminder,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "%02d:%02d".format(reminder.time.hour, reminder.time.minute),
                style = MaterialTheme.typography.titleMedium,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = reminder.isEnabled, onCheckedChange = { onToggle() })
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onDelete) { Text("Удалить") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Время напоминания") },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = {
                onTimeSelected(LocalTime.of(state.hour, state.minute))
            }) { Text("ОК") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

@Composable
fun PhotoSection(
    photoUri: String?,
    isPhotoHidden: Boolean,
    onPhotoSelected: (String?) -> Unit,
    onIsPhotoHiddenChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current

    // Шаг 2: кадрирование
    val cropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.toString()?.let { onPhotoSelected(it) }
        }
    }

    // Шаг 1: выбор из галереи → сразу запускает кадрирование
    val pickLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { picked ->
            cropLauncher.launch(
                CropImageContractOptions(
                    uri = picked,
                    cropImageOptions = CropImageOptions(
                        imageSourceIncludeCamera = false,
                        imageSourceIncludeGallery = false,
                    ),
                )
            )
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Фото привычки", style = MaterialTheme.typography.titleMedium)

        if (photoUri != null) {
            Box {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Фото привычки",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop,
                )
                // Кнопка кадрирования (перекроп)
                IconButton(
                    onClick = {
                        cropLauncher.launch(
                            CropImageContractOptions(
                                uri = Uri.parse(photoUri),
                                cropImageOptions = CropImageOptions(),
                            )
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                ) {
                    Icon(
                        Icons.Default.Crop,
                        contentDescription = "Кадрировать",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp),
                    )
                }
                // Кнопка удаления
                IconButton(
                    onClick = { onPhotoSelected(null) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить фото",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            // Переключатель скрытия на общем экране
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        Icons.Default.VisibilityOff,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Column {
                        Text("Скрыть на общем экране", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Видно только в деталях привычки",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Switch(checked = isPhotoHidden, onCheckedChange = onIsPhotoHiddenChange)
            }
        } else {
            OutlinedButton(
                onClick = { pickLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Image, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Выбрать и кадрировать фото")
            }
        }
    }
}

@Composable
fun AudioSection(
    audioUri: String?,
    onAudioSelected: (String?) -> Unit,
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var isPrepared by remember { mutableStateOf(false) }
    val mediaPlayer = remember { MediaPlayer() }

    DisposableEffect(audioUri) {
        isPrepared = false
        isPlaying = false
        if (audioUri != null) {
            try {
                mediaPlayer.reset()
                mediaPlayer.setDataSource(context, Uri.parse(audioUri))
                mediaPlayer.setOnPreparedListener { isPrepared = true }
                mediaPlayer.setOnCompletionListener { isPlaying = false }
                mediaPlayer.prepareAsync()
            } catch (_: Exception) { }
        } else {
            try { mediaPlayer.reset() } catch (_: Exception) { }
        }
        onDispose {
            try { mediaPlayer.stop() } catch (_: Exception) { }
            try { mediaPlayer.release() } catch (_: Exception) { }
        }
    }

    val pickLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            onAudioSelected(it.toString())
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Аудио к привычке", style = MaterialTheme.typography.titleMedium)

        if (audioUri != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = audioFileName(context, Uri.parse(audioUri)),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    // Play / Pause
                    IconButton(
                        onClick = {
                            if (!isPrepared) return@IconButton
                            if (isPlaying) { mediaPlayer.pause(); isPlaying = false }
                            else { mediaPlayer.start(); isPlaying = true }
                        },
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Пауза" else "Воспроизвести",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    // Delete
                    IconButton(onClick = { onAudioSelected(null) }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Удалить аудио",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        } else {
            OutlinedButton(
                onClick = { pickLauncher.launch("audio/*") },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.MusicNote, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Выбрать аудиофайл")
            }
        }
    }
}

@Composable
fun PeriodGoalSection(
    periodGoalType: PeriodGoalType,
    periodGoalCount: Int,
    onTypeChange: (PeriodGoalType) -> Unit,
    onCountChange: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Цель на период", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PeriodGoalType.entries.forEach { type ->
                FilterChip(
                    selected = periodGoalType == type,
                    onClick = { onTypeChange(type) },
                    label = { Text(type.label) },
                )
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

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

private fun audioFileName(context: android.content.Context, uri: Uri): String = try {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        cursor.getString(idx)
    } ?: uri.lastPathSegment ?: "Аудиофайл"
} catch (_: Exception) { uri.lastPathSegment ?: "Аудиофайл" }

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

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Таймер привычки", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "При запуске таймер отсчитает время и автоматически отметит привычку выполненной",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        // Пресеты + чип «Своё» в горизонтальном скролле
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
        // Поле ввода произвольного времени
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
fun RecurringReminderSection(
    recurringEnabled: Boolean,
    startHour: Int,
    endHour: Int,
    intervalHours: Int,
    onToggle: () -> Unit,
    onStartHourChange: (Int) -> Unit,
    onEndHourChange: (Int) -> Unit,
    onIntervalChange: (Int) -> Unit,
) {
    val intervals = listOf(1, 2, 3, 4, 6)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Повторяющееся напоминание", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Напоминать несколько раз в день через равные интервалы",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = recurringEnabled, onCheckedChange = { onToggle() })
        }

        if (recurringEnabled) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "С %02d:00".format(startHour),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Slider(
                        value = startHour.toFloat(),
                        onValueChange = { onStartHourChange(it.toInt()) },
                        valueRange = 0f..22f,
                        steps = 21,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "До %02d:00".format(endHour),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Slider(
                        value = endHour.toFloat(),
                        onValueChange = { onEndHourChange(it.toInt()) },
                        valueRange = 1f..23f,
                        steps = 21,
                    )
                }
            }

            Text(
                "Интервал (часов):",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                intervals.forEach { h ->
                    FilterChip(
                        selected = intervalHours == h,
                        onClick = { onIntervalChange(h) },
                        label = { Text("${h}ч", style = MaterialTheme.typography.labelSmall) },
                    )
                }
            }

            val times = buildList {
                var h = startHour
                while (h <= endHour) {
                    add("%02d:00".format(h))
                    h += intervalHours
                }
            }
            if (times.isNotEmpty()) {
                Text(
                    text = "Напоминания: ${times.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
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
    allHabits: List<com.flowbit.app.domain.model.Habit>,
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
