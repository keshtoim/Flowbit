package com.flowbit.app.presentation.settings

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowbit.app.BuildConfig
import com.flowbit.app.R
import com.flowbit.app.data.backup.HabitSerializer
import com.flowbit.app.data.database.dao.HabitDao
import com.flowbit.app.data.receiver.EveningCheckReceiver
import com.flowbit.app.data.database.dao.ReminderDao
import com.flowbit.app.data.database.entity.HabitEntity
import com.flowbit.app.data.database.entity.HabitEntryEntity
import com.flowbit.app.data.database.entity.ReminderEntity
import com.flowbit.app.domain.model.Habit
import com.flowbit.app.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import javax.inject.Inject

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val habits: List<Habit> = emptyList(),
    val backupMessage: String? = null,
    val isImporting: Boolean = false,
    val appVersion: String = "",
    val currentLanguage: String = "ru",
    val needsRecreate: Boolean = false,
    val eveningEnabled: Boolean = true,
    val eveningHour: Int = 20,
    val eveningMinute: Int = 0,
    val isCompactMode: Boolean = false,
    val formStyle: String = "A",
    val analyticsStyle: String = "A",
    val accentColorHex: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>,
    private val repository: HabitRepository,
    private val dao: HabitDao,
    private val reminderDao: ReminderDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(appVersion = BuildConfig.VERSION_NAME) }
        viewModelScope.launch {
            dataStore.data.map { prefs ->
                when (prefs[THEME_MODE_KEY]) {
                    "light" -> ThemeMode.LIGHT
                    "dark"  -> ThemeMode.DARK
                    else    -> ThemeMode.SYSTEM
                }
            }.collect { mode -> _uiState.update { it.copy(themeMode = mode) } }
        }
        viewModelScope.launch {
            dataStore.data.map { prefs -> prefs[COMPACT_MODE_KEY] ?: false }
                .collect { compact -> _uiState.update { it.copy(isCompactMode = compact) } }
        }
        viewModelScope.launch {
            dataStore.data.map { prefs -> prefs[FORM_STYLE_KEY] ?: "A" }
                .collect { style -> _uiState.update { it.copy(formStyle = style) } }
        }
        viewModelScope.launch {
            dataStore.data.map { prefs -> prefs[ANALYTICS_STYLE_KEY] ?: "A" }
                .collect { style -> _uiState.update { it.copy(analyticsStyle = style) } }
        }
        viewModelScope.launch {
            dataStore.data.map { prefs -> prefs[ACCENT_COLOR_KEY] }
                .collect { hex -> _uiState.update { it.copy(accentColorHex = hex) } }
        }
        val currentLang = AppCompatDelegate.getApplicationLocales().toLanguageTags()
            .let { if (it.contains("en")) "en" else "ru" }
        _uiState.update { it.copy(currentLanguage = currentLang) }

        // Загружаем настройки вечернего дайджеста из SharedPreferences
        val prefs = context.getSharedPreferences(EveningCheckReceiver.PREFS_NAME, Context.MODE_PRIVATE)
        _uiState.update {
            it.copy(
                eveningEnabled = prefs.getBoolean(EveningCheckReceiver.KEY_EVENING_ENABLED, true),
                eveningHour = prefs.getInt(EveningCheckReceiver.KEY_EVENING_HOUR, 20),
                eveningMinute = prefs.getInt(EveningCheckReceiver.KEY_EVENING_MINUTE, 0),
            )
        }

        loadHabits()
    }

    private fun loadHabits() {
        viewModelScope.launch {
            repository.getAllHabits().collect { habits ->
                _uiState.update { it.copy(habits = habits) }
            }
        }
    }

    fun setCompactMode(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[COMPACT_MODE_KEY] = enabled }
        }
    }

    fun setFormStyle(style: String) {
        viewModelScope.launch {
            dataStore.edit { it[FORM_STYLE_KEY] = style }
        }
    }

    fun setAnalyticsStyle(style: String) {
        viewModelScope.launch {
            dataStore.edit { it[ANALYTICS_STYLE_KEY] = style }
        }
    }

    fun setAccentColor(hex: String) {
        viewModelScope.launch {
            dataStore.edit { it[ACCENT_COLOR_KEY] = hex }
        }
    }

    fun clearAccentColor() {
        viewModelScope.launch {
            dataStore.edit { it.remove(ACCENT_COLOR_KEY) }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            val value = when (mode) {
                ThemeMode.SYSTEM -> "system"
                ThemeMode.LIGHT  -> "light"
                ThemeMode.DARK   -> "dark"
            }
            dataStore.edit { it[THEME_MODE_KEY] = value }
        }
    }

    fun setLanguage(lang: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang))
        _uiState.update { it.copy(currentLanguage = lang, needsRecreate = true) }
    }

    fun clearNeedsRecreate() {
        _uiState.update { it.copy(needsRecreate = false) }
    }

    fun moveHabitUp(habitId: Long) {
        viewModelScope.launch {
            val habits = _uiState.value.habits.toMutableList()
            val idx = habits.indexOfFirst { it.id == habitId }
            if (idx <= 0) return@launch
            habits.add(idx - 1, habits.removeAt(idx))
            updateSortOrders(habits)
        }
    }

    fun moveHabitDown(habitId: Long) {
        viewModelScope.launch {
            val habits = _uiState.value.habits.toMutableList()
            val idx = habits.indexOfFirst { it.id == habitId }
            if (idx < 0 || idx >= habits.size - 1) return@launch
            habits.add(idx + 1, habits.removeAt(idx))
            updateSortOrders(habits)
        }
    }

    private suspend fun updateSortOrders(ordered: List<Habit>) {
        ordered.forEachIndexed { index, habit ->
            dao.updateSortOrder(habit.id, index)
        }
    }

    fun setEveningEnabled(enabled: Boolean) {
        context.getSharedPreferences(EveningCheckReceiver.PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(EveningCheckReceiver.KEY_EVENING_ENABLED, enabled).apply()
        _uiState.update { it.copy(eveningEnabled = enabled) }
        if (enabled) EveningCheckReceiver.schedule(context) else EveningCheckReceiver.cancel(context)
    }

    fun setEveningTime(hour: Int, minute: Int) {
        val h = hour.coerceIn(0, 23)
        val m = minute.coerceIn(0, 59)
        context.getSharedPreferences(EveningCheckReceiver.PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(EveningCheckReceiver.KEY_EVENING_HOUR, h)
            .putInt(EveningCheckReceiver.KEY_EVENING_MINUTE, m).apply()
        _uiState.update { it.copy(eveningHour = h, eveningMinute = m) }
        if (_uiState.value.eveningEnabled) EveningCheckReceiver.schedule(context)
    }

    fun backupData(uri: Uri) {
        viewModelScope.launch {
            try {
                val habits = dao.getAllHabitsList()
                val entries = dao.getAllEntries()
                val reminders = reminderDao.getAllReminders()

                val root = JSONObject().apply {
                    put("version", 2)
                    put("exportedAt", LocalDate.now().toString())
                    put("habits", JSONArray().apply {
                        habits.forEach { h -> put(habitToJson(h)) }
                    })
                    put("entries", JSONArray().apply {
                        entries.forEach { e -> put(entryToJson(e)) }
                    })
                    put("reminders", JSONArray().apply {
                        reminders.forEach { r -> put(reminderToJson(r)) }
                    })
                }

                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(root.toString(2).toByteArray(Charsets.UTF_8))
                }
                _uiState.update { it.copy(backupMessage = context.getString(R.string.backup_success)) }
            } catch (e: Exception) {
                _uiState.update { it.copy(backupMessage = context.getString(R.string.backup_error, e.message)) }
            }
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }
            try {
                val json = context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader().readText()
                } ?: throw IllegalStateException("Не удалось открыть файл")

                val root = JSONObject(json)
                val habitsJson = root.getJSONArray("habits")
                val entriesJson = root.getJSONArray("entries")
                val remindersJson = root.optJSONArray("reminders")

                val habitEntities = (0 until habitsJson.length()).map { i ->
                    jsonToHabit(habitsJson.getJSONObject(i))
                }
                val entryEntities = (0 until entriesJson.length()).map { i ->
                    jsonToEntry(entriesJson.getJSONObject(i))
                }

                dao.insertAllHabits(habitEntities)
                dao.insertAllEntries(entryEntities)

                if (remindersJson != null) {
                    for (i in 0 until remindersJson.length()) {
                        reminderDao.insertReminder(jsonToReminder(remindersJson.getJSONObject(i)))
                    }
                }

                _uiState.update { it.copy(backupMessage = context.getString(R.string.import_success)) }
            } catch (e: Exception) {
                _uiState.update { it.copy(backupMessage = context.getString(R.string.import_error, e.message)) }
            } finally {
                _uiState.update { it.copy(isImporting = false) }
            }
        }
    }

    fun exportCsv(uri: Uri) {
        viewModelScope.launch {
            try {
                val habits = dao.getAllHabitsList().associateBy { it.id }
                val entries = dao.getAllEntries()
                val sb = StringBuilder()
                sb.appendLine("habit_id,habit_name,date,completed_count,target_count,is_skipped,note,marked_at")
                entries.forEach { e ->
                    val h = habits[e.habitId]
                    val name = (h?.name ?: "").replace(",", ";").replace("\n", " ")
                    val note = (e.note ?: "").replace(",", ";").replace("\n", " ")
                    sb.appendLine("${e.habitId},\"$name\",${e.date},${e.completedCount},${h?.targetCount ?: 1},${e.isSkipped},\"$note\",${e.markedAt ?: ""}")
                }
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(sb.toString().toByteArray(Charsets.UTF_8))
                }
                _uiState.update { it.copy(backupMessage = "CSV экспортирован") }
            } catch (e: Exception) {
                _uiState.update { it.copy(backupMessage = "Ошибка экспорта CSV: ${e.message}") }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(backupMessage = null) }
    }

    // ── Serialization / Deserialization (делегируем в HabitSerializer) ─────────

    private fun habitToJson(h: HabitEntity) = HabitSerializer.habitToJson(h)
    private fun entryToJson(e: HabitEntryEntity) = HabitSerializer.entryToJson(e)
    private fun reminderToJson(r: ReminderEntity) = HabitSerializer.reminderToJson(r)
    private fun jsonToHabit(j: JSONObject) = HabitSerializer.jsonToHabit(j)
    private fun jsonToEntry(j: JSONObject) = HabitSerializer.jsonToEntry(j)
    private fun jsonToReminder(j: JSONObject) = HabitSerializer.jsonToReminder(j)

    companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val COMPACT_MODE_KEY = booleanPreferencesKey("compact_mode")
        val FORM_STYLE_KEY = stringPreferencesKey("form_style")
        val ANALYTICS_STYLE_KEY = stringPreferencesKey("analytics_style")
        val ACCENT_COLOR_KEY = stringPreferencesKey("accent_color")
    }
}
