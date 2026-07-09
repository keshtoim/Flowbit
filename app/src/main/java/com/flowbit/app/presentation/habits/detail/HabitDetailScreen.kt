package com.flowbit.app.presentation.habits.detail

import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Pause
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habitId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    viewModel: HabitDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(habitId) { viewModel.load(habitId) }

    if (uiState.noteDialogOpen) {
        AlertDialog(
            onDismissRequest = viewModel::dismissNoteDialog,
            title = { Text("Заметка на сегодня") },
            text = {
                OutlinedTextField(
                    value = uiState.noteInput,
                    onValueChange = viewModel::onNoteInputChange,
                    placeholder = { Text("Как прошло?") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5,
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::saveNote) { Text("Сохранить") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissNoteDialog) { Text("Отмена") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.stats?.habitName ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, "Редактировать")
                    }
                },
            )
        },
    ) { padding ->
        val stats = uiState.stats
        if (stats == null) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Фото-баннер (всегда виден в детальном экране, не зависит от isPhotoHidden)
                if (stats.photoUri != null) {
                    item {
                        AsyncImage(
                            model = stats.photoUri,
                            contentDescription = "Фото привычки",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(20.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }

                // Аудиоплеер
                if (stats.audioUri != null) {
                    item {
                        DetailAudioPlayer(audioUri = stats.audioUri)
                    }
                }

                // Header
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stats.habitEmoji,
                                style = MaterialTheme.typography.headlineSmall,
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = stats.habitName,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "${stats.totalCompletions} выполнений",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                // Calendar
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        HabitCalendar(
                            completedDates = stats.completedDates,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }

                // Заметка на сегодня
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.NoteAdd,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp),
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Заметка на сегодня",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                uiState.todayNote?.let { note ->
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = note,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                            TextButton(onClick = viewModel::openNoteDialog) {
                                Text(if (uiState.todayNote == null) "Добавить" else "Изменить")
                            }
                        }
                    }
                }

                // График прогресса за 30 дней
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Активность за 30 дней",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.height(12.dp))
                            ProgressBarChart(
                                completedDates = stats.completedDates,
                                modifier = Modifier.fillMaxWidth().height(80.dp),
                            )
                        }
                    }
                }

                // Current streak — big card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "🔥",
                                style = MaterialTheme.typography.displaySmall,
                            )
                            Spacer(Modifier.width(20.dp))
                            Column {
                                Text(
                                    text = "${stats.currentStreak} Дн.",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                )
                                Text(
                                    text = "Текущая серия",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                                )
                            }
                        }
                    }
                }

                // Best streak + total
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                            ) {
                                Text(
                                    text = "🚀",
                                    style = MaterialTheme.typography.titleLarge,
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = "${stats.longestStreak} Дн.",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "Лучшая серия",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp),
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = "${stats.totalCompletions}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "Всего\nвыполнено",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                // Completion rate
                item {
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
                                    text = "Частота выполнения",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    text = "${(stats.completionRate * 100).toInt()}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { stats.completionRate },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressBarChart(
    completedDates: List<LocalDate>,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()
    val days = (29 downTo 0).map { today.minusDays(it.toLong()) }
    val completedSet = completedDates.toHashSet()
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = modifier) {
        val barCount = days.size
        val gap = 2.dp.toPx()
        val barWidth = (size.width - gap * (barCount - 1)) / barCount
        val maxH = size.height

        days.forEachIndexed { i, date ->
            val x = i * (barWidth + gap)
            val isCompleted = date in completedSet
            val barH = if (isCompleted) maxH else maxH * 0.15f
            drawRoundRect(
                color = if (isCompleted) primaryColor else surfaceVariant,
                topLeft = Offset(x, maxH - barH),
                size = Size(barWidth, barH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()),
            )
        }
    }
}

@Composable
private fun DetailAudioPlayer(audioUri: String) {
    val context = LocalContext.current
    val mediaPlayer = remember { MediaPlayer() }
    var isPlaying by remember { mutableStateOf(false) }
    var isPrepared by remember { mutableStateOf(false) }

    DisposableEffect(audioUri) {
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(context, Uri.parse(audioUri))
            mediaPlayer.prepare()
            isPrepared = true
        } catch (e: Exception) {
            isPrepared = false
        }
        mediaPlayer.setOnCompletionListener { isPlaying = false }
        onDispose {
            mediaPlayer.stop()
            isPlaying = false
        }
    }

    DisposableEffect(Unit) {
        onDispose { mediaPlayer.release() }
    }

    val fileName = remember(audioUri) {
        try {
            val cursor = context.contentResolver.query(
                Uri.parse(audioUri),
                arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),
                null, null, null,
            )
            cursor?.use { c ->
                if (c.moveToFirst()) c.getString(0) else null
            }
        } catch (e: Exception) { null }
    } ?: audioUri.substringAfterLast('/')

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = fileName,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (!isPrepared) return@IconButton
                    if (isPlaying) {
                        mediaPlayer.pause()
                        isPlaying = false
                    } else {
                        mediaPlayer.start()
                        isPlaying = true
                    }
                },
                enabled = isPrepared,
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Пауза" else "Воспроизвести",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun HabitCalendar(
    completedDates: List<LocalDate>,
    modifier: Modifier = Modifier,
) {
    var displayMonth by remember { mutableStateOf(YearMonth.now()) }
    val completedSet = remember(completedDates) { completedDates.toHashSet() }
    val today = LocalDate.now()

    val monthFormatter = DateTimeFormatter.ofPattern("LLLL yyyy 'г.'", Locale("ru"))
    val dayNames = listOf("пн", "вт", "ср", "чт", "пт", "сб", "вс")

    Column(modifier = modifier) {
        // Month navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = { displayMonth = displayMonth.minusMonths(1) }) {
                Icon(Icons.Default.ChevronLeft, "Предыдущий месяц")
            }
            Text(
                text = displayMonth.format(monthFormatter)
                    .replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            IconButton(onClick = { displayMonth = displayMonth.plusMonths(1) }) {
                Icon(Icons.Default.ChevronRight, "Следующий месяц")
            }
        }

        Spacer(Modifier.height(8.dp))

        // Day of week header
        Row(modifier = Modifier.fillMaxWidth()) {
            dayNames.forEach { name ->
                Text(
                    text = name,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Calendar grid
        val firstDay = displayMonth.atDay(1)
        // Monday=1 … Sunday=7 → offset 0..6
        val offset = (firstDay.dayOfWeek.value - 1)
        val daysInMonth = displayMonth.lengthOfMonth()
        val totalCells = offset + daysInMonth
        val weeks = (totalCells + 6) / 7

        repeat(weeks) { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                repeat(7) { col ->
                    val dayNum = week * 7 + col - offset + 1
                    if (dayNum < 1 || dayNum > daysInMonth) {
                        Box(
                            modifier = Modifier.weight(1f).height(46.dp),
                        )
                    } else {
                        val date = displayMonth.atDay(dayNum)
                        val isToday = date == today
                        val isCompleted = date in completedSet
                        val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY ||
                                date.dayOfWeek == DayOfWeek.SUNDAY

                        Column(
                            modifier = Modifier.weight(1f).height(46.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .then(
                                        if (isToday) Modifier.border(
                                            2.dp,
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape,
                                        ) else Modifier
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = dayNum.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = when {
                                        isToday -> MaterialTheme.colorScheme.primary
                                        isWeekend -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                )
                            }
                            Spacer(Modifier.height(2.dp))
                            if (isCompleted) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                )
                            } else {
                                Box(modifier = Modifier.size(6.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
