package com.flowbit.app.presentation.settings

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.flowbit.app.domain.model.Habit
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // --- Notification permission state ---
    var hasNotifPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            else true
        )
    }
    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasNotifPermission = granted }

    val alarmManager = remember { context.getSystemService(AlarmManager::class.java) }
    var canExactAlarm by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                alarmManager?.canScheduleExactAlarms() ?: true
            else true
        )
    }

    // --- File pickers ---
    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? -> uri?.let { viewModel.backupData(it) } }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> uri?.let { viewModel.importData(it) } }

    LaunchedEffect(uiState.backupMessage) {
        uiState.backupMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {

            // — Внешний вид —
            item {
                Spacer(Modifier.height(8.dp))
                SectionHeader(
                    icon = { Icon(Icons.Default.DarkMode, null, Modifier.size(18.dp)) },
                    title = "Внешний вид",
                )
            }
            item {
                SettingsCard {
                    SettingsRow(
                        title = "Тёмная тема",
                        subtitle = "Переключить тёмный режим",
                    ) {
                        Switch(
                            checked = uiState.isDarkTheme,
                            onCheckedChange = viewModel::setDarkTheme,
                        )
                    }
                }
            }

            // — Уведомления —
            item {
                Spacer(Modifier.height(4.dp))
                SectionHeader(
                    icon = { Icon(Icons.Default.NotificationsActive, null, Modifier.size(18.dp)) },
                    title = "Уведомления",
                )
            }
            item {
                SettingsCard {
                    SettingsRow(
                        title = "Разрешение на уведомления",
                        subtitle = if (hasNotifPermission) "Уведомления включены" else "Нажмите, чтобы разрешить",
                    ) {
                        if (hasNotifPermission) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        } else {
                            TextButton(onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    context.startActivity(intent)
                                }
                            }) { Text("Разрешить") }
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsRow(
                            title = "Точные будильники",
                            subtitle = if (canExactAlarm) "Для точных напоминаний — включено"
                                       else "Нажмите для включения точных напоминаний",
                        ) {
                            if (canExactAlarm) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            } else {
                                TextButton(onClick = {
                                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                        .apply { data = Uri.fromParts("package", context.packageName, null) }
                                    context.startActivity(intent)
                                    // Обновить статус когда пользователь вернётся
                                    canExactAlarm = alarmManager?.canScheduleExactAlarms() ?: false
                                }) { Text("Включить") }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRow(
                        title = "Настройки уведомлений",
                        subtitle = "Открыть системные настройки уведомлений",
                    ) {
                        TextButton(onClick = {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            context.startActivity(intent)
                        }) { Text("Открыть") }
                    }
                }
            }

            // — Данные —
            item {
                Spacer(Modifier.height(4.dp))
                SectionHeader(
                    icon = { Icon(Icons.Default.Download, null, Modifier.size(18.dp)) },
                    title = "Данные",
                )
            }
            item {
                SettingsCard {
                    SettingsRow(
                        title = "Создать резервную копию",
                        subtitle = "Экспорт всех привычек и записей в JSON",
                    ) {
                        IconButton(onClick = {
                            backupLauncher.launch("flowbit_backup_${LocalDate.now()}.json")
                        }) {
                            Icon(
                                Icons.Default.Upload,
                                contentDescription = "Создать бекап",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRow(
                        title = "Импорт данных",
                        subtitle = "Восстановить привычки из файла резервной копии",
                    ) {
                        IconButton(onClick = {
                            importLauncher.launch(arrayOf("application/json", "*/*"))
                        }) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = "Импорт",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            // — Порядок привычек —
            if (uiState.habits.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(4.dp))
                    SectionHeader(
                        icon = { Icon(Icons.Default.Reorder, null, Modifier.size(18.dp)) },
                        title = "Порядок привычек",
                    )
                }
                items(uiState.habits, key = { it.id }) { habit ->
                    HabitOrderItem(
                        habit = habit,
                        isFirst = uiState.habits.first().id == habit.id,
                        isLast = uiState.habits.last().id == habit.id,
                        onMoveUp = { viewModel.moveHabitUp(habit.id) },
                        onMoveDown = { viewModel.moveHabitDown(habit.id) },
                    )
                }
            }

            // — О приложении —
            item {
                Spacer(Modifier.height(4.dp))
                SectionHeader(
                    icon = { Icon(Icons.Default.Info, null, Modifier.size(18.dp)) },
                    title = "О приложении",
                )
            }
            item {
                SettingsCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Flowbit", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Трекер привычек с виджетом и напоминаниями",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "Версия ${uiState.appVersion}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(icon: @Composable () -> Unit, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        icon()
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) { content() }
}

@Composable
private fun SettingsRow(title: String, subtitle: String, action: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        action()
    }
}

@Composable
private fun HabitOrderItem(
    habit: Habit,
    isFirst: Boolean,
    isLast: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = habit.emoji,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(end = 12.dp),
            )
            Text(
                text = habit.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onMoveUp, enabled = !isFirst) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = "Вверх",
                    tint = if (!isFirst) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                )
            }
            IconButton(onClick = onMoveDown, enabled = !isLast) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Вниз",
                    tint = if (!isLast) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                )
            }
        }
    }
}
