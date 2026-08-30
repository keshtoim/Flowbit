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

    // ── Serialization ─────────────────────────────────────────────────────────

    private fun habitToJson(h: HabitEntity) = JSONObject().apply {
        put("id", h.id)
        put("name", h.name)
        put("emoji", h.emoji)
        put("colorHex", h.colorHex)
        put("targetCount", h.targetCount)
        put("frequency", h.frequency)
        put("scheduledDays", h.scheduledDays)
        put("startDate", h.startDate)
        put("isArchived", h.isArchived)
        put("showInWidget", h.showInWidget)
        put("createdAt", h.createdAt)
        put("sortOrder", h.sortOrder)
        put("isPhotoHidden", h.isPhotoHidden)
        put("periodGoalType", h.periodGoalType)
        put("periodGoalCount", h.periodGoalCount)
        put("timerSeconds", h.timerSeconds)
        h.photoUri?.let { put("photoUri", it) }
        h.audioUri?.let { put("audioUri", it) }
        h.tagId?.let { put("tagId", it) }
        h.unit?.let { put("unit", it) }
    }

    private fun entryToJson(e: HabitEntryEntity) = JSONObject().apply {
        put("id", e.id)
        put("habitId", e.habitId)
        put("date", e.date)
        put("completedCount", e.completedCount)
        put("isSkipped", e.isSkipped)
        e.note?.let { put("note", it) }
        e.markedAt?.let { put("markedAt", it) }
    }

    private fun reminderToJson(r: ReminderEntity) = JSONObject().apply {
        put("id", r.id)
        put("habitId", r.habitId)
        put("timeHour", r.timeHour)
        put("timeMinute", r.timeMinute)
        put("isEnabled", r.isEnabled)
    }

    // ── Deserialization ────────────────────────────────────────────────────────

    private fun jsonToHabit(j: JSONObject) = HabitEntity(
        id = j.getLong("id"),
        name = j.getString("name"),
        emoji = j.getString("emoji"),
        colorHex = j.getString("colorHex"),
        targetCount = j.getInt("targetCount"),
        frequency = j.getString("frequency"),
        scheduledDays = j.optString("scheduledDays", ""),
        startDate = j.getString("startDate"),
        isArchived = j.getBoolean("isArchived"),
        showInWidget = j.getBoolean("showInWidget"),
        createdAt = j.getString("createdAt"),
        sortOrder = j.optInt("sortOrder", 0),
        isPhotoHidden = j.optBoolean("isPhotoHidden", false),
        periodGoalType = j.optString("periodGoalType", "NONE"),
        periodGoalCount = j.optInt("periodGoalCount", 0),
        timerSeconds = j.optInt("timerSeconds", 0),
        photoUri = j.optString("photoUri").takeIf { it.isNotEmpty() },
        audioUri = j.optString("audioUri").takeIf { it.isNotEmpty() },
        tagId = if (j.has("tagId")) j.getLong("tagId") else null,
        unit = j.optString("unit").takeIf { it.isNotEmpty() },
    )

    private fun jsonToEntry(j: JSONObject) = HabitEntryEntity(
        id = j.getLong("id"),
        habitId = j.getLong("habitId"),
        date = j.getString("date"),
        completedCount = j.getInt("completedCount"),
        isSkipped = j.optBoolean("isSkipped", false),
        note = j.optString("note").takeIf { it.isNotEmpty() },
        markedAt = j.optString("markedAt").takeIf { it.isNotEmpty() },
    )

    private fun jsonToReminder(j: JSONObject) = ReminderEntity(
        id = j.getLong("id"),
        habitId = j.getLong("habitId"),
        timeHour = j.getInt("timeHour"),
        timeMinute = j.getInt("timeMinute"),
        isEnabled = j.getBoolean("isEnabled"),
    )

    companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val COMPACT_MODE_KEY = booleanPreferencesKey("compact_mode")
    }
}
