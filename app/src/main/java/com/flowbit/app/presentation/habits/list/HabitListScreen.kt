package com.flowbit.app.presentation.habits.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flowbit.app.R
import com.flowbit.app.domain.model.GroupingMode
import com.flowbit.app.domain.model.HabitFrequency
import com.flowbit.app.domain.usecase.habit.HabitForDate
import com.flowbit.app.presentation.habits.components.HabitCard
import com.flowbit.app.presentation.habits.components.WeekDatePicker
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.time.LocalDate
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HabitListScreen(
    onAddHabit: () -> Unit,
    onHabitClick: (Long) -> Unit,
    onStatisticsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HabitListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val allTags by viewModel.allTags.collectAsState()
    var groupMenuExpanded by remember { mutableStateOf(false) }

    // Drag-and-drop state
    var draggableHabits by remember { mutableStateOf(uiState.habits) }
    var isDragging by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.habits) {
        if (!isDragging) draggableHabits = uiState.habits
    }
    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        draggableHabits = draggableHabits.toMutableList().apply { add(to.index, removeAt(from.index)) }
    }

    // Диалог таймера
    uiState.timerHabitId?.let { timerHabitId ->
        val habitName = uiState.habits.find { it.habit.id == timerHabitId }?.habit?.name ?: ""
        TimerDialog(
            habitName = habitName,
            remainingSeconds = uiState.timerRemaining,
            onStop = viewModel::stopTimer,
        )
    }

    // Диалог подтверждения отмены пропуска
    if (uiState.unSkipRequestId != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissUnSkip,
            title = { Text(stringResource(R.string.cancel_skip_title)) },
            text = { Text(stringResource(R.string.cancel_skip_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::confirmUnSkip) {
                    Text(stringResource(R.string.cancel_skip_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissUnSkip) { Text(stringResource(R.string.no)) }
            },
        )
    }

    // Диалог подбадривания
    uiState.motivationQuote?.let { quote ->
        AlertDialog(
            onDismissRequest = viewModel::dismissMotivation,
            title = { Text(stringResource(R.string.motivation_dialog_title)) },
            text = {
                Text(
                    text = "\"$quote\"",
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::dismissMotivation) {
                    Text(stringResource(R.string.motivation_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissMotivation) {
                    Text(stringResource(R.string.motivation_skip))
                }
            },
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Flowbit",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    IconButton(onClick = onStatisticsClick) {
                        Icon(Icons.Default.BarChart, contentDescription = "Статистика", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Box {
                        IconButton(onClick = { groupMenuExpanded = true }) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Группировка",
                                tint = if (uiState.groupingMode != GroupingMode.NONE)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        DropdownMenu(
                            expanded = groupMenuExpanded,
                            onDismissRequest = { groupMenuExpanded = false },
                        ) {
                            GroupingMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            mode.label,
                                            fontWeight = if (uiState.groupingMode == mode) FontWeight.Bold else FontWeight.Normal,
                                            color = if (uiState.groupingMode == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        )
                                    },
                                    onClick = {
                                        viewModel.setGroupingMode(mode)
                                        groupMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Настройки", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddHabit,
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Добавить привычку",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp),
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .swipeToDayGesture(
                    selectedDate = uiState.selectedDate,
                    onPreviousDay = { viewModel.onDateSelected(uiState.selectedDate.minusDays(1)) },
                    onNextDay = { viewModel.onDateSelected(uiState.selectedDate.plusDays(1)) },
                ),
        ) {
            WeekDatePicker(
                selectedDate = uiState.selectedDate,
                onDateSelected = viewModel::onDateSelected,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            TodaySummaryCard(
                habits = uiState.habits,
                selectedDate = uiState.selectedDate,
                weekDelta = uiState.weekDelta,
                modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 4.dp),
            )

            AnimatedVisibility(
                visible = uiState.habits.isEmpty(),
                enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 },
                exit = fadeOut(tween(200)),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(text = "🌱", style = MaterialTheme.typography.displayMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.no_habits_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = stringResource(R.string.no_habits_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = uiState.habits.isNotEmpty(),
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(200)),
            ) {
                val isGrouped = uiState.groupingMode != GroupingMode.NONE
                val strDaily = stringResource(R.string.group_daily)
                val strScheduled = stringResource(R.string.group_scheduled)
                val strNotDone = stringResource(R.string.group_not_done)
                val strDone = stringResource(R.string.group_done)
                val strNoTag = stringResource(R.string.group_no_tag)
                val strUnknownTag = stringResource(R.string.group_unknown_tag)
                val groups = remember(uiState.habits, uiState.groupingMode, allTags) {
                    groupHabits(
                        uiState.habits,
                        uiState.groupingMode,
                        allTags.associateBy { it.id },
                        strDaily, strScheduled, strNotDone, strDone, strNoTag, strUnknownTag,
                    )
                }

                LazyColumn(
                    state = lazyListState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (isGrouped) {
                        groups.forEach { (groupName, habits) ->
                            if (groupName != null) {
                                stickyHeader(key = "header_$groupName") {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.background,
                                    ) {
                                        Text(
                                            text = groupName,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(vertical = 6.dp),
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                            }
                            items(habits, key = { it.habit.id }) { habitForDate ->
                                SwipeableHabitCard(
                                    habitForDate = habitForDate,
                                    onToggle = { viewModel.toggleHabit(habitForDate.habit.id) },
                                    onDecrease = { viewModel.decreaseHabit(habitForDate.habit.id) },
                                    onGiveUp = { viewModel.showMotivation() },
                                    onClick = { onHabitClick(habitForDate.habit.id) },
                                    onSkip = { viewModel.skipHabit(habitForDate.habit.id) },
                                    onUnSkipRequest = { viewModel.requestUnSkip(habitForDate.habit.id) },
                                    onTimer = { viewModel.startTimer(habitForDate.habit.id) },
                                )
                            }
                        }
                    } else {
                        items(draggableHabits, key = { it.habit.id }) { habitForDate ->
                            ReorderableItem(reorderState, key = habitForDate.habit.id) { _ ->
                                SwipeableHabitCard(
                                    habitForDate = habitForDate,
                                    onToggle = { viewModel.toggleHabit(habitForDate.habit.id) },
                                    onDecrease = { viewModel.decreaseHabit(habitForDate.habit.id) },
                                    onGiveUp = { viewModel.showMotivation() },
                                    onClick = { onHabitClick(habitForDate.habit.id) },
                                    onSkip = { viewModel.skipHabit(habitForDate.habit.id) },
                                    onUnSkipRequest = { viewModel.requestUnSkip(habitForDate.habit.id) },
                                    onTimer = { viewModel.startTimer(habitForDate.habit.id) },
                                    cardModifier = Modifier.longPressDraggableHandle(
                                        onDragStarted = { isDragging = true },
                                        onDragStopped = {
                                            isDragging = false
                                            viewModel.persistReorder(draggableHabits)
                                        },
                                    ),
                                )
                            }
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
    if (uiState.showConfetti) {
        ConfettiOverlay(onDone = viewModel::hideConfetti)
    }
    } // end Box
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableHabitCard(
    habitForDate: HabitForDate,
    onToggle: () -> Unit,
    onDecrease: () -> Unit,
    onGiveUp: () -> Unit,
    onClick: () -> Unit,
    onSkip: () -> Unit,
    onUnSkipRequest: () -> Unit,
    onTimer: () -> Unit,
    cardModifier: Modifier = Modifier,
) {
    val swipeState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd) onToggle()
            false  // always snap back
        },
    )
    SwipeToDismissBox(
        state = swipeState,
        backgroundContent = {
            // progress == 1f when settled — show only while actively swiping StartToEnd
            val alpha = if (swipeState.targetValue == SwipeToDismissBoxValue.StartToEnd)
                swipeState.progress.coerceIn(0f, 1f)
            else 0f
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = Color(0xFF4CAF50).copy(alpha = (alpha * 1.2f).coerceAtMost(1f)),
                        shape = RoundedCornerShape(20.dp),
                    )
                    .padding(start = 24.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = alpha),
                    modifier = Modifier.size(28.dp),
                )
            }
        },
        enableDismissFromEndToStart = false,
        enableDismissFromStartToEnd = true,
    ) {
        HabitCard(
            habitForDate = habitForDate,
            onToggle = onToggle,
            onDecrease = onDecrease,
            onGiveUp = onGiveUp,
            onClick = onClick,
            onSkip = onSkip,
            onUnSkipRequest = onUnSkipRequest,
            onTimer = onTimer,
            modifier = cardModifier,
        )
    }
}

private fun groupHabits(
    habits: List<HabitForDate>,
    mode: GroupingMode,
    tagsById: Map<Long, com.flowbit.app.domain.model.HabitTag>,
    strDaily: String,
    strScheduled: String,
    strNotDone: String,
    strDone: String,
    strNoTag: String,
    strUnknownTag: String,
): List<Pair<String?, List<HabitForDate>>> = when (mode) {
    GroupingMode.NONE -> listOf(null to habits)
    GroupingMode.BY_TAG -> {
        val withTag = habits.filter { it.habit.tagId != null }
            .groupBy { tagsById[it.habit.tagId]?.name ?: strUnknownTag }
            .map { (name, list) -> name to list }
            .sortedBy { it.first }
        val withoutTag = habits.filter { it.habit.tagId == null }
        if (withoutTag.isNotEmpty()) withTag + (strNoTag to withoutTag)
        else withTag
    }
    GroupingMode.BY_FREQUENCY -> {
        val daily = habits.filter { it.habit.frequency == HabitFrequency.DAILY }
        val scheduled = habits.filter { it.habit.frequency != HabitFrequency.DAILY }
        buildList {
            if (daily.isNotEmpty()) add(strDaily to daily)
            if (scheduled.isNotEmpty()) add(strScheduled to scheduled)
        }
    }
    GroupingMode.BY_STATUS -> {
        val done = habits.filter { (it.entry?.completedCount ?: 0) >= it.habit.targetCount }
        val notDone = habits.filter { (it.entry?.completedCount ?: 0) < it.habit.targetCount }
        buildList {
            if (notDone.isNotEmpty()) add(strNotDone to notDone)
            if (done.isNotEmpty()) add(strDone to done)
        }
    }
}

// Распознаёт горизонтальный свайп, не мешая вертикальной прокрутке LazyColumn
private fun Modifier.swipeToDayGesture(
    selectedDate: LocalDate,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
): Modifier = this.pointerInput(selectedDate) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        var totalX = 0f
        var totalY = 0f
        var directionDecided = false
        var isHorizontal = false

        do {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull() ?: break
            totalX += change.positionChange().x
            totalY += change.positionChange().y

            if (!directionDecided && (abs(totalX) > 15f || abs(totalY) > 15f)) {
                directionDecided = true
                isHorizontal = abs(totalX) > abs(totalY) * 1.5f
            }
            if (isHorizontal) change.consume()
        } while (event.changes.any { it.pressed })

        if (isHorizontal) {
            when {
                totalX > 80f -> onPreviousDay()
                totalX < -80f -> onNextDay()
            }
        }
    }
}
