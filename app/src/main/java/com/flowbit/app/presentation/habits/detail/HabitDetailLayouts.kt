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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import kotlin.math.roundToInt

// ── Layout A · Dashboard ──────────────────────────────────────────────────────

@Composable
internal fun DetailLayoutA(
    uiState: HabitDetailUiState,
    viewModel: HabitDetailViewModel,
    padding: PaddingValues,
) {
    val stats = uiState.stats ?: return
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val rateInt = (stats.completionRate * 100).roundToInt()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (stats.photoUri != null) item {
            AsyncImage(
                model = stats.photoUri,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop,
            )
        }
        if (stats.audioUri != null) item { DetailAudioPlayer(stats.audioUri) }

        // Hero: процент выполнения + название
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                primary.copy(alpha = 0.22f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                            ),
                        ),
                    )
                    .padding(18.dp),
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(primary.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(stats.habitEmoji, fontSize = 22.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stats.habitName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "${stats.totalCompletions} выполнений всего",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$rateInt",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            color = primary,
                            lineHeight = 48.sp,
                        )
                        Text(
                            text = "%",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = primary,
                            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "выполнение за 30 дней",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                    LinearProgressIndicator(
                        progress = { stats.completionRate },
                        modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                    )
                }
            }
        }

        // 2×2 плитки метрик
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile(
                    modifier = Modifier.weight(1f),
                    icon = "🔥",
                    value = "${stats.currentStreak} дн.",
                    label = "Текущая серия",
                    valueColor = primary,
                )
                MetricTile(
                    modifier = Modifier.weight(1f),
                    icon = "🏆",
                    value = "${stats.longestStreak} дн.",
                    label = "Лучшая серия",
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile(
                    modifier = Modifier.weight(1f),
                    icon = "✅",
                    value = "${stats.totalCompletions}",
                    label = "Всего выполнено",
                    valueColor = MaterialTheme.colorScheme.tertiary,
                )
                val weeklyAvg = if (stats.completedDates.size > 0) {
                    val weeks = (stats.completedDates.size / 7f).coerceAtLeast(1f)
                    "%.1f".format(stats.completedDates.size / weeks)
                } else "0"
                MetricTile(
                    modifier = Modifier.weight(1f),
                    icon = "📅",
                    value = "$weeklyAvg раз",
                    label = "В среднем/неделю",
                )
            }
        }

        // Пропуск / заморозка
        item { SkipFreezeSection(uiState, viewModel) }

        // 30-дневная диаграмма
        item {
            SectionCard(title = "Активность — 30 дней") {
                ProgressBarChart(
                    completedDates = stats.completedDates,
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                )
            }
        }

        // Тепловая карта
        item { YearHeatmapCard(completedDates = stats.completedDates) }

        // Анализ периодов
        item { PeriodAnalysisCard(completedDates = stats.completedDates) }

        // Паттерн по дням недели
        item { WeeklyPatternCard(completedDates = stats.completedDates) }

        // Паттерн по часам
        item { HourlyPatternCard(hourlyCompletions = uiState.hourlyCompletions) }

        // Прогноз серии
        item {
            StreakForecastCard(
                currentStreak = stats.currentStreak,
                longestStreak = stats.longestStreak,
                completionRate = stats.completionRate,
            )
        }

        // Заметка + история
        item { NoteSection(uiState, viewModel) }
        item { NoteHistoryCard(notes = uiState.noteHistory) }
    }
}

// ── Layout B · Timeline ───────────────────────────────────────────────────────

@Composable
internal fun DetailLayoutB(
    uiState: HabitDetailUiState,
    viewModel: HabitDetailViewModel,
    padding: PaddingValues,
) {
    val stats = uiState.stats ?: return
    val primary = MaterialTheme.colorScheme.primary
    val rateInt = (stats.completionRate * 100).roundToInt()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        if (stats.photoUri != null) item {
            AsyncImage(
                model = stats.photoUri,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(180.dp),
                contentScale = ContentScale.Crop,
            )
        }
        if (stats.audioUri != null) item {
            Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                DetailAudioPlayer(stats.audioUri)
            }
        }

        // Шапка — компактная
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            ) {
                Column {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(primary.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(stats.habitEmoji, fontSize = 20.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stats.habitName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "${stats.totalCompletions} выполнений",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    // 4 метрики в ряд
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        CompactMetric(value = "🔥 ${stats.currentStreak}", label = "Серия", valueColor = primary)
                        VerticalDividerThin()
                        CompactMetric(value = "${stats.longestStreak}", label = "Рекорд")
                        VerticalDividerThin()
                        CompactMetric(value = "$rateInt%", label = "30 дней", valueColor = primary)
                        VerticalDividerThin()
                        CompactMetric(value = "${stats.totalCompletions}", label = "Всего")
                    }
                }
            }
        }

        // Пропуск / заморозка
        item {
            Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SkipFreezeSection(uiState, viewModel)
            }
        }

        // Месячный календарь
        item {
            Box(Modifier.padding(horizontal = 16.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                ) {
                    HabitCalendar(completedDates = stats.completedDates, modifier = Modifier.padding(16.dp))
                }
            }
        }

        // Текущая серия + лучшая серия рядом
        item {
            Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Текущая серия с анимацией
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = primary),
                    ) {
                        val fireScale by rememberInfiniteTransition(label = "fire").animateFloat(
                            initialValue = 1f,
                            targetValue = if (stats.currentStreak >= 3) 1.18f else 1f,
                            animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
                            label = "fs",
                        )
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🔥", fontSize = 24.sp, modifier = Modifier.scale(fireScale))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "${stats.currentStreak} дн.",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                            Text(
                                text = "Серия",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(0.8f),
                            )
                        }
                    }
                    // Лучшая серия
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🏆", fontSize = 24.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "${stats.longestStreak} дн.",
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
                }
            }
        }

        // Год
        item {
            Box(Modifier.padding(horizontal = 16.dp)) {
                YearHeatmapCard(completedDates = stats.completedDates)
            }
        }

        item {
            Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                SectionCard(title = "Активность — 30 дней") {
                    ProgressBarChart(
                        completedDates = stats.completedDates,
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                    )
                }
            }
        }
        item {
            Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                PeriodAnalysisCard(completedDates = stats.completedDates)
            }
        }
        item {
            Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                WeeklyPatternCard(completedDates = stats.completedDates)
            }
        }
        item {
            Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                HourlyPatternCard(hourlyCompletions = uiState.hourlyCompletions)
            }
        }
        item {
            Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                StreakForecastCard(
                    currentStreak = stats.currentStreak,
                    longestStreak = stats.longestStreak,
                    completionRate = stats.completionRate,
                )
            }
        }
        item {
            Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                NoteSection(uiState, viewModel)
            }
        }
        item {
            Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                NoteHistoryCard(notes = uiState.noteHistory)
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

// ── Layout C · История (нарративный) ─────────────────────────────────────────

@Composable
internal fun DetailLayoutC(
    uiState: HabitDetailUiState,
    viewModel: HabitDetailViewModel,
    padding: PaddingValues,
) {
    val stats = uiState.stats ?: return
    val primary = MaterialTheme.colorScheme.primary
    val rateInt = (stats.completionRate * 100).roundToInt()

    // Сколько дней этого месяца уже выполнено
    val today = java.time.LocalDate.now()
    val monthStart = today.withDayOfMonth(1)
    val daysInMonth = today.dayOfMonth
    val completedThisMonth = stats.completedDates.count { !it.isBefore(monthStart) && !it.isAfter(today) }

    // Пиковый час из hourlyCompletions
    val peakHour = uiState.hourlyCompletions.maxByOrNull { it.value }?.key

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (stats.photoUri != null) item {
            AsyncImage(
                model = stats.photoUri,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop,
            )
        }
        if (stats.audioUri != null) item { DetailAudioPlayer(stats.audioUri) }

        // Кольцо + название
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                RingProgressIndicator(
                    progress = stats.completionRate,
                    centerLabel = "$rateInt%",
                    subLabel = "30 дней",
                    modifier = Modifier.size(110.dp),
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(primary.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(stats.habitEmoji, fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(stats.habitName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "${stats.totalCompletions} выполнений · с ${stats.completedDates.minOrNull()?.let {
                                "${it.monthValue}.${it.year}"
                            } ?: "?"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // Пропуск / заморозка
        item { SkipFreezeSection(uiState, viewModel) }

        // Нарративный блок — Серия
        item {
            NarrativeBlock(title = "🔥 Серия") {
                Column {
                    Text(
                        text = buildString {
                            append("Сейчас ")
                            append(stats.currentStreak)
                            append(" дней подряд")
                            if (stats.currentStreak >= 3) append(" — так держать! 💪")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiniStatBadge(modifier = Modifier.weight(1f), value = "${stats.currentStreak}", label = "Сейчас", color = primary)
                        MiniStatBadge(modifier = Modifier.weight(1f), value = "${stats.longestStreak} 🏆", label = "Рекорд")
                        MiniStatBadge(modifier = Modifier.weight(1f), value = "$rateInt%", label = "За 30 дн.")
                    }
                }
            }
        }

        // Нарративный блок — Этот месяц (точки)
        item {
            NarrativeBlock(title = "📅 ${monthName(today.monthValue)} ${today.year}") {
                Column {
                    Text(
                        text = "$completedThisMonth из $daysInMonth дней выполнено",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    MonthDotGrid(
                        daysInMonth = daysInMonth,
                        completedDates = stats.completedDates,
                        today = today,
                    )
                }
            }
        }

        // Нарративный блок — Часы
        if (peakHour != null) item {
            NarrativeBlock(title = "⏰ Когда выполняешь") {
                Column {
                    Text(
                        text = "Чаще всего в ${peakHour}:00–${peakHour + 1}:00",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    HourlyPatternCard(hourlyCompletions = uiState.hourlyCompletions)
                }
            }
        }

        // Остальные карты
        item { YearHeatmapCard(completedDates = stats.completedDates) }
        item { WeeklyPatternCard(completedDates = stats.completedDates) }
        item { PeriodAnalysisCard(completedDates = stats.completedDates) }
        item {
            StreakForecastCard(
                currentStreak = stats.currentStreak,
                longestStreak = stats.longestStreak,
                completionRate = stats.completionRate,
            )
        }
        item { NoteSection(uiState, viewModel) }
        item { NoteHistoryCard(notes = uiState.noteHistory) }
    }
}

// ── Общие вспомогательные компоненты ─────────────────────────────────────────

@Composable
private fun MetricTile(
    modifier: Modifier = Modifier,
    icon: String,
    value: String,
    label: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(icon, fontSize = 20.sp)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = valueColor)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CompactMetric(value: String, label: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = valueColor)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun VerticalDividerThin() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(32.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun NarrativeBlock(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp,
            )
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun MiniStatBadge(modifier: Modifier = Modifier, value: String, label: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MonthDotGrid(daysInMonth: Int, completedDates: List<java.time.LocalDate>, today: java.time.LocalDate) {
    val completedSet = completedDates.toHashSet()
    val primary = MaterialTheme.colorScheme.primary
    val surface2 = MaterialTheme.colorScheme.surfaceVariant
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        (1..daysInMonth).chunked(7).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                week.forEach { d ->
                    val date = today.withDayOfMonth(d)
                    val done = date in completedSet
                    val isToday = date == today
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when {
                                    isToday -> primary.copy(alpha = 0.4f)
                                    done -> primary
                                    else -> surface2
                                },
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun RingProgressIndicator(
    progress: Float,
    centerLabel: String,
    subLabel: String,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 10.dp.toPx()
            val inset = stroke / 2
            val oval = androidx.compose.ui.geometry.Rect(inset, inset, size.width - inset, size.height - inset)
            drawArc(
                color = primary.copy(alpha = 0.12f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = oval.topLeft,
                size = oval.size,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
            )
            drawArc(
                brush = Brush.sweepGradient(listOf(primary, secondary)),
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = oval.topLeft,
                size = oval.size,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = stroke,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                ),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(centerLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = primary)
            Text(subLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun NoteSection(uiState: HabitDetailUiState, viewModel: HabitDetailViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.NoteAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Заметка на сегодня", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                uiState.todayNote?.let { Spacer(Modifier.height(2.dp)); Text(it, style = MaterialTheme.typography.bodyMedium) }
            }
            TextButton(onClick = viewModel::openNoteDialog) {
                Text(if (uiState.todayNote == null) "Добавить" else "Изменить")
            }
        }
    }
}

@Composable
private fun SkipFreezeSection(uiState: HabitDetailUiState, viewModel: HabitDetailViewModel) {
    val stats = uiState.stats ?: return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (uiState.isTodaySkipped) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("⏭ Сегодня пропущено", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    TextButton(onClick = viewModel::requestUnSkip) { Text("Отменить", color = MaterialTheme.colorScheme.primary) }
                }
            }
        } else {
            OutlinedButton(onClick = viewModel::skipToday, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Text("⏭ Пропустить сегодня")
            }
        }

        val canFreeze = stats.currentStreak > 0 && !uiState.isFrozenToday && !uiState.isTodaySkipped && uiState.freezeCountThisWeek == 0
        if (canFreeze || uiState.isFrozenToday) {
            if (uiState.isFrozenToday) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                ) {
                    Text(
                        "🛡 Серия заморожена на сегодня",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
            } else {
                OutlinedButton(onClick = viewModel::freezeStreak, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Text("🛡 Заморозить серию (${stats.currentStreak} дней)")
                }
            }
        }
    }
}

private fun monthName(month: Int): String = when (month) {
    1 -> "Январь"; 2 -> "Февраль"; 3 -> "Март"; 4 -> "Апрель"
    5 -> "Май"; 6 -> "Июнь"; 7 -> "Июль"; 8 -> "Август"
    9 -> "Сентябрь"; 10 -> "Октябрь"; 11 -> "Ноябрь"; else -> "Декабрь"
}
