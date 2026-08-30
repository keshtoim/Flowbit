package com.flowbit.app.presentation.habits.add

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHabitScreen(
    habitId: Long?,
    onBack: () -> Unit,
    viewModel: AddEditHabitViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val allTags by viewModel.allTags.collectAsState()
    var showTemplatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(habitId) { viewModel.loadHabit(habitId) }
    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) onBack() }

    if (showTemplatePicker) {
        TemplatePickerDialog(
            onDismiss = { showTemplatePicker = false },
            onSelect = { viewModel.applyTemplate(it) },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (habitId == null) "Новая привычка" else "Редактировать") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (habitId == null) {
                        TextButton(onClick = { showTemplatePicker = true }) { Text("Шаблон") }
                    }
                    TextButton(onClick = viewModel::save) { Text("Сохранить") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EmojiAndNameSection(
                name = uiState.name,
                emoji = uiState.emoji,
                onNameChange = viewModel::onNameChange,
                onEmojiChange = viewModel::onEmojiChange,
            )
            ColorPickerSection(
                selectedColor = uiState.color,
                customColorHex = uiState.customColorHex,
                onColorSelected = viewModel::onColorChange,
                onCustomColorHexChange = viewModel::onCustomColorHexChange,
            )
            TargetCountSection(
                targetCount = uiState.targetCount,
                onTargetCountChange = viewModel::onTargetCountChange,
                unit = uiState.unit,
                onUnitChange = viewModel::onUnitChange,
            )
            FrequencySection(
                frequency = uiState.frequency,
                scheduledDays = uiState.scheduledDays,
                onFrequencyChange = viewModel::onFrequencyChange,
                onDayToggle = viewModel::onDayToggle,
            )
            StartDateSection(
                startDate = uiState.startDate,
                onStartDateChange = viewModel::onStartDateChange,
            )
            WidgetSection(
                showInWidget = uiState.showInWidget,
                onShowInWidgetChange = viewModel::onShowInWidgetChange,
            )
            RemindersSection(
                reminders = uiState.reminders,
                onAddReminder = viewModel::onAddReminder,
                onRemoveReminder = viewModel::onRemoveReminder,
                onToggleReminder = viewModel::onToggleReminder,
            )
            RecurringReminderSection(
                recurringEnabled = uiState.recurringEnabled,
                startHour = uiState.recurringStartHour,
                endHour = uiState.recurringEndHour,
                intervalHours = uiState.recurringIntervalHours,
                onToggle = viewModel::onRecurringToggle,
                onStartHourChange = viewModel::onRecurringStartHour,
                onEndHourChange = viewModel::onRecurringEndHour,
                onIntervalChange = viewModel::onRecurringInterval,
            )
            PhotoSection(
                photoUri = uiState.photoUri,
                isPhotoHidden = uiState.isPhotoHidden,
                onPhotoSelected = viewModel::onPhotoSelected,
                onIsPhotoHiddenChange = viewModel::onIsPhotoHiddenChange,
            )
            AudioSection(
                audioUri = uiState.audioUri,
                onAudioSelected = viewModel::onAudioSelected,
            )
            PeriodGoalSection(
                periodGoalType = uiState.periodGoalType,
                periodGoalCount = uiState.periodGoalCount,
                onTypeChange = viewModel::onPeriodGoalTypeChange,
                onCountChange = viewModel::onPeriodGoalCountChange,
            )
            TimerSection(
                timerSeconds = uiState.timerSeconds,
                onTimerSecondsChange = viewModel::onTimerSecondsChange,
            )
            TagSection(
                tags = allTags,
                selectedTagId = uiState.tagId,
                onTagSelected = viewModel::onTagSelected,
                onCreateTag = viewModel::createTag,
                onDeleteTag = viewModel::deleteTag,
            )
            TabooSection(
                isBadHabit = uiState.isBadHabit,
                onIsBadHabitChange = viewModel::onIsBadHabitChange,
            )
            StreakSkipSection(
                allowStreakSkip = uiState.allowStreakSkip,
                onAllowStreakSkipChange = viewModel::onAllowStreakSkipChange,
            )
            StackingSection(
                allHabits = uiState.allHabits,
                stackAfterHabitId = uiState.stackAfterHabitId,
                currentHabitId = habitId,
                onStackAfterChange = viewModel::onStackAfterHabitChange,
            )
        }
    }
}
