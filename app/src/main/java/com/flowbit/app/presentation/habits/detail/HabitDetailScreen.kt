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
import androidx.compose.material.icons.filled.Delete
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

    // Диалог удаления
    if (uiState.deleteConfirmOpen) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteConfirm,
            title = { Text("Удалить привычку?") },
            text = { Text("Все данные и история будут удалены безвозвратно.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmDelete(onBack) },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteConfirm) { Text("Отмена") }
            },
        )
    }

    // Диалог подтверждения отмены пропуска
    if (uiState.unSkipConfirmOpen) {
        AlertDialog(
            onDismissRequest = viewModel::dismissUnSkip,
            title = { Text("Отменить пропуск?") },
            text = { Text("Привычка снова будет считаться невыполненной.") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmUnSkip) { Text("Да, отменить") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissUnSkip) { Text("Нет") }
            },
        )
    }

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
                    IconButton(onClick = viewModel::openDeleteConfirm) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Удалить",
                            tint = MaterialTheme.colorScheme.error,
                        )
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

                // Блок пропуска на сегодня
                item {
                    if (uiState.isTodaySkipped) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            ),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "⏭ Сегодня пропущено",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f),
                                )
                                TextButton(onClick = viewModel::requestUnSkip) {
                                    Text("Отменить", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    } else {
                        androidx.compose.material3.OutlinedButton(
                            onClick = viewModel::skipToday,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Text("⏭ Пропустить сегодня")
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

                // История заметок
                if (uiState.noteHistory.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "📝 История заметок",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                uiState.noteHistory.take(10).forEach { (date, note) ->
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                        Text(
                                            text = note,
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        androidx.compose.material3.HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        )
                                    }
                                }
                                if (uiState.noteHistory.size > 10) {
                                    Text(
                                        text = "… ещё ${uiState.noteHistory.size - 10} заметок",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
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

                // Тепловая карта года (GitHub-style)
                item { YearHeatmapCard(stats.completedDates) }

                // Weekly pattern
                item { WeeklyPatternCard(stats.completedDates) }
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
            mediaPlayer.setOnPreparedListener { isPrepared = true }
            mediaPlayer.setOnCompletionListener { isPlaying = false }
            mediaPlayer.prepareAsync()
        } catch (_: Exception) {
            isPrepared = false
        }
        onDispose {
            try { mediaPlayer.stop() } catch (_: Exception) { }
            try { mediaPlayer.release() } catch (_: Exception) { }
        }
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
private fun YearHeatmapCard(completedDates: List<LocalDate>) {
    if (completedDates.isEmpty()) return
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Год активности",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(12.dp))
            val completedSet = remember(completedDates) { completedDates.toHashSet() }
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp),
            ) {
                val today = LocalDate.now()
                val cols = 53
                val rows = 7
                val gap = 2.dp.toPx()
                val cellW = (size.width - gap * (cols - 1)) / cols
                val cellH = (size.height - gap * (rows - 1)) / rows
                val radius = minOf(cellW, cellH) * 0.28f
                val startDate = today
                    .minusWeeks(52)
                    .with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

                for (col in 0 until cols) {
                    for (row in 0 until rows) {
                        val date = startDate.plusDays((col * 7 + row).toLong())
                        if (date.isAfter(today)) continue
                        val isDone = date in completedSet
                        val x = col * (cellW + gap)
                        val y = row * (cellH + gap)
                        drawRoundRect(
                            color = if (isDone) primaryColor else surfaceVariantColor,
                            topLeft = Offset(x, y),
                            size = Size(cellW, cellH),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyPatternCard(completedDates: List<LocalDate>) {
    if (completedDates.isEmpty()) return
    val dayLabels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val counts = (1..7).map { dow ->
        completedDates.count { it.dayOfWeek == DayOfWeek.of(dow) }
    }
    val maxCount = counts.maxOrNull()?.takeIf { it > 0 } ?: return
    val bestIndex = counts.indexOf(maxCount)

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

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
                    text = "Паттерн по дням",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Лучший: ${dayLabels[bestIndex]}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(16.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
            ) {
                val gap = 8.dp.toPx()
                val barWidth = (size.width - gap * 6) / 7f
                counts.forEachIndexed { i, count ->
                    val x = i * (barWidth + gap)
                    val barH = (count.toFloat() / maxCount) * size.height
                    drawRoundRect(
                        color = surfaceVariantColor,
                        topLeft = Offset(x, 0f),
                        size = Size(barWidth, size.height),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
                    )
                    if (barH > 0f) {
                        drawRoundRect(
                            color = if (i == bestIndex) primaryColor else primaryColor.copy(alpha = 0.45f),
                            topLeft = Offset(x, size.height - barH),
                            size = Size(barWidth, barH),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                dayLabels.forEachIndexed { i, label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (i == bestIndex) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (i == bestIndex) FontWeight.Bold else FontWeight.Normal,
                    )
                }
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

    val monthFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru"))
    val dayNames = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

    // Статистика текущего отображаемого месяца
    val monthStats = remember(displayMonth, completedSet) {
        val daysInMonth = displayMonth.lengthOfMonth()
        val lastDay = minOf(daysInMonth, if (displayMonth == YearMonth.now()) today.dayOfMonth else daysInMonth)
        val doneDays = (1..lastDay).count { displayMonth.atDay(it) in completedSet }
        Triple(doneDays, lastDay, if (lastDay > 0) doneDays * 100 / lastDay else 0)
    }
    val (doneDays, passedDays, pct) = monthStats

    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    Column(modifier = modifier) {
        // ── Навигация по месяцу ───────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { displayMonth = displayMonth.minusMonths(1) },
                modifier = Modifier.size(36.dp),
            ) {
                Icon(Icons.Default.ChevronLeft, "Предыдущий месяц", modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = displayMonth.format(monthFormatter).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                // Строка статистики месяца
                Text(
                    text = "$doneDays из $passedDays дн. • $pct%",
                    style = MaterialTheme.typography.labelSmall,
                    color = primaryColor,
                )
            }
            IconButton(
                onClick = { displayMonth = displayMonth.plusMonths(1) },
                modifier = Modifier.size(36.dp),
            ) {
                Icon(Icons.Default.ChevronRight, "Следующий месяц", modifier = Modifier.size(20.dp))
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Заголовки дней недели ─────────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth()) {
            dayNames.forEachIndexed { i, name ->
                val isWeekend = i >= 5
                Text(
                    text = name,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isWeekend)
                        MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        // ── Сетка дней ───────────────────────────────────────────────────
        val firstDay = displayMonth.atDay(1)
        val offset = firstDay.dayOfWeek.value - 1   // Пн=0 … Вс=6
        val daysInMonth = displayMonth.lengthOfMonth()
        val weeks = (offset + daysInMonth + 6) / 7

        repeat(weeks) { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val dayNum = week * 7 + col - offset + 1
                    if (dayNum < 1 || dayNum > daysInMonth) {
                        Box(modifier = Modifier.weight(1f).height(44.dp))
                    } else {
                        val date = displayMonth.atDay(dayNum)
                        val isToday = date == today
                        val isFuture = date.isAfter(today)
                        val isCompleted = date in completedSet
                        val isWeekend = col >= 5

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .then(when {
                                        isCompleted -> Modifier.background(primaryColor)
                                        isToday -> Modifier.border(2.dp, primaryColor, CircleShape)
                                        else -> Modifier
                                    }),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = dayNum.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = when {
                                        isCompleted || isToday -> FontWeight.Bold
                                        else -> FontWeight.Normal
                                    },
                                    color = when {
                                        isCompleted -> onPrimaryColor
                                        isFuture -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                        isToday -> primaryColor
                                        isWeekend -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
