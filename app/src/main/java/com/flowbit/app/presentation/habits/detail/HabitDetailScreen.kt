package com.flowbit.app.presentation.habits.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    LaunchedEffect(habitId) { viewModel.load(habitId) }

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
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stats.habitEmoji, style = MaterialTheme.typography.displaySmall)
                    Spacer(Modifier.width(12.dp))
                    Text(stats.habitName, style = MaterialTheme.typography.headlineMedium)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        label = "Серия",
                        value = "${stats.currentStreak} дн.",
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label = "Рекорд",
                        value = "${stats.longestStreak} дн.",
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label = "Выполнено",
                        value = "${(stats.completionRate * 100).toInt()}%",
                        modifier = Modifier.weight(1f),
                    )
                }

                Text("Всего выполнений: ${stats.totalCompletions}", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge)
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
