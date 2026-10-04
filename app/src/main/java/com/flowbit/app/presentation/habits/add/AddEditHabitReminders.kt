package com.flowbit.app.presentation.habits.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flowbit.app.domain.model.HabitReminder
import java.time.LocalTime

@Composable
fun RemindersSection(
    reminders: List<HabitReminder>,
    onAddReminder: (LocalTime) -> Unit,
    onRemoveReminder: (HabitReminder) -> Unit,
    onToggleReminder: (HabitReminder) -> Unit,
) {
    var showTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Напоминания", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { showTimePicker = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Добавить")
            }
        }
        reminders.forEach { reminder ->
            ReminderItem(
                reminder = reminder,
                onToggle = { onToggleReminder(reminder) },
                onDelete = { onRemoveReminder(reminder) },
            )
        }
        if (showTimePicker) {
            TimePickerDialog(
                onTimeSelected = { onAddReminder(it); showTimePicker = false },
                onDismiss = { showTimePicker = false },
            )
        }
    }
}

@Composable
fun ReminderItem(
    reminder: HabitReminder,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "%02d:%02d".format(reminder.time.hour, reminder.time.minute),
                style = MaterialTheme.typography.titleMedium,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = reminder.isEnabled, onCheckedChange = { onToggle() })
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onDelete) { Text("Удалить") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Время напоминания") },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = {
                onTimeSelected(LocalTime.of(state.hour, state.minute))
            }) { Text("ОК") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

@Composable
fun RecurringReminderSection(
    recurringEnabled: Boolean,
    startHour: Int,
    endHour: Int,
    intervalHours: Int,
    onToggle: () -> Unit,
    onStartHourChange: (Int) -> Unit,
    onEndHourChange: (Int) -> Unit,
    onIntervalChange: (Int) -> Unit,
) {
    val intervals = listOf(1, 2, 3, 4, 6)

    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Повторяющееся напоминание", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Напоминать несколько раз в день через равные интервалы",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = recurringEnabled, onCheckedChange = { onToggle() })
        }

        if (recurringEnabled) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "С %02d:00".format(startHour),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Slider(
                        value = startHour.toFloat(),
                        onValueChange = { onStartHourChange(it.toInt()) },
                        valueRange = 0f..22f,
                        steps = 21,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "До %02d:00".format(endHour),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Slider(
                        value = endHour.toFloat(),
                        onValueChange = { onEndHourChange(it.toInt()) },
                        valueRange = 1f..23f,
                        steps = 21,
                    )
                }
            }

            Text(
                "Интервал (часов):",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                intervals.forEach { h ->
                    FilterChip(
                        selected = intervalHours == h,
                        onClick = { onIntervalChange(h) },
                        label = { Text("${h}ч", style = MaterialTheme.typography.labelSmall) },
                    )
                }
            }

            val times = buildList {
                var h = startHour
                while (h <= endHour) {
                    add("%02d:00".format(h))
                    h += intervalHours
                }
            }
            if (times.isNotEmpty()) {
                Text(
                    text = "Напоминания: ${times.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
