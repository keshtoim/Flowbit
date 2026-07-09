package com.flowbit.app.presentation.settings

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowbit.app.BuildConfig
import com.flowbit.app.data.database.dao.HabitDao
import com.flowbit.app.data.database.entity.HabitEntity
import com.flowbit.app.data.database.entity.HabitEntryEntity
import com.flowbit.app.domain.model.Habit
import com.flowbit.app.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import javax.inject.Inject

data class SettingsUiState(
    val isDarkTheme: Boolean = false,
    val habits: List<Habit> = emptyList(),
    val backupMessage: String? = null,
    val isImporting: Boolean = false,
    val appVersion: String = "",
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>,
    private val repository: HabitRepository,
    private val dao: HabitDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(appVersion = BuildConfig.VERSION_NAME) }
        viewModelScope.launch {
            dataStore.data.map { prefs -> prefs[DARK_THEME_KEY] ?: false }
                .collect { isDark -> _uiState.update { it.copy(isDarkTheme = isDark) } }
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

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[DARK_THEME_KEY] = enabled }
        }
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

    fun backupData(uri: Uri) {
        viewModelScope.launch {
            try {
                val habits = dao.getAllHabitsList()
                val entries = dao.getAllEntries()

                val root = JSONObject().apply {
                    put("version", 1)
                    put("exportedAt", LocalDate.now().toString())
                    put("habits", JSONArray().apply {
                        habits.forEach { h -> put(habitToJson(h)) }
                    })
                    put("entries", JSONArray().apply {
                        entries.forEach { e -> put(entryToJson(e)) }
                    })
                }

                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(root.toString(2).toByteArray(Charsets.UTF_8))
                }
                _uiState.update { it.copy(backupMessage = "Бекап сохранён") }
            } catch (e: Exception) {
                _uiState.update { it.copy(backupMessage = "Ошибка бекапа: ${e.message}") }
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

                val habitEntities = (0 until habitsJson.length()).map { i ->
                    jsonToHabit(habitsJson.getJSONObject(i))
                }
                val entryEntities = (0 until entriesJson.length()).map { i ->
                    jsonToEntry(entriesJson.getJSONObject(i))
                }

                dao.insertAllHabits(habitEntities)
                dao.insertAllEntries(entryEntities)

                _uiState.update { it.copy(backupMessage = "Импорт завершён") }
            } catch (e: Exception) {
                _uiState.update { it.copy(backupMessage = "Ошибка импорта: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isImporting = false) }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(backupMessage = null) }
    }

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
        h.photoUri?.let { put("photoUri", it) }
        put("isPhotoHidden", h.isPhotoHidden)
        h.audioUri?.let { put("audioUri", it) }
    }

    private fun entryToJson(e: HabitEntryEntity) = JSONObject().apply {
        put("id", e.id)
        put("habitId", e.habitId)
        put("date", e.date)
        put("completedCount", e.completedCount)
    }

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
        photoUri = j.optString("photoUri").takeIf { it.isNotEmpty() },
        isPhotoHidden = j.optBoolean("isPhotoHidden", false),
        audioUri = j.optString("audioUri").takeIf { it.isNotEmpty() },
    )

    private fun jsonToEntry(j: JSONObject) = HabitEntryEntity(
        id = j.getLong("id"),
        habitId = j.getLong("habitId"),
        date = j.getString("date"),
        completedCount = j.getInt("completedCount"),
    )

    companion object {
        val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
    }
}
