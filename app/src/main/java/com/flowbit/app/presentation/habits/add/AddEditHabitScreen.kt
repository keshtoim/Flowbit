package com.flowbit.app.presentation.habits.add

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    val formStyle by viewModel.formStyle.collectAsState()
    var showTemplatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(habitId) { viewModel.loadHabit(habitId) }
    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) onBack() }

    if (showTemplatePicker) {
        TemplatePickerDialog(
            onDismiss = { showTemplatePicker = false },
            onSelect = { viewModel.applyTemplate(it) },
        )
    }

    // Layout C handles its own bottom Save button; A and B use TopAppBar action
    val isWizard = formStyle == "C"

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
                    if (!isWizard) {
                        TextButton(onClick = viewModel::save) { Text("Сохранить") }
                    }
                },
            )
        },
    ) { padding ->
        when (formStyle) {
            "B" -> FormLayoutB(
                uiState = uiState,
                allTags = allTags,
                viewModel = viewModel,
                paddingValues = padding,
                habitId = habitId,
            )
            "C" -> FormLayoutC(
                uiState = uiState,
                allTags = allTags,
                viewModel = viewModel,
                paddingValues = padding,
                habitId = habitId,
                onSave = viewModel::save,
            )
            else -> FormLayoutA(
                uiState = uiState,
                allTags = allTags,
                viewModel = viewModel,
                paddingValues = padding,
                habitId = habitId,
                showTemplatePicker = showTemplatePicker,
                onShowTemplatePicker = { showTemplatePicker = it },
            )
        }
    }
}
