package com.flowbit.app.domain.repository

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.flowbit.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val themeMode: Flow<ThemeMode>
    val isCompactMode: Flow<Boolean>
    val formStyle: Flow<String>
    val analyticsStyle: Flow<String>
    val accentColorHex: Flow<String?>

    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setCompactMode(enabled: Boolean)
    suspend fun setFormStyle(style: String)
    suspend fun setAnalyticsStyle(style: String)
    suspend fun setAccentColor(hex: String)
    suspend fun clearAccentColor()

    companion object {
        val THEME_MODE_KEY     = stringPreferencesKey("theme_mode")
        val COMPACT_MODE_KEY   = booleanPreferencesKey("compact_mode")
        val FORM_STYLE_KEY     = stringPreferencesKey("form_style")
        val ANALYTICS_STYLE_KEY = stringPreferencesKey("analytics_style")
        val ACCENT_COLOR_KEY   = stringPreferencesKey("accent_color")
    }
}
