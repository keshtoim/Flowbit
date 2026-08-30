package com.flowbit.app.presentation.habits.detail

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

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

    // ── Диалог удаления ───────────────────────────────────────────────────────
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

    // ── Диалог подтверждения отмены пропуска ─────────────────────────────────
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

    // ── Диалог заметки ────────────────────────────────────────────────────────
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

        // ── Состояние загрузки ────────────────────────────────────────────────
        if (stats == null) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Фото-баннер ───────────────────────────────────────────────────
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

            // ── Аудиоплеер ────────────────────────────────────────────────────
            if (stats.audioUri != null) {
                item { DetailAudioPlayer(audioUri = stats.audioUri) }
            }

            // ── Заголовок (эмодзи + название) ────────────────────────────────
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = stats.habitEmoji, style = MaterialTheme.typography.headlineSmall)
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

            // ── Пропуск дня ───────────────────────────────────────────────────
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

            // ── Щит серии (заморозка) ─────────────────────────────────────────
            // Показываем если: серия > 0, сегодня не заморожено, лимит в неделю не исчерпан
            val canFreeze = stats.currentStreak > 0
                && !uiState.isFrozenToday
                && !uiState.isTodaySkipped
                && uiState.freezeCountThisWeek == 0
            if (canFreeze || uiState.isFrozenToday) {
                item {
                    if (uiState.isFrozenToday) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            ),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "🛡 Серия заморожена на сегодня",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    } else {
                        androidx.compose.material3.OutlinedButton(
                            onClick = viewModel::freezeStreak,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Text("🛡 Заморозить серию (${stats.currentStreak} дней)")
                        }
                    }
                }
            }

            // ── Календарь ─────────────────────────────────────────────────────
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

            // ── Заметка на сегодня ────────────────────────────────────────────
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
                                Text(text = note, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        TextButton(onClick = viewModel::openNoteDialog) {
                            Text(if (uiState.todayNote == null) "Добавить" else "Изменить")
                        }
                    }
                }
            }

            // ── История заметок ───────────────────────────────────────────────
            item { NoteHistoryCard(notes = uiState.noteHistory) }

            // ── Гистограмма активности за 30 дней ────────────────────────────
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

            // ── Текущая серия (большая карточка с анимацией огня) ─────────────
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
                        // Пульсация активируется при серии ≥ 3 дней
                        val fireScale by rememberInfiniteTransition(label = "fire").animateFloat(
                            initialValue = 1f,
                            targetValue = if (stats.currentStreak >= 3) 1.18f else 1f,
                            animationSpec = infiniteRepeatable(
                                tween(900),
                                repeatMode = RepeatMode.Reverse,
                            ),
                            label = "fireScale",
                        )
                        Text(
                            text = "🔥",
                            style = MaterialTheme.typography.displaySmall,
                            modifier = Modifier.scale(fireScale),
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

            // ── Лучшая серия + всего выполнено (два блока рядом) ─────────────
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(text = "🚀", style = MaterialTheme.typography.titleLarge)
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
                        Column(modifier = Modifier.padding(20.dp)) {
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

            // ── Частота выполнения (прогресс-бар) ────────────────────────────
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

            // ── Анализ по периодам ────────────────────────────────────────────
            item { PeriodAnalysisCard(completedDates = stats.completedDates) }

            // ── Тепловая карта года ───────────────────────────────────────────
            item { YearHeatmapCard(completedDates = stats.completedDates) }

            // ── Паттерн по дням недели ────────────────────────────────────────
            item { WeeklyPatternCard(completedDates = stats.completedDates) }

            // ── Паттерн по часам суток ────────────────────────────────────────
            item { HourlyPatternCard(hourlyCompletions = uiState.hourlyCompletions) }

            // ── Прогноз серии ─────────────────────────────────────────────────
            item {
                StreakForecastCard(
                    currentStreak = stats.currentStreak,
                    longestStreak = stats.longestStreak,
                    completionRate = stats.completionRate,
                )
            }
        }
    }
}
