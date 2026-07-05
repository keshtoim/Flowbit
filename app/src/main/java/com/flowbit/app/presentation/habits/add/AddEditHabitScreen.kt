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

    LaunchedEffect(habitId) {
        viewModel.loadHabit(habitId)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onBack()
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
                    TextButton(onClick = viewModel::save) {
                        Text("Сохранить")
                    }
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
                onColorSelected = viewModel::onColorChange,
            )

            TargetCountSection(
                targetCount = uiState.targetCount,
                onTargetCountChange = viewModel::onTargetCountChange,
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

            PhotoSection(
                photoUri = uiState.photoUri,
                onPhotoSelected = viewModel::onPhotoSelected,
            )
        }
    }
}
