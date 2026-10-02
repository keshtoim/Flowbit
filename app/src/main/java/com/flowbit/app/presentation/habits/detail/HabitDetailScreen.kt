package com.flowbit.app.presentation.habits.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habitId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    viewModel: HabitDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val analyticsStyle by viewModel.analyticsStyle.collectAsState()
    LaunchedEffect(habitId) { viewModel.load(habitId) }

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
        if (uiState.stats == null) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        when (analyticsStyle) {
            "B" -> DetailLayoutB(uiState = uiState, viewModel = viewModel, padding = padding)
            "C" -> DetailLayoutC(uiState = uiState, viewModel = viewModel, padding = padding)
            else -> DetailLayoutA(uiState = uiState, viewModel = viewModel, padding = padding)
        }
    }
}
