package com.flowbit.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.flowbit.app.domain.model.ThemeMode
import com.flowbit.app.domain.repository.PreferencesRepository
import com.flowbit.app.domain.repository.PreferencesRepository.Companion.ACCENT_COLOR_KEY
import com.flowbit.app.domain.repository.PreferencesRepository.Companion.ANALYTICS_STYLE_KEY
import com.flowbit.app.domain.repository.PreferencesRepository.Companion.COMPACT_MODE_KEY
import com.flowbit.app.domain.repository.PreferencesRepository.Companion.FORM_STYLE_KEY
import com.flowbit.app.domain.repository.PreferencesRepository.Companion.THEME_MODE_KEY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : PreferencesRepository {

    override val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        when (prefs[THEME_MODE_KEY]) {
            "light" -> ThemeMode.LIGHT
            "dark"  -> ThemeMode.DARK
            else    -> ThemeMode.SYSTEM
        }
    }

    override val isCompactMode: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[COMPACT_MODE_KEY] ?: false }

    override val formStyle: Flow<String> =
        dataStore.data.map { prefs -> prefs[FORM_STYLE_KEY] ?: "A" }

    override val analyticsStyle: Flow<String> =
        dataStore.data.map { prefs -> prefs[ANALYTICS_STYLE_KEY] ?: "A" }

    override val accentColorHex: Flow<String?> =
        dataStore.data.map { prefs -> prefs[ACCENT_COLOR_KEY] }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[THEME_MODE_KEY] = when (mode) {
                ThemeMode.SYSTEM -> "system"
                ThemeMode.LIGHT  -> "light"
                ThemeMode.DARK   -> "dark"
            }
        }
    }

    override suspend fun setCompactMode(enabled: Boolean) {
        dataStore.edit { it[COMPACT_MODE_KEY] = enabled }
    }

    override suspend fun setFormStyle(style: String) {
        dataStore.edit { it[FORM_STYLE_KEY] = style }
    }

    override suspend fun setAnalyticsStyle(style: String) {
        dataStore.edit { it[ANALYTICS_STYLE_KEY] = style }
    }

    override suspend fun setAccentColor(hex: String) {
        dataStore.edit { it[ACCENT_COLOR_KEY] = hex }
    }

    override suspend fun clearAccentColor() {
        dataStore.edit { it.remove(ACCENT_COLOR_KEY) }
    }
}
