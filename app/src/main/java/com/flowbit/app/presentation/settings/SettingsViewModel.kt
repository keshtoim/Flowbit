package com.flowbit.app.presentation.settings

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowbit.app.BuildConfig
import com.flowbit.app.R
import com.flowbit.app.data.receiver.EveningCheckReceiver
import com.flowbit.app.domain.model.Habit
import com.flowbit.app.domain.model.ThemeMode
import com.flowbit.app.domain.repository.BackupRepository
import com.flowbit.app.domain.repository.HabitRepository
import com.flowbit.app.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    private val prefs: PreferencesRepository,
    private val backup: BackupRepository,
    private val repository: HabitRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(appVersion = BuildConfig.VERSION_NAME) }
        viewModelScope.launch {
            prefs.themeMode.collect { mode -> _uiState.update { it.copy(themeMode = mode) } }
        }
        viewModelScope.launch {
            prefs.isCompactMode.collect { v -> _uiState.update { it.copy(isCompactMode = v) } }
        }
        viewModelScope.launch {
            prefs.formStyle.collect { v -> _uiState.update { it.copy(formStyle = v) } }
        }
        viewModelScope.launch {
            prefs.analyticsStyle.collect { v -> _uiState.update { it.copy(analyticsStyle = v) } }
        }
        viewModelScope.launch {
            prefs.accentColorHex.collect { v -> _uiState.update { it.copy(accentColorHex = v) } }
        }
        val currentLang = AppCompatDelegate.getApplicationLocales().toLanguageTags()
            .let { if (it.contains("en")) "en" else "ru" }
        _uiState.update { it.copy(currentLanguage = currentLang) }
        val sp = context.getSharedPreferences(EveningCheckReceiver.PREFS_NAME, Context.MODE_PRIVATE)
        _uiState.update {
            it.copy(
                eveningEnabled = sp.getBoolean(EveningCheckReceiver.KEY_EVENING_ENABLED, true),
                eveningHour    = sp.getInt(EveningCheckReceiver.KEY_EVENING_HOUR, 20),
                eveningMinute  = sp.getInt(EveningCheckReceiver.KEY_EVENING_MINUTE, 0),
            )
        }
        loadHabits()
    }

    private fun loadHabits() {
        viewModelScope.launch {
            repository.getAllHabits().collect { habits -> _uiState.update { it.copy(habits = habits) } }
        }
    }

    fun setThemeMode(mode: ThemeMode) { viewModelScope.launch { prefs.setThemeMode(mode) } }
    fun setCompactMode(enabled: Boolean) { viewModelScope.launch { prefs.setCompactMode(enabled) } }
    fun setFormStyle(style: String) { viewModelScope.launch { prefs.setFormStyle(style) } }
    fun setAnalyticsStyle(style: String) { viewModelScope.launch { prefs.setAnalyticsStyle(style) } }
    fun setAccentColor(hex: String) { viewModelScope.launch { prefs.setAccentColor(hex) } }
    fun clearAccentColor() { viewModelScope.launch { prefs.clearAccentColor() } }

    fun setLanguage(lang: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang))
        _uiState.update { it.copy(currentLanguage = lang, needsRecreate = true) }
    }

    fun clearNeedsRecreate() { _uiState.update { it.copy(needsRecreate = false) } }

    fun moveHabitUp(habitId: Long) {
        viewModelScope.launch {
            val habits = _uiState.value.habits.toMutableList()
            val idx = habits.indexOfFirst { it.id == habitId }
            if (idx <= 0) return@launch
            habits.add(idx - 1, habits.removeAt(idx))
            habits.forEachIndexed { i, h -> repository.updateSortOrder(h.id, i) }
        }
    }

    fun moveHabitDown(habitId: Long) {
        viewModelScope.launch {
            val habits = _uiState.value.habits.toMutableList()
            val idx = habits.indexOfFirst { it.id == habitId }
            if (idx < 0 || idx >= habits.size - 1) return@launch
            habits.add(idx + 1, habits.removeAt(idx))
            habits.forEachIndexed { i, h -> repository.updateSortOrder(h.id, i) }
        }
    }

    fun setEveningEnabled(enabled: Boolean) {
        context.getSharedPreferences(EveningCheckReceiver.PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(EveningCheckReceiver.KEY_EVENING_ENABLED, enabled).apply()
        _uiState.update { it.copy(eveningEnabled = enabled) }
        if (enabled) EveningCheckReceiver.schedule(context) else EveningCheckReceiver.cancel(context)
    }

    fun setEveningTime(hour: Int, minute: Int) {
        val h = hour.coerceIn(0, 23); val m = minute.coerceIn(0, 59)
        context.getSharedPreferences(EveningCheckReceiver.PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(EveningCheckReceiver.KEY_EVENING_HOUR, h)
            .putInt(EveningCheckReceiver.KEY_EVENING_MINUTE, m).apply()
        _uiState.update { it.copy(eveningHour = h, eveningMinute = m) }
        if (_uiState.value.eveningEnabled) EveningCheckReceiver.schedule(context)
    }

    fun backupData(uri: Uri) {
        viewModelScope.launch {
            try {
                backup.exportJson(uri)
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
                backup.importJson(uri)
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
                backup.exportCsv(uri)
                _uiState.update { it.copy(backupMessage = "CSV экспортирован") }
            } catch (e: Exception) {
                _uiState.update { it.copy(backupMessage = "Ошибка экспорта CSV: ${e.message}") }
            }
        }
    }

    fun clearMessage() { _uiState.update { it.copy(backupMessage = null) } }
}
