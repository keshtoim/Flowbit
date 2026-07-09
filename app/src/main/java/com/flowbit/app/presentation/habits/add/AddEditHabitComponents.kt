package com.flowbit.app.presentation.habits.add

import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.flowbit.app.domain.model.HabitColor
import com.flowbit.app.domain.model.HabitFrequency
import com.flowbit.app.domain.model.HabitReminder
import com.flowbit.app.domain.model.HabitTag
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
    onColorSelected: (HabitColor) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Цвет", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HabitColor.entries.forEach { color ->
                val isSelected = color == selectedColor
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(color.hex)))
                        .then(
                            if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                            else Modifier
                        )
                        .clickable { onColorSelected(color) },
                )
            }
        }
    }
}

@Composable
fun TargetCountSection(
    targetCount: Int,
    onTargetCountChange: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Количество раз в день", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onTargetCountChange(targetCount - 1) }) {
                Icon(Icons.Default.Remove, "Уменьшить")
            }
            Text(
                text = targetCount.toString(),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            IconButton(onClick = { onTargetCountChange(targetCount + 1) }) {
                Icon(Icons.Default.Add, "Увеличить")
            }
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
            mediaPlayer.reset()
        }
        onDispose {
            try { if (mediaPlayer.isPlaying) mediaPlayer.stop() } catch (_: Exception) { }
            mediaPlayer.reset()
        }
    }

    DisposableEffect(Unit) {
        onDispose { mediaPlayer.release() }
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
fun TagSection(
    tags: List<HabitTag>,
    selectedTagId: Long?,
    onTagSelected: (Long?) -> Unit,
    onCreateTag: (name: String, colorHex: String) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }
    var newTagColor by remember { mutableStateOf("#4A90E2") }

    val tagColors = listOf(
        "#4A90E2", "#2ECC71", "#E74C3C", "#9B59B6",
        "#E67E22", "#00E5C0", "#FF69B4", "#F1C40F",
    )

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

    SectionCard(title = "Тег") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // «Без тега»
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
                                Box(
                                    modifier = Modifier.size(10.dp).clip(CircleShape).background(color)
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
}

private fun audioFileName(context: android.content.Context, uri: Uri): String = try {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        cursor.getString(idx)
    } ?: uri.lastPathSegment ?: "Аудиофайл"
} catch (_: Exception) { uri.lastPathSegment ?: "Аудиофайл" }
