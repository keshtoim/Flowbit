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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.flowbit.app.R
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

    LaunchedEffect(uiState.needsRecreate) {
        if (uiState.needsRecreate) {
            viewModel.clearNeedsRecreate()
            (context as? android.app.Activity)?.recreate()
        }
    }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var pendingLanguage by remember { mutableStateOf<String?>(null) }

    // Section expansion state; порядок привычек collapsed by default
    var appearanceExpanded by rememberSaveable { mutableStateOf(true) }
    var languageExpanded by rememberSaveable { mutableStateOf(true) }
    var notificationsExpanded by rememberSaveable { mutableStateOf(true) }
    var dataExpanded by rememberSaveable { mutableStateOf(true) }
    var orderExpanded by rememberSaveable { mutableStateOf(false) }
    var aboutExpanded by rememberSaveable { mutableStateOf(true) }

    val isSearchActive = searchQuery.isNotBlank()
    val q = searchQuery.trim().lowercase()

    fun String.matchesQuery() = !isSearchActive || lowercase().contains(q)

    val showAppearance = "внешний вид тема системная светлая тёмная appearance theme".matchesQuery()
    val showLanguage = "язык русский english language".matchesQuery()
    val showNotifications = "уведомления notifications".matchesQuery()
    val showData = "данные бекап импорт data backup import".matchesQuery()
    val showOrder = "порядок привычек order habits".matchesQuery()
    val showAbout = "о приложении версия about version".matchesQuery()

    val appearanceVisible = if (isSearchActive) showAppearance else appearanceExpanded
    val languageVisible = if (isSearchActive) showLanguage else languageExpanded
    val notificationsVisible = if (isSearchActive) showNotifications else notificationsExpanded
    val dataVisible = if (isSearchActive) showData else dataExpanded
    val orderVisible = if (isSearchActive) showOrder else orderExpanded
    val aboutVisible = if (isSearchActive) showAbout else aboutExpanded

    // Notification permission
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
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
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
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // ── Поиск ────────────────────────────────────────────────────────
            item(key = "search") {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Поиск настроек…") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                )
                Spacer(Modifier.height(4.dp))
            }

            // ── Внешний вид ───────────────────────────────────────────────────
            if (!isSearchActive || showAppearance) {
                item(key = "header_appearance") {
                    CollapsibleSectionHeader(
                        icon = { Icon(Icons.Default.DarkMode, null, Modifier.size(18.dp)) },
                        title = stringResource(R.string.appearance_section),
                        expanded = appearanceVisible,
                        onToggle = { if (!isSearchActive) appearanceExpanded = !appearanceExpanded },
                    )
                }
                item(key = "content_appearance") {
                    AnimatedVisibility(
                        visible = appearanceVisible,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        SettingsCard {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                Text(
                                    text = "Тема оформления",
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = uiState.themeMode == ThemeMode.SYSTEM,
                                        onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                                        label = { Text("Системная") },
                                    )
                                    FilterChip(
                                        selected = uiState.themeMode == ThemeMode.LIGHT,
                                        onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                                        label = { Text("Светлая") },
                                    )
                                    FilterChip(
                                        selected = uiState.themeMode == ThemeMode.DARK,
                                        onClick = { viewModel.setThemeMode(ThemeMode.DARK) },
                                        label = { Text("Тёмная") },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Язык ──────────────────────────────────────────────────────────
            if (!isSearchActive || showLanguage) {
                item(key = "header_language") {
                    CollapsibleSectionHeader(
                        icon = { Icon(Icons.Default.Language, null, Modifier.size(18.dp)) },
                        title = stringResource(R.string.language_section),
                        expanded = languageVisible,
                        onToggle = { if (!isSearchActive) languageExpanded = !languageExpanded },
                    )
                }
                item(key = "content_language") {
                    AnimatedVisibility(
                        visible = languageVisible,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        val effectiveLang = pendingLanguage ?: uiState.currentLanguage
                        val langChanged = pendingLanguage != null && pendingLanguage != uiState.currentLanguage
                        SettingsCard {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                Text(
                                    text = stringResource(R.string.language_title),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Text(
                                    text = if (effectiveLang == "en") stringResource(R.string.lang_en)
                                           else stringResource(R.string.lang_ru),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilterChip(
                                            selected = effectiveLang == "ru",
                                            onClick = { pendingLanguage = "ru" },
                                            label = { Text("RU") },
                                        )
                                        FilterChip(
                                            selected = effectiveLang == "en",
                                            onClick = { pendingLanguage = "en" },
                                            label = { Text("EN") },
                                        )
                                    }
                                    if (langChanged) {
                                        TextButton(
                                            onClick = {
                                                viewModel.setLanguage(pendingLanguage!!)
                                                pendingLanguage = null
                                            },
                                        ) {
                                            Text(stringResource(R.string.save))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Уведомления ───────────────────────────────────────────────────
            if (!isSearchActive || showNotifications) {
                item(key = "header_notifications") {
                    CollapsibleSectionHeader(
                        icon = { Icon(Icons.Default.NotificationsActive, null, Modifier.size(18.dp)) },
                        title = stringResource(R.string.notifications_section),
                        expanded = notificationsVisible,
                        onToggle = { if (!isSearchActive) notificationsExpanded = !notificationsExpanded },
                    )
                }
                item(key = "content_notifications") {
                    AnimatedVisibility(
                        visible = notificationsVisible,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        SettingsCard {
                            SettingsRow(
                                title = stringResource(R.string.notif_permission_title),
                                subtitle = if (hasNotifPermission) stringResource(R.string.notif_permission_enabled)
                                           else stringResource(R.string.notif_permission_prompt),
                            ) {
                                if (hasNotifPermission) {
                                    Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                                } else {
                                    TextButton(onClick = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        } else {
                                            val i = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                            context.startActivity(i)
                                        }
                                    }) { Text(stringResource(R.string.allow)) }
                                }
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                SettingsRow(
                                    title = stringResource(R.string.exact_alarm_title),
                                    subtitle = if (canExactAlarm) stringResource(R.string.exact_alarm_enabled)
                                               else stringResource(R.string.exact_alarm_prompt),
                                ) {
                                    if (canExactAlarm) {
                                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                                    } else {
                                        TextButton(onClick = {
                                            val i = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                                .apply { data = Uri.fromParts("package", context.packageName, null) }
                                            context.startActivity(i)
                                            canExactAlarm = alarmManager?.canScheduleExactAlarms() ?: false
                                        }) { Text(stringResource(R.string.enable)) }
                                    }
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            SettingsRow(
                                title = stringResource(R.string.notif_settings_title),
                                subtitle = stringResource(R.string.notif_settings_subtitle),
                            ) {
                                TextButton(onClick = {
                                    val i = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    context.startActivity(i)
                                }) { Text(stringResource(R.string.open)) }
                            }
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            SettingsRow(
                                title = "Вечерний дайджест",
                                subtitle = "Напоминание о невыполненных привычках",
                            ) {
                                Switch(
                                    checked = uiState.eveningEnabled,
                                    onCheckedChange = viewModel::setEveningEnabled,
                                )
                            }
                            if (uiState.eveningEnabled) {
                                var showTimePicker by remember { mutableStateOf(false) }
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                SettingsRow(
                                    title = "Время дайджеста",
                                    subtitle = "%02d:%02d".format(uiState.eveningHour, uiState.eveningMinute),
                                ) {
                                    TextButton(onClick = { showTimePicker = true }) { Text("Изменить") }
                                }
                                if (showTimePicker) {
                                    EveningTimePickerDialog(
                                        initialHour = uiState.eveningHour,
                                        initialMinute = uiState.eveningMinute,
                                        onConfirm = { h, m ->
                                            viewModel.setEveningTime(h, m)
                                            showTimePicker = false
                                        },
                                        onDismiss = { showTimePicker = false },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Данные ────────────────────────────────────────────────────────
            if (!isSearchActive || showData) {
                item(key = "header_data") {
                    CollapsibleSectionHeader(
                        icon = { Icon(Icons.Default.Download, null, Modifier.size(18.dp)) },
                        title = stringResource(R.string.data_section),
                        expanded = dataVisible,
                        onToggle = { if (!isSearchActive) dataExpanded = !dataExpanded },
                    )
                }
                item(key = "content_data") {
                    AnimatedVisibility(
                        visible = dataVisible,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        SettingsCard {
                            SettingsRow(
                                title = stringResource(R.string.backup_title),
                                subtitle = stringResource(R.string.backup_subtitle),
                            ) {
                                IconButton(onClick = {
                                    backupLauncher.launch("flowbit_backup_${LocalDate.now()}.json")
                                }) {
                                    Icon(
                                        Icons.Default.Upload,
                                        contentDescription = stringResource(R.string.backup_title),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            SettingsRow(
                                title = stringResource(R.string.import_title),
                                subtitle = stringResource(R.string.import_subtitle),
                            ) {
                                IconButton(onClick = {
                                    importLauncher.launch(arrayOf("application/json", "*/*"))
                                }) {
                                    Icon(
                                        Icons.Default.Download,
                                        contentDescription = stringResource(R.string.import_title),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Порядок привычек ──────────────────────────────────────────────
            if (uiState.habits.isNotEmpty() && (!isSearchActive || showOrder)) {
                item(key = "header_order") {
                    CollapsibleSectionHeader(
                        icon = { Icon(Icons.Default.Reorder, null, Modifier.size(18.dp)) },
                        title = stringResource(R.string.order_section),
                        expanded = orderVisible,
                        onToggle = { if (!isSearchActive) orderExpanded = !orderExpanded },
                    )
                }
                item(key = "content_order") {
                    AnimatedVisibility(
                        visible = orderVisible,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        SettingsCard {
                            Column {
                                uiState.habits.forEachIndexed { index, habit ->
                                    if (index > 0) {
                                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                    }
                                    HabitOrderItemRow(
                                        habit = habit,
                                        isFirst = index == 0,
                                        isLast = index == uiState.habits.size - 1,
                                        onMoveUp = { viewModel.moveHabitUp(habit.id) },
                                        onMoveDown = { viewModel.moveHabitDown(habit.id) },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── О приложении ──────────────────────────────────────────────────
            if (!isSearchActive || showAbout) {
                item(key = "header_about") {
                    CollapsibleSectionHeader(
                        icon = { Icon(Icons.Default.Info, null, Modifier.size(18.dp)) },
                        title = stringResource(R.string.about_section),
                        expanded = aboutVisible,
                        onToggle = { if (!isSearchActive) aboutExpanded = !aboutExpanded },
                    )
                }
                item(key = "content_about") {
                    AnimatedVisibility(
                        visible = aboutVisible,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        SettingsCard {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Flowbit", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    stringResource(R.string.about_subtitle),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    stringResource(R.string.version, uiState.appVersion),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun CollapsibleSectionHeader(
    icon: @Composable () -> Unit,
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 8.dp),
    ) {
        icon()
        Spacer(Modifier.size(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
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
private fun EveningTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var hourText by remember { mutableStateOf(initialHour.toString()) }
    var minuteText by remember { mutableStateOf("%02d".format(initialMinute)) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Время дайджеста") },
        text = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = hourText,
                    onValueChange = { if (it.length <= 2) hourText = it.filter { c -> c.isDigit() } },
                    label = { Text("Час") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                Text(":", style = MaterialTheme.typography.headlineMedium)
                OutlinedTextField(
                    value = minuteText,
                    onValueChange = { if (it.length <= 2) minuteText = it.filter { c -> c.isDigit() } },
                    label = { Text("Минута") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val h = hourText.toIntOrNull()?.coerceIn(0, 23) ?: initialHour
                val m = minuteText.toIntOrNull()?.coerceIn(0, 59) ?: initialMinute
                onConfirm(h, m)
            }) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
    )
}

@Composable
private fun HabitOrderItemRow(
    habit: Habit,
    isFirst: Boolean,
    isLast: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
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
